#!/usr/bin/env bash
# julyConfig permission matrix (033 §021) — run inside WSL dev016
set -euo pipefail
BASE="${BASE:-http://127.0.0.1:11160}"
CFG="$BASE/klsjnh/system011/julyConfig/v1"
LOGIN="$BASE/klsjnh/iam/julyUser/v1/loginByUserName"
PASS="Test011!"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

login() {
  local acct="$1"
  curl -s -X POST "$LOGIN" -H "Content-Type: application/json" \
    -d "{\"userAccount\":\"${acct}\"}" >"$TMP"
  python3 - "$TMP" <<'PY'
import json,sys
d=json.load(open(sys.argv[1]))
tok=(d.get("data") or {}).get("token") or ""
print(tok)
print(f"  login statusCode={d.get('statusCode')} message={d.get('message')}", file=sys.stderr)
PY
}

call() {
  local tok="$1" meth="$2" path="$3" body="${4:-{}}"
  local code
  if [ "$meth" = GET ]; then
    code=$(curl -s -o "$TMP" -w "%{http_code}" -H "Authorization: Bearer ${tok}" "$path")
  else
    code=$(curl -s -o "$TMP" -w "%{http_code}" -X POST \
      -H "Authorization: Bearer ${tok}" -H "Content-Type: application/json" \
      -d "$body" "$path")
  fi
  local sc
  sc=$(python3 -c "import json; print(json.load(open('$TMP')).get('statusCode','?'))" 2>/dev/null || echo "?")
  local msg
  msg=$(python3 -c "import json; print(json.load(open('$TMP')).get('message','')[:80])" 2>/dev/null || echo "")
  printf "HTTP=%s SC=%s %s\n" "$code" "$sc" "$msg"
}

echo "=== julyConfig permission matrix @ $BASE ==="
for acct in klsjnh test011 test013 test015 test016; do
  echo "---- $acct ----"
  tok=$(login "$acct")
  if [ -z "$tok" ]; then
    echo "LOGIN FAIL"
    continue
  fi
  printf "  select: "; call "$tok" POST "$CFG/selectListByPage" '{"pageIndex":1,"pageSize":10}'
  printf "  insert: "; call "$tok" POST "$CFG/insert" "{\"code\":\"demo.tmp.${acct}.$$\",\"data\":\"x\",\"status\":\"1\"}"
  printf "  delete: "; call "$tok" POST "$CFG/logicDelete" '{"id":"00000000000000000000000000000000"}'
  printf "  export: "; call "$tok" POST "$CFG/export" '{}'
  printf "  backup: "; call "$tok" POST "$CFG/backup011" '{}'
done
