# 운영과 구동

앱을 띄우고, 상태를 확인하고, 문제를 추적하는 방법. 인증 설정은 [security.md](security.md), 실패한 이벤트 복구는 [events.md](events.md)를 참고한다.

## 구성

| 구성 요소 | 용도 | 비고 |
|---|---|---|
| 앱 (Spring Boot, 8080) | API | 단일 모듈. `./gradlew bootJar`로 만든 jar를 `Dockerfile`이 담는다 |
| MySQL 8.0 | 업무 데이터 | Flyway가 기동 시 마이그레이션 적용, 격리 수준 READ COMMITTED ([ADR-0005](../adr/0005-transaction-isolation-read-committed.md)) |
| Redis 7.4 | 리프레시 토큰 | 장애 시 로그인·재발급이 실패한다. 발급된 액세스 토큰으로 하는 API 호출은 영향이 없다 |

## 환경변수

| 환경변수 | 필수 | 기본값 | 설명 |
|---|:-:|---|---|
| `DB_PASSWORD` | O | - | MySQL 비밀번호 |
| `DB_USERNAME` | | `root` | |
| `DB_URL` | | 로컬 MySQL | JDBC URL |
| `JWT_SECRET` | O | - | 32바이트 이상. 없거나 짧으면 기동하지 않는다 |
| `JWT_ACCESS_TOKEN_TTL` | | `30m` | |
| `JWT_REFRESH_TOKEN_TTL` | | `14d` | |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | | `localhost` / `6379` / 없음 | |
| `HQ_LOGIN_ID`, `HQ_PASSWORD`, `HQ_MEMBER_NAME`, `HQ_EMAIL`, `HQ_PHONE_NUMBER` | | 없음 | 5개를 모두 지정하면 본사 관리자가 없을 때 한 번 생성. 일부만 지정하면 기동 실패 |
| `INVENTORY_SHELF_LIFE_MIN_REMAINING_DAYS` | | `7` | 잔여 유통기한이 이 일수 이하인 재고는 출고하지 않는다 ([ADR-0007](../adr/0007-minimum-remaining-shelf-life.md)) |
| `KAKAO_REST_API_KEY` | | 없음 | 지오코딩·라우팅. 없으면 창고·가맹점 등록이 502로 실패한다 |

비밀 값(`DB_PASSWORD`, `JWT_SECRET`, `HQ_PASSWORD`, `REDIS_PASSWORD`, `KAKAO_REST_API_KEY`)은 코드나 설정 기본값에 넣지 않는다. 로컬은 `.env`(gitignore 대상)에, 운영은 비밀 관리 서비스에서 주입한다. `.env.example`은 값을 비워 둔 템플릿이다.

## 로컬에서 실행

```bash
cp .env.example .env && $EDITOR .env
docker compose up -d                       # MySQL, Redis
set -a; source .env; set +a; ./gradlew bootRun
```

| 명령 | 설명 |
|---|---|
| `docker compose up -d` | MySQL, Redis만 (앱은 IDE나 `bootRun`으로) |
| `./gradlew bootJar && docker compose --profile app up -d --build` | 앱까지 컨테이너로 |
| `docker compose down -v` | 중지하고 데이터 삭제 |

호스트 포트가 겹치면 `.env`에서 `MYSQL_PORT`, `REDIS_PORT`, `APP_PORT`를 바꾼다.

jar는 반드시 테스트를 포함한 `./gradlew bootJar`로 만든다. `-x test`를 붙이면 실패한다 ([troubleshooting](../troubleshooting.md)).

## 상태 확인

| 경로 | 인증 | 용도 |
|---|---|---|
| `/actuator/health` | 공개 | 종합 상태 (`UP`/`DOWN`만, 세부 정보 없음) |
| `/actuator/health/liveness` | 공개 | 프로세스가 살아 있는가 |
| `/actuator/health/readiness` | 공개 | 요청을 받을 준비가 되었는가 (DB, Redis 포함) |

그 외 actuator 경로는 노출하지 않으며 인증 여부와 무관하게 거부한다.

## 실제 구동 검증 (smoke)

