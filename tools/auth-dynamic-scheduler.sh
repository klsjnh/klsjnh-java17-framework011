#!/usr/bin/env bash
# Dynamic auth: grant start → can start → revoke → cannot (WSL dev016)
set -euo pipefail
BASE="${BASE:-http://127.0.0.1:11160}"
LOGIN="$BASE/klsjnh/iam/julyUser/v1/loginByUserName"
ROLE="$BASE/klsjnh/iam/julyRole/v1"
SCHED="$BASE/klsjnh/system011/julyScheduler/v1"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT

login() {
  local acct="$1"
  curl -s -X POST "$LOGIN" -H "Content-Type: application/json" \
    -d "{\"userAccount\":\"${acct}\"}" >"$TMP"
  python3 - "$TMP" <<'PY'
import json,sys
d=json.load(open(sys.argv[1]))
print((d.get("data") or {}).get("token") or "")
print(f"  login {sys.argv[1]} statusCode={d.get('statusCode')}", file=sys.stderr)
PY
}

sc() {
  python3 -c "import json; print(json.load(open('$TMP')).get('statusCode','?'))" 2>/dev/null || echo "?"
}

msg() {
  python3 -c "import json; print(json.load(open('$TMP')).get('message','')[:120])" 2>/dev/null || echo ""
}

echo "=== dynamic auth julyScheduler @ $BASE ==="

ADMIN=$(login klsjnh)
START=$(login test017)
if [ -z "$ADMIN" ] || [ -z "$START" ]; then
  echo "LOGIN FAIL"; exit 1
fi

# resolve role id for role_scheduler_start
curl -s -X POST "$ROLE/selectListByPage" \
  -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d '{"pageIndex":1,"pageSize":50,"keyword":"role_scheduler_start"}' >"$TMP"
ROLE_ID=$(python3 - "$TMP" <<'PY'
import json,sys
d=json.load(open(sys.argv[1]))
rows=(d.get("data") or {}).get("rows") or []
for r in rows:
  if r.get("roleCode")=="role_scheduler_start":
    print(r.get("id") or ""); break
PY
)
echo "role_scheduler_start id=$ROLE_ID"
if [ -z "$ROLE_ID" ]; then echo "ROLE NOT FOUND"; exit 1; fi

# resolve demo scheduler id
curl -s -X POST "$SCHED/selectListByPage" \
  -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d '{"pageIndex":1,"pageSize":20,"schedulerCode":"demo.auth.scheduler"}' >"$TMP"
TASK_ID=$(python3 - "$TMP" <<'PY'
import json,sys
d=json.load(open(sys.argv[1]))
rows=(d.get("data") or {}).get("rows") or []
for r in rows:
  if r.get("schedulerCode")=="demo.auth.scheduler":
    print(r.get("id") or ""); break
PY
)
echo "demo.auth.scheduler id=$TASK_ID"
if [ -z "$TASK_ID" ]; then echo "TASK NOT FOUND"; exit 1; fi

# ensure start+select granted
curl -s -X POST "$ROLE/assignObjectActions" \
  -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d "{\"id\":\"$ROLE_ID\",\"objectCode\":\"julyScheduler\",\"actionCodes\":[\"select\",\"start\"]}" >"$TMP"
echo "1) grant select+start: SC=$(sc) $(msg)"

# stop first so start is meaningful
curl -s -X POST "$SCHED/stop" \
  -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d "{\"id\":\"$TASK_ID\"}" >"$TMP" || true

curl -s -o "$TMP" -w "" -X POST "$SCHED/start" \
  -H "Authorization: Bearer $START" -H "Content-Type: application/json" \
  -d "{\"id\":\"$TASK_ID\"}"
echo "2) test017 start (expect 200): SC=$(sc) $(msg)"

# revoke all julyScheduler direct grants
curl -s -X POST "$ROLE/assignObjectActions" \
  -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d "{\"id\":\"$ROLE_ID\",\"objectCode\":\"julyScheduler\",\"actionCodes\":[]}" >"$TMP"
echo "3) revoke all julyScheduler actions: SC=$(sc) $(msg)"

# stop again as admin then try start as test017
curl -s -X POST "$SCHED/stop" \
  -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d "{\"id\":\"$TASK_ID\"}" >"$TMP" || true

curl -s -o "$TMP" -w "" -X POST "$SCHED/start" \
  -H "Authorization: Bearer $START" -H "Content-Type: application/json" \
  -d "{\"id\":\"$TASK_ID\"}"
echo "4) test017 start after revoke (expect 403): SC=$(sc) $(msg)"

# restore start+select for next run
curl -s -X POST "$ROLE/assignObjectActions" \
  -H "Authorization: Bearer $ADMIN" -H "Content-Type: application/json" \
  -d "{\"id\":\"$ROLE_ID\",\"objectCode\":\"julyScheduler\",\"actionCodes\":[\"select\",\"start\"]}" >"$TMP"
echo "5) restore select+start: SC=$(sc) $(msg)"
