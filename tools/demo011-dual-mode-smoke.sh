#!/usr/bin/env bash
# G9a - java17-demo011-app011 dual-mode smoke (WSL; jar must already be up on BASE)
#
#   MODE=dev  bash tools/demo011-dual-mode-smoke.sh   # development / debug
#   MODE=prod bash tools/demo011-dual-mode-smoke.sh   # production profile
#
# Expect EXIT=0. Both modes must pass for G9a (restart jar between MODE runs).
# Account: demo011 / Test011! - Port default 11161
set -euo pipefail

BASE="${BASE:-http://127.0.0.1:11161}"
MODE="${MODE:-}"
PASS="${PASS:-Test011!}"
ACCT="${ACCT:-demo011}"
TMP="$(mktemp)"
trap 'rm -f "$TMP"' EXIT
FAIL=0

die() { echo "FAIL: $*" >&2; FAIL=1; }
ok()  { echo "OK: $*"; }

need_mode() {
  if [ "$MODE" != "dev" ] && [ "$MODE" != "prod" ]; then
    echo "Usage: MODE=dev|prod [BASE=http://127.0.0.1:11161] $0" >&2
    exit 2
  fi
}

# POST/GET -> write body to TMP; print HTTP code on stdout
http() {
  local meth="$1" path="$2" body="${3:-}" auth="${4:-}"
  local args=(-s -o "$TMP" -w "%{http_code}")
  [ -n "$auth" ] && args+=(-H "Authorization: Bearer ${auth}")
  if [ "$meth" = GET ]; then
    curl "${args[@]}" "$path"
  else
    args+=(-X POST -H "Content-Type: application/json" -d "$body")
    curl "${args[@]}" "$path"
  fi
}

json_sc() {
  python3 -c "import json; print(json.load(open('$TMP')).get('statusCode','?'))" 2>/dev/null || echo "?"
}

json_token() {
  python3 -c "import json; print((json.load(open('$TMP')).get('data') or {}).get('token') or '')" 2>/dev/null || echo ""
}

json_id() {
  python3 -c "import json; print((json.load(open('$TMP')).get('data') or {}).get('id') or '')" 2>/dev/null || echo ""
}

json_field() {
  local key="$1"
  python3 -c "import json; d=json.load(open('$TMP')).get('data') or {}; print(d.get('$key') or '')" 2>/dev/null || echo ""
}

expect_http() {
  local got="$1" want="$2" label="$3"
  if [ "$got" = "$want" ]; then
    ok "$label HTTP=$got"
  else
    die "$label HTTP=$got want=$want body=$(head -c 200 "$TMP")"
  fi
}

expect_sc() {
  local got want="$1" label="$2"
  got="$(json_sc)"
  if [ "$got" = "$want" ]; then
    ok "$label statusCode=$got"
  else
    die "$label statusCode=$got want=$want body=$(head -c 200 "$TMP")"
  fi
}

crud_demo011() {
  local tok="$1" prefix="$2"
  local dcode="g9${prefix}$(date +%s)"
  local http_code id name

  http_code=$(http POST "$BASE/demo011/demo/v1/insert" \
    "{\"code\":\"$dcode\",\"name\":\"g9-${prefix}-v1\"}" "$tok")
  expect_http "$http_code" "200" "demo insert"
  expect_sc 200 "demo insert"
  id="$(json_id)"
  [ -n "$id" ] || die "demo insert missing id"

  http_code=$(http GET "$BASE/demo011/demo/v1/getById?id=$id" "" "$tok")
  expect_http "$http_code" "200" "demo getById"
  name="$(json_field name)"
  [ "$name" = "g9-${prefix}-v1" ] || die "demo getById name='$name'"

  http_code=$(http POST "$BASE/demo011/demo/v1/update" \
    "{\"id\":\"$id\",\"name\":\"g9-${prefix}-v2\"}" "$tok")
  expect_http "$http_code" "200" "demo update"
  expect_sc 200 "demo update"

  http_code=$(http GET "$BASE/demo011/demo/v1/getById?id=$id" "" "$tok")
  name="$(json_field name)"
  [ "$name" = "g9-${prefix}-v2" ] || die "demo re-get name='$name'"

  http_code=$(http POST "$BASE/demo011/demo/v1/logicDelete" "{\"id\":\"$id\"}" "$tok")
  expect_http "$http_code" "200" "demo logicDelete"
  expect_sc 200 "demo logicDelete"

  http_code=$(http GET "$BASE/demo011/demo/v1/getById?id=$id" "" "$tok")
  if [ "$http_code" != "404" ]; then
    die "demo post-delete getById HTTP=$http_code want=404"
  else
    ok "demo post-delete getById HTTP=404"
  fi
}

