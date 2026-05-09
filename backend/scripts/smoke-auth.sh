#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
BACKEND_DIR="$(cd "$(dirname "$0")/.." && pwd)"
TMP_DIR="$(mktemp -d)"
GREEN='\033[0;32m'
NC='\033[0m'

cleanup() {
  if [[ -n "${APP_PID:-}" ]] && kill -0 "$APP_PID" 2>/dev/null; then
    kill "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
  fi
  rm -rf "$TMP_DIR"
}
trap cleanup EXIT

EMAIL="testuser_$(date +%s)@example.com"
PASSWORD="strongPassword123"
BAD_PASSWORD="wrongPassword123"

request() {
  local method="$1"
  local path="$2"
  local body="${3:-}"
  local auth="${4:-}"

  if [[ -n "$auth" && -n "$body" ]]; then
    curl -s -o "$TMP_DIR/response.json" -w "%{http_code}" \
      -X "$method" \
      -H "Content-Type: application/json" \
      -H "Authorization: Bearer $auth" \
      "$BASE_URL$path" \
      -d "$body"
    return
  fi

  if [[ -n "$auth" ]]; then
    curl -s -o "$TMP_DIR/response.json" -w "%{http_code}" \
      -X "$method" \
      -H "Authorization: Bearer $auth" \
      "$BASE_URL$path"
    return
  fi

  if [[ -n "$body" ]]; then
    curl -s -o "$TMP_DIR/response.json" -w "%{http_code}" \
      -X "$method" \
      -H "Content-Type: application/json" \
      "$BASE_URL$path" \
      -d "$body"
    return
  fi

  curl -s -o "$TMP_DIR/response.json" -w "%{http_code}" \
    -X "$method" \
    "$BASE_URL$path"
}

assert_status() {
  local actual="$1"
  local expected="$2"
  local label="$3"

  if [[ "$actual" != "$expected" ]]; then
    echo "FAIL: $label"
    echo "Expected status: $expected"
    echo "Actual status:   $actual"
    echo "Response body:"
    cat "$TMP_DIR/response.json"
    echo
    exit 1
  fi

  printf "${GREEN}PASS: %s${NC}\n\n" "$label"
}

echo "Starting backend..."
(
  cd "$BACKEND_DIR"
  ./mvnw spring-boot:run >"$TMP_DIR/backend.log" 2>&1
) &
APP_PID=$!

echo "Waiting for backend..."
READY=0
for _ in {1..60}; do
  if curl -s "$BASE_URL/auth/login" >/dev/null 2>&1; then
    READY=1
    break
  fi
  sleep 1
done

if [[ "$READY" -ne 1 ]]; then
  echo "Backend did not become ready in time."
  echo "Backend log:"
  cat "$TMP_DIR/backend.log"
  exit 1
fi

SIGNUP_PAYLOAD=$(cat <<JSON
{"email":"$EMAIL","password":"$PASSWORD"}
JSON
)

BAD_LOGIN_PAYLOAD=$(cat <<JSON
{"email":"$EMAIL","password":"$BAD_PASSWORD"}
JSON
)

LOGIN_PAYLOAD=$(cat <<JSON
{"email":"$EMAIL","password":"$PASSWORD"}
JSON
)

echo "Testing signup..."
status=$(request POST /auth/signup "$SIGNUP_PAYLOAD")
assert_status "$status" "200" "signup succeeds"

echo "Testing duplicate signup..."
status=$(request POST /auth/signup "$SIGNUP_PAYLOAD")
assert_status "$status" "409" "duplicate signup rejected"

echo "Testing wrong-password login..."
status=$(request POST /auth/login "$BAD_LOGIN_PAYLOAD")
assert_status "$status" "401" "wrong password rejected"

echo "Testing login..."
status=$(request POST /auth/login "$LOGIN_PAYLOAD")
assert_status "$status" "200" "login succeeds"

TOKEN=$(sed -n 's/.*"token"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p' "$TMP_DIR/response.json")
if [[ -z "$TOKEN" ]]; then
  echo "FAIL: login response missing token"
  cat "$TMP_DIR/response.json"
  exit 1
fi
printf "${GREEN}PASS: %s${NC}\n\n" "token extracted"

echo "Testing /auth/me without token..."
status=$(request GET /auth/me)
assert_status "$status" "403" "me blocked without token"

echo "Testing /auth/me with token..."
status=$(request GET /auth/me "" "$TOKEN")
assert_status "$status" "200" "me succeeds with token"

echo "Testing malformed token..."
status=$(request GET /auth/me "" "abc.def.ghi")
assert_status "$status" "401" "malformed token rejected"

echo "Testing logout..."
status=$(request POST /auth/logout "" "$TOKEN")
assert_status "$status" "200" "logout succeeds"

echo "All auth smoke tests passed."