```bash
scripts/smoke.sh                      # jar를 빌드하고 컨테이너로 띄워 검증
SMOKE_SKIP_BUILD=1 scripts/smoke.sh   # 기존 jar 사용
SMOKE_SHOW_LOGS=1 scripts/smoke.sh    # 성공해도 앱 로그 출력
```

실행마다 무작위 비밀 값과 빈 포트를 쓰고, 끝나면 컨테이너와 볼륨을 지운다. 확인하는 것: 빈 MySQL에 Flyway 마이그레이션 적용, 본사 관리자 부트스트랩, 로그인, 역할 제한(점주가 관리자 API에 접근 불가, 본사 권한 직접 가입 차단), 리프레시 토큰 회전과 재사용 거부, 로그아웃, health.

배포 설정이나 기동 경로를 바꾸는 변경은 이 스크립트가 통과하는지 확인한다. 이 스크립트가 쓰는 것과 같은 방식으로 앱이 뜨므로, 테스트 컨텍스트로는 잡히지 않는 구동 문제(환경변수, 이미지, compose)를 잡는다.

## 로그

- 레벨: 정상 흐름은 `INFO`, 사람이 확인해야 하지만 요청은 처리된 경우는 `WARN`, 처리되지 않은 예외는 Tomcat이 `ERROR`로 스택 트레이스와 함께 남긴다.
- 업무상 확인이 필요한 지점은 `WARN`으로 남는다. 아래 문구로 검색한다.

| 로그 문구 | 의미 | 조치 |
|---|---|---|
| `창고 배정 실패를 기록했습니다` | 발주 확정 후 배정이 재시도 후에도 실패 | `/api/v1/admin/failed-assignments`에서 조회·재처리 |
| `검사 전표 생성 실패를 기록했습니다` | 입고 완료 후 전표 생성이 재시도 후에도 실패 | `/api/v1/admin/failed-inspections`에서 조회·재처리 |
| `재처리에 실패했습니다` | 관리자가 재처리했으나 다시 실패 (사유 포함) | 원인 해소 후 재시도, 불필요하면 폐기 |
| `로그인에 실패했습니다` | 인증 실패. 존재하지 않는 ID인지 비밀번호 불일치(`memberId`)인지 구분 | 반복되면 계정 점검 |
| `유효하지 않은 리프레시 토큰으로 재발급이 시도되었습니다` | 만료·폐기·재사용된 토큰 | 반복되면 토큰 탈취 가능성 점검 |
| `접근이 거부되었습니다` | 역할이 부족한 호출 (`memberId`, 메서드, 경로) | 권한 규칙 점검 |

- 로그에 **비밀번호, 토큰, 사용자가 입력한 로그인 ID 같은 원문을 남기지 않는다.** 입력 원문은 줄바꿈으로 가짜 로그 줄을 만들 수 있다 (log injection). 식별이 필요하면 서버가 아는 `memberId`를 쓴다. 로그 테스트(`LogCapture`)가 이를 검증한다.

## 테스트 커버리지

`scripts/verify.sh`가 끝에 라인·브랜치 커버리지를 출력하고, 리포트는 `build/reports/jacoco/test/html/index.html`에 생긴다. CI는 같은 리포트를 `coverage-report` 아티팩트로 올린다. 아직 임계값은 없다 (측정만).

## 출고 작업 흐름

발주 확정 후 창고 배정이 끝나면 출고 전표가 `ALLOCATED`로 만들어진다. 이후 작업은 모두 본사·창고 관리자 API로 한다.

1. `PATCH /api/v1/outbounds/{id}/process` — 출고 준비 시작(`PROCESSING`)
2. `GET /api/v1/outbounds/{id}` — 품목 목록에서 `outboundItemId`와 `targetQuantity` 확인
3. `PATCH /api/v1/outbounds/{id}/items/{outboundItemId}/picking` 본문 `{"pickedQuantity": n}` — 실제 피킹 수량 기록. **누적이 아니라 절대값 설정**이라 같은 요청을 다시 보내도 결과가 같다. 0 이상, 지시 수량 이하여야 하고 `PROCESSING`에서만 가능하다.
4. `PATCH /api/v1/outbounds/{id}/ship` — 모든 품목이 지시 수량만큼 피킹돼야 한다. 출하되면 할당분이 재고에서 차감되고 발주가 `SHIPPED`가 된다.

