#!/usr/bin/env bash
# 컨테이너(MySQL, Redis, 앱)를 실제로 띄워 핵심 흐름을 HTTP로 확인한다. Docker가 필요하다.
#   scripts/smoke.sh                  # jar를 빌드(./gradlew bootJar)하고 실행
#   SMOKE_SKIP_BUILD=1 scripts/smoke.sh   # build/libs의 기존 jar 사용
#   SMOKE_SHOW_LOGS=1 scripts/smoke.sh    # 성공해도 앱 로그를 출력 (SMOKE_LOG_LINES로 줄 수 조정)
# 비밀번호와 키는 실행마다 무작위로 만들고, 끝나면 컨테이너와 볼륨을 지운다.
set -euo pipefail

cd "$(dirname "$0")/.."

if [ "${SMOKE_SKIP_BUILD:-0}" != "1" ]; then
  ./gradlew bootJar
fi

PROJECT=cosmetic-wms-smoke
COMPOSE=(docker compose -p "$PROJECT" --profile app)

free_port() {
  python3 -c 'import socket; s = socket.socket(); s.bind(("", 0)); print(s.getsockname()[1]); s.close()'
}

export DB_PASSWORD="smoke-$(openssl rand -hex 8)"
export JWT_SECRET="$(openssl rand -hex 32)"
export HQ_LOGIN_ID="hq-smoke"
export HQ_PASSWORD="Smoke-$(openssl rand -hex 4)-1!"
export HQ_MEMBER_NAME="스모크본사"
export HQ_EMAIL="hq-smoke@example.com"
export HQ_PHONE_NUMBER="010-0000-0001"
export MYSQL_PORT="$(free_port)"
export REDIS_PORT="$(free_port)"
export APP_PORT="$(free_port)"
BASE="http://localhost:${APP_PORT}"

STATUS=""
BODY=""
FAILED=1

cleanup() {
  if [ "$FAILED" -ne 0 ] || [ "${SMOKE_SHOW_LOGS:-0}" = "1" ]; then
    echo "---- 앱 로그 ----"
    "${COMPOSE[@]}" logs --no-log-prefix app 2>/dev/null | tail -n "${SMOKE_LOG_LINES:-60}" || true
  fi
  "${COMPOSE[@]}" down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

# req METHOD PATH [JSON] [ACCESS_TOKEN] → STATUS, BODY 설정
req() {
  local method="$1" path="$2" data="${3:-}" token="${4:-}"
  local args=(-s -o /tmp/smoke-body.$$ -w '%{http_code}' -X "$method" "${BASE}${path}")
  [ -n "$data" ] && args+=(-H 'Content-Type: application/json' -d "$data")
  [ -n "$token" ] && args+=(-H "Authorization: Bearer ${token}")
  STATUS="$(curl "${args[@]}")"
  BODY="$(cat /tmp/smoke-body.$$)"
  rm -f /tmp/smoke-body.$$
}

json() { python3 -c 'import json,sys; print(json.load(sys.stdin)[sys.argv[1]])' "$1" <<<"$BODY"; }

expect() {
  local label="$1" expected="$2"
  if [ "$STATUS" != "$expected" ]; then
    echo "FAIL  ${label}: 기대 ${expected}, 실제 ${STATUS}, 응답 ${BODY}"
    exit 1
  fi
  echo "ok    ${label} (${STATUS})"
}

echo "== 컨테이너 기동 (MySQL ${MYSQL_PORT}, Redis ${REDIS_PORT}, 앱 ${APP_PORT})"
"${COMPOSE[@]}" up -d --build --wait --wait-timeout 240

echo "== 상태 확인"
req GET /actuator/health/readiness;                                  expect "readiness" 200
req GET /actuator/health;                                            expect "health" 200
req GET /actuator/env;                                               expect "health 외 actuator 차단" 401

echo "== 인증"
req GET /api/v1/products;                                            expect "토큰 없이 보호된 API" 401
req POST /api/v1/auth/login "{\"loginId\":\"${HQ_LOGIN_ID}\",\"password\":\"${HQ_PASSWORD}\"}"
expect "본사 관리자 부트스트랩 계정 로그인" 200
HQ_ACCESS="$(json accessToken)"; HQ_REFRESH="$(json refreshToken)"

req GET /api/v1/products "" "$HQ_ACCESS";                           expect "본사: 상품 조회" 200
req GET /api/v1/admin/failed-assignments "" "$HQ_ACCESS";           expect "본사: 배정 실패 건 조회" 200
[ "$BODY" = "[]" ] || { echo "FAIL  배정 실패 건이 비어 있어야 한다: ${BODY}"; exit 1; }
req GET /api/v1/admin/failed-inspections "" "$HQ_ACCESS";           expect "본사: 검사 실패 건 조회" 200

echo "== 가입과 역할 제한"
req POST /api/v1/members/signup '{"loginId":"smoke-franchise","password":"Smoke-pass1!","role":"ROLE_FRANCHISE_MANAGER","memberName":"스모크점주","email":"franchise-smoke@example.com","phoneNumber":"010-0000-0002"}'
expect "점주 가입" 201
req POST /api/v1/members/signup '{"loginId":"smoke-evil","password":"Smoke-pass1!","role":"ROLE_HEADQUARTERS","memberName":"악의","email":"evil-smoke@example.com","phoneNumber":"010-0000-0003"}'
expect "본사 권한 직접 가입 차단" 422
req POST /api/v1/auth/login '{"loginId":"smoke-franchise","password":"Smoke-pass1!"}'
expect "점주 로그인" 200
FR_ACCESS="$(json accessToken)"
req GET /api/v1/products "" "$FR_ACCESS";                           expect "점주: 상품 조회" 200
req GET /api/v1/admin/failed-assignments "" "$FR_ACCESS";           expect "점주: 관리자 API 거부" 403
req GET /api/v1/inventories/1 "" "$FR_ACCESS";                      expect "점주: 재고 API 거부" 403

echo "== 토큰 재발급과 로그아웃"
req POST /api/v1/auth/reissue "{\"refreshToken\":\"${HQ_REFRESH}\"}"; expect "리프레시 토큰으로 재발급" 200
NEW_REFRESH="$(json refreshToken)"
[ "$NEW_REFRESH" != "$HQ_REFRESH" ] || { echo "FAIL  리프레시 토큰이 회전되지 않았다"; exit 1; }
req POST /api/v1/auth/reissue "{\"refreshToken\":\"${HQ_REFRESH}\"}"; expect "사용한 리프레시 토큰 재사용 거부" 401
req POST /api/v1/auth/logout "{\"refreshToken\":\"${NEW_REFRESH}\"}"; expect "로그아웃" 204
req POST /api/v1/auth/reissue "{\"refreshToken\":\"${NEW_REFRESH}\"}"; expect "로그아웃한 토큰 재발급 거부" 401

FAILED=0
echo "SMOKE OK"