crud_user() {
  local tok="$1"
  local ucode="u$(date +%s)"
  local http_code id uname

  http_code=$(http POST "$BASE/klsjnh/iam/julyUser/v1/insert" \
    "{\"userAccount\":\"$ucode\",\"userName\":\"G9Smoke\",\"password\":\"$PASS\"}" "$tok")
  expect_http "$http_code" "200" "user insert"
  expect_sc 200 "user insert"
  id="$(json_id)"
  [ -n "$id" ] || die "user insert missing id"

  http_code=$(http GET "$BASE/klsjnh/iam/julyUser/v1/getById?id=$id" "" "$tok")
  expect_http "$http_code" "200" "user getById"
  uname="$(json_field userName)"
  [ "$uname" = "G9Smoke" ] || die "user getById userName='$uname'"

  http_code=$(http POST "$BASE/klsjnh/iam/julyUser/v1/update" \
    "{\"id\":\"$id\",\"userName\":\"G9Smoke2\"}" "$tok")
  expect_http "$http_code" "200" "user update"
  expect_sc 200 "user update"

  http_code=$(http POST "$BASE/klsjnh/iam/julyUser/v1/logicDelete" "{\"id\":\"$id\"}" "$tok")
  expect_http "$http_code" "200" "user logicDelete"
  expect_sc 200 "user logicDelete"
}

run_dev() {
  echo "=== G9a MODE=dev @ $BASE ==="
  local http_code tok

  http_code=$(http POST "$BASE/klsjnh/iam/julyUser/v1/loginByUserName" \
    "{\"userAccount\":\"$ACCT\"}")
  expect_http "$http_code" "200" "loginByUserName"
  expect_sc 200 "loginByUserName"
  tok="$(json_token)"
  [ -n "$tok" ] || die "dev login missing token"

  crud_user "$tok"
  crud_demo011 "$tok" "d"
}

run_prod() {
  echo "=== G9a MODE=prod @ $BASE ==="
  local http_code tok

  http_code=$(http POST "$BASE/klsjnh/iam/julyUser/v1/loginByUserName" \
    "{\"userAccount\":\"$ACCT\"}")
  expect_http "$http_code" "401" "loginByUserName(no-password must 401)"

  http_code=$(http POST "$BASE/klsjnh/iam/julyUser/v1/login" \
    "{\"userAccount\":\"$ACCT\",\"password\":\"$PASS\"}")
  expect_http "$http_code" "200" "login(password)"
  expect_sc 200 "login(password)"
  tok="$(json_token)"
  [ -n "$tok" ] || die "prod login missing token"

  http_code=$(http POST "$BASE/demo011/demo/v1/insert" \
    "{\"code\":\"nt$(date +%s)\",\"name\":\"no-token\"}")
  if [ "$http_code" = "401" ] || [ "$http_code" = "403" ]; then
    ok "demo insert without token HTTP=$http_code"
  else
    die "demo insert without token HTTP=$http_code want=401/403"
  fi

  crud_demo011 "$tok" "p"
}

need_mode
case "$MODE" in
  dev)  run_dev ;;
  prod) run_prod ;;
esac

if [ "$FAIL" -ne 0 ]; then
  echo "=== G9a MODE=$MODE FAILED ===" >&2
  exit 1
fi
echo "=== G9a MODE=$MODE PASSED ==="
exit 0