피킹이 지시 수량에 못 미치면 출하할 수 없고(409), `PROCESSING` 출고는 취소 API도 없다. 이 막다른 상태를 푸는 것은 F-018이다.

## 리콜·보류 대응 절차

로트에 문제가 생겼을 때의 순서. 시스템은 새 출고를 막아 주고, 이미 할당된 출고는 사람이 확인해 처리한다.

1. **로트 상태 변경** — `PATCH /api/v1/lots/{lotId}/status` (`RECALLED`, `HOLD`, `DAMAGED`, `EXPIRED`). 변경과 함께 그 로트의 재고가 FEFO 후보, 창고 배정 집계, 수동 할당에서 즉시 제외된다 ([ADR-0008](../adr/0008-inventory-lot-block.md)).
2. **영향받는 출고 확인** — `GET /api/v1/outbounds?lotId={lotId}` (본사·창고 관리자). 이 로트의 재고가 이미 할당된 `ALLOCATED`, `PROCESSING` 출고 전표 목록이 나온다.
3. **출고 처리**
   - `ALLOCATED`: `PATCH /api/v1/outbounds/{id}/cancel`로 취소하면 할당이 풀린다. 풀린 재고는 로트가 차단되어 있으므로 다시 할당되지 않는다. 발주는 창고 배정이 이미 끝난 상태라 다시 배정하려면 별도 조치가 필요하다.
   - `PROCESSING`(피킹 중): **취소 API가 없다**(취소는 `PENDING`, `ALLOCATED`만 가능). 현장에서 피킹을 멈추게 하고 별도로 처리해야 한다. 아직 시스템에 이 단계를 처리하는 방법이 없다.
   - 이미 `SHIPPED`된 출고는 이 조회에 나오지 않는다. 출하된 물량의 회수는 이 시스템 범위 밖이다.
4. **확인** — 같은 조회가 빈 목록이면 이 로트의 할당 재고에 영향받는 출고가 남아 있지 않다.

조회는 읽기 전용이며 자동으로 취소하거나 재고를 바꾸지 않는다. 같은 로트·구역의 할당은 재고 행이 하나로 병합되므로, 어떤 출고인지는 재고 행이 아니라 `inventory_transaction`의 `ALLOCATE` 이력(`reference_id`=출고 ID)으로 찾고, 출하·취소된 출고는 출고 쪽 상태로 걸러낸다.

## 로트 차단 목록 점검

로트 상태와 `inventory_lot_block`이 어긋나지 않았는지 확인한다. 아무 행도 나오지 않아야 정상이다. 행이 나오면 로트 상태를 `updateStatus`를 거치지 않고 바꾼 경로가 있다는 뜻이다 ([ADR-0008](../adr/0008-inventory-lot-block.md)).

```sql
SELECT l.lot_id, l.status AS lot_status, b.status AS block_status
FROM lot l
LEFT JOIN inventory_lot_block b ON b.lot_id = l.lot_id
WHERE (l.status <> 'AVAILABLE') <> (b.lot_id IS NOT NULL)
   OR (b.lot_id IS NOT NULL AND b.status <> l.status);
```

## 배포 전 확인

- `JWT_SECRET`을 무작위로 만들었고(`openssl rand -hex 32`) 비밀 관리 서비스에서 주입한다.
- Redis가 준비되어 있다.
- 운영 DB에 서버·계정 수준으로 지정된 트랜잭션 격리 수준이 있다면 그것이 앱 설정보다 우선한다 ([ADR-0005](../adr/0005-transaction-isolation-read-committed.md)).
- V3 마이그레이션 이후 기존 실패 기록이 모두 `PENDING`으로 보인다. 이미 수동 처리한 건은 `discard`로 정리한다.
- 최초 본사 관리자를 만들려면 `HQ_*` 5개를 지정해 한 번 기동한 뒤 제거해도 된다.

## 아직 없는 것

`PROCESSING` 출고의 취소(F-018), 유통기한이 지난 로트의 자동 만료 처리, 출고 불가가 된 임박 재고의 처리 흐름(폐기·할인·반품).


메트릭 수집(Prometheus), 분산 추적, 요청 상관 ID(MDC), 알림, 배포 파이프라인.
