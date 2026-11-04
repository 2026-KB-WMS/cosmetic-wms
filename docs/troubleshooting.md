# 트러블슈팅

원인이 직관적이지 않았거나 재현이 어려웠던 문제와 해결 과정을 기록한다. 같은 문제를 다시 만났을 때 처음부터 추적하지 않기 위한 문서다.

새 항목은 **최신순**으로 위에 추가한다. 각 항목은 증상 → 원인 → 해결 → 재발 방지 순서로 쓰고, 확인하지 못한 것은 추측과 구분해서 적는다.

| 날짜 | 항목 |
|---|---|
| 2026-11-03 | [개발자 셸의 HQ_* 환경변수 때문에 부트스트랩 통합 테스트가 실패](#개발자-셸의-hq_-환경변수-때문에-부트스트랩-통합-테스트가-실패) |
| 2026-11-03 | [`./gradlew bootJar -x test`가 copyOasToStatic 검증 오류로 실패](#gradlew-bootjar--x-test가-copyoastostatic-검증-오류로-실패) |
| 2026-11-03 | [Actuator 추가 후 RequestMappingHandlerMapping 주입이 모호해짐](#actuator-추가-후-requestmappinghandlermapping-주입이-모호해짐) |
| 2026-11-03 | [로그인 도입 후 비동기 배정과 재고 이력이 FK 위반으로 실패](#로그인-도입-후-비동기-배정과-재고-이력이-fk-위반으로-실패) |
| 2026-11-03 | [CI에서 의존성 다운로드가 403 Forbidden으로 실패](#ci에서-의존성-다운로드가-403-forbidden으로-실패) |
| 2026-11-03 | [동시 입고 시 DOCKING 용량 갱신 유실 (MySQL REPEATABLE READ)](#동시-입고-시-docking-용량-갱신-유실-mysql-repeatable-read) |
| 2026-11-03 | [Testcontainers가 Docker에 접속하지 못함 (client version 1.32 is too old)](#testcontainers가-docker에-접속하지-못함-client-version-132-is-too-old) |
| 2026-11-03 | [H2의 SET REFERENTIAL_INTEGRITY가 MySQL에서 동작하지 않음](#h2의-set-referential_integrity가-mysql에서-동작하지-않음) |
| 2026-11-03 | [import 검색으로 찾지 못한 도메인 간 의존 위반](#import-검색으로-찾지-못한-도메인-간-의존-위반) |
| 2026-07-04 | [BEFORE_COMMIT 리스너 안에서 발행한 이벤트의 리스너가 실행되지 않음](#before_commit-리스너-안에서-발행한-이벤트의-리스너가-실행되지-않음) |
| 2026-07-04 | [도메인 객체를 수정했는데 DB에 반영되지 않음](#도메인-객체를-수정했는데-db에-반영되지-않음) |

---

## 개발자 셸의 HQ_* 환경변수 때문에 부트스트랩 통합 테스트가 실패

이슈 #147

**증상**
- `scripts/smoke.sh`가 `./gradlew bootJar`(테스트 포함)를 실행하는 중 `HeadquartersBootstrapIntegrationTest` 2건이 실패했다: `NoSuchElementException: No value present`(`hq-boot` 계정을 찾지 못함).
- 같은 테스트를 단독으로 돌리거나 셸에 `HQ_*`가 없을 때는 통과했다.

**원인**
- 스크립트가 `HQ_LOGIN_ID` 등을 `export`한 상태에서 Gradle을 실행했다. `application.yaml`의 `bootstrap.headquarters.*`가 `${HQ_LOGIN_ID:}`처럼 환경변수를 읽으므로, 모든 `@SpringBootTest` 컨텍스트가 시작될 때 본사 계정을 만들었다.
- 부트스트랩은 "본사 관리자가 이미 있으면 건너뛴다". 먼저 뜬 컨텍스트가 계정을 만들어 두어서 이 테스트의 컨텍스트는 계정을 만들지 않았다.
- 스크립트만의 문제가 아니라, 개발자 셸이나 `.env`에 `HQ_*`가 있으면 누구나 겪는 테스트 격리 결함이었다.

**해결**
- `src/test/resources/application.yml`에서 `bootstrap.headquarters.*`를 빈 값으로 지정했다. `HQ_LOGIN_ID`는 `bootstrap.headquarters.login-id`로 직접 매핑되는 환경변수가 아니므로 테스트 설정이 우선하고, 환경변수가 새어 들어오지 않는다.
- `smoke.sh`는 비밀 값을 `export`하기 전에 빌드를 먼저 하도록 순서를 바꿨다.
- 셸에 `HQ_*`를 설정한 채 부트스트랩 테스트를 돌려 통과하는 것을 확인했다.

**재발 방지**
- 환경변수로 켜지는 기능은 테스트 설정에서 값을 비워 둔다 ([testing.md](agents/testing.md)).

---

## `./gradlew bootJar -x test`가 copyOasToStatic 검증 오류로 실패

이슈 #147

**증상**
- Docker 이미지에 담을 jar를 빠르게 만들려고 `./gradlew bootJar -x test`를 실행하면 실패한다.
- `Task ':processResources' uses this output of task ':copyOasToStatic' without declaring an explicit or implicit dependency.`

**원인**
- `copyOasToStatic`이 `src/main/resources/static/docs`(OpenAPI 문서)에 쓰고 `processResources`가 같은 위치를 읽는데 두 태스크 사이에 의존 관계가 없다. `processResources → copyOasToStatic`으로 의존을 걸면 `copyOasToStatic → openapi3 → test → processResources`로 순환한다. 전체 `build`에서는 Gradle이 이를 문제 삼지 않는 실행 순서로 돌아가 드러나지 않았다.
- 해결책으로 문서 복사를 `bootJar`가 jar 안에 직접 넣도록 바꾸는 방법이 있지만, 빌드 구조 변경이라 이번 범위에서 하지 않았다.

**해결(우회)**
- jar는 테스트를 포함한 `./gradlew bootJar`로 만든다 (약 1분, Docker 필요). Dockerfile은 그 jar를 담기만 한다. 이미지 안에서 빌드하지 않는 이유도 같다: 빌드가 Testcontainers 테스트를 실행하는데 컨테이너 안에서는 Docker를 쓸 수 없다.

**재발 방지**
- 빌드 시간을 줄이고 싶어 `-x test`를 붙이지 않는다. 문서 복사 구조를 바꿀 때 이 문제를 함께 해결한다.

---

## Actuator 추가 후 RequestMappingHandlerMapping 주입이 모호해짐

이슈 #147

**증상**
- Actuator 의존성을 추가하자 `AuthorizationMatrixTest`의 모든 케이스(133건)가 실패했다: `expected single matching bean but found 2: requestMappingHandlerMapping, controllerEndpointHandlerMapping`.
- Actuator의 health 테스트만 돌렸을 때는 드러나지 않았다.

**원인**
- Actuator가 `RequestMappingHandlerMapping`의 하위 타입인 `controllerEndpointHandlerMapping` 빈을 추가해, 타입만으로 주입하던 테스트 필드가 모호해졌다.

**해결**
- 주입할 빈을 `@Qualifier("requestMappingHandlerMapping")`로 지정했다.

**재발 방지**
- 의존성을 추가한 뒤에는 변경과 직접 관련된 테스트만이 아니라 전체 테스트를 돌려 확인한다.

---

## 로그인 도입 후 비동기 배정과 재고 이력이 FK 위반으로 실패

이슈 #142

**증상**
- 인증 도입 후 `InboundToInventoryIntegrationTest`의 검사 완료 플로우 4건이 실패했다.
- 로그: `Cannot add or update a child row: a foreign key constraint fails (inventory_transaction, CONSTRAINT fk_inv_tx_member FOREIGN KEY (member_id) REFERENCES member (member_id))`

**원인**
- 재고 이력의 행위자(`inventory_transaction.member_id`, `member` FK)를 `AuditorAware.getCurrentAuditor()`에서 얻고 있었다. 이전 구현이 항상 `1L`을 반환해서 문제가 드러나지 않았다.
- 인증 정보가 없을 때의 감사자를 시스템 값 `0`으로 바꾸자 `member`에 없는 `0`이 들어가 FK가 깨졌다.
- 운영에서도 같은 경로가 있었다. 발주 확정 후 창고 배정은 `@Async` 스레드에서 실행되는데, 이 스레드에는 보안 컨텍스트가 전달되지 않아 행위자가 `0`이 된다. 테스트 한 건만 실패한 게 아니라 운영 흐름이 깨질 뻔했다.

**해결**
- `@Async` 실행기(`eventTaskExecutor`)에 `TaskDecorator`로 `DelegatingSecurityContextRunnable`을 적용해 호출한 스레드의 보안 컨텍스트를 전달했다. 이제 비동기 배정·출고 생성의 행위자도 발주를 확정한 로그인 사용자다.
- 서비스를 직접 호출하는 통합 테스트는 `TestAuthentication.authenticateAs(회원ID)`로 실제 회원이 로그인한 상태에서 실행한다.

**확인 방법**
- `OrderToWarehouseAssignmentIntegrationTest`가 비동기 체인이 만든 모든 재고 이력의 `member_id`가 확정한 사용자 ID인지 검사한다. 컨텍스트 전달 코드를 빼면 이 테스트와 `AsyncConfigSecurityContextTest`가 실패한다.

**재발 방지**
- 새 `@Async` 실행기나 스레드 풀을 만들면 보안 컨텍스트 전달을 함께 설정한다.
- `member`를 FK로 참조하는 컬럼에 행위자를 쓰는 작업은 인증된 사용자가 필요하다. 사용자가 없는 시스템 작업은 시스템 회원을 정해야 한다 ([security.md](agents/security.md#사용자-정보-읽기와-감사-필드)).

---

## CI에서 의존성 다운로드가 403 Forbidden으로 실패

PR #141

**증상**
- CI의 `verify`가 30초대에 실패한다. 테스트까지 가지 못하고 `compileJava`에서 멈춘다.
- 로그: `Could not resolve all files for configuration ':compileClasspath'`, `Could not GET 'https://repo.maven.apache.org/maven2/...'. Received status code 403 from server: Forbidden`

**원인**
- 코드나 설정 문제가 아니라 러너가 Maven Central에서 파일을 받을 때 403을 받았다. 일시적인 차단이나 속도 제한으로 보인다. 확정한 원인은 아니다.
- 근거: 같은 커밋을 재실행하자 통과했다 (`verify` 1분 51초).

**해결**
- 실패한 잡만 재실행한다: `gh run rerun <run-id> --failed`

**재발 방지**
- 로그가 의존성 해석 단계의 403이면 코드를 의심하기 전에 재실행한다.
- 자주 반복되면 Gradle 의존성 캐시 구성이나 미러 사용을 검토한다.

---

## 동시 입고 시 DOCKING 용량 갱신 유실 (MySQL REPEATABLE READ)

이슈 #140, ADR-0005

**증상**
- 입고 50건(각 10개)을 동시에 수령하면 모두 성공으로 응답하는데 `section.current_capacity`가 500이 아니라 50이다.
- 용량 초과 시나리오에서 거절이 한 건도 발생하지 않는다.
- 기존 테스트(H2)는 통과했다. 통합 테스트를 MySQL 컨테이너로 옮기자 `InboundToInventoryIntegrationTest`의 동시성 테스트 2개가 실패했다.

**원인**
- 창고 행에는 `PESSIMISTIC_WRITE`를 걸지만, 같은 트랜잭션이 락 이전에 일반 조회로 읽기 스냅샷을 만든다.
- MySQL 기본 격리 수준(REPEATABLE READ)에서는 락 이후의 일반 조회(`entityManager.refresh`, 하위 `section`의 지연 로딩)가 그 스냅샷 기준의 오래된 값을 돌려준다. 오래된 값으로 계산해 저장하므로 다른 트랜잭션의 갱신을 덮어쓴다.
- H2의 기본 격리 수준에서는 같은 현상이 없어 문제가 가려져 있었다. 운영 설정에도 격리 수준 지정이 없었으므로 운영에서도 발생하는 버그였다.

**확인 방법**
- 설정을 바꾸지 않은 상태에서 동시성 테스트가 실패하는 것을 확인했다.
- 격리 수준만 READ COMMITTED로 바꾸자 같은 테스트가 3회 연속 통과했다.

**해결**
- `application.yaml`에 `spring.datasource.hikari.transaction-isolation: TRANSACTION_READ_COMMITTED`를 설정했다.

**재발 방지**
- DB를 쓰는 테스트는 운영과 같은 DB(Testcontainers MySQL)와 운영 설정으로 실행한다. 테스트에서만 격리 수준을 바꾸지 않는다.
- 락을 거는 코드를 고칠 때는 락 이후의 조회가 최신 값을 읽는지 동시성 테스트로 확인한다.
- 운영 DB에 서버/계정 수준의 격리 수준 지정이 있으면 그것이 우선하므로 배포 전에 확인한다.

---

## Testcontainers가 Docker에 접속하지 못함 (client version 1.32 is too old)

PR #141(이슈 #136)

**증상**
- MySQL 컨테이너를 쓰는 테스트가 전부 `ExceptionInInitializerError`로 실패한다.
- 원인 예외: `BadRequestException: Status 400: {"message":"client version 1.32 is too old. Minimum supported API version is 1.40, please upgrade your client to a newer version"}`

**원인**
- Spring Boot 3.5.0의 BOM이 관리하는 Testcontainers 1.21.0은 docker-java 3.4.2를 쓰고, 이 클라이언트가 Docker API 1.32로 요청한다.
- 설치된 Docker(29.x)는 API 1.40 미만 요청을 거부한다.

**해결**
- `build.gradle`에 `ext['testcontainers.version'] = '1.21.4'`를 지정했다. 1.21.4는 docker-java 버전이 같은 3.4.2로 표시되지만 이 Docker에서 정상 접속됐다.
- 1.21.4로 해결되지 않았다면 docker-java 3.7.0을 쓰는 2.0.x(아티팩트 이름이 `testcontainers-mysql`로 바뀜)가 대안이다.

**재발 방지**
- Testcontainers를 1.21.4 미만으로 내리지 않는다 (`build.gradle` 주석 참고).
- Docker 업데이트 후 같은 오류가 나면 위 메시지의 API 버전 요구를 먼저 확인한다.

---

## H2의 SET REFERENTIAL_INTEGRITY가 MySQL에서 동작하지 않음

PR #141(이슈 #136)

**증상**
- MySQL 컨테이너로 옮긴 뒤 3개 테스트가 `BadSqlGrammarException: SET REFERENTIAL_INTEGRITY FALSE`로 실패한다.

**원인**
- 마스터 데이터(FK 체인)를 세팅하지 않고 재고를 직접 삽입하려고 H2 전용 구문으로 참조 무결성을 껐다. MySQL의 대응 설정은 `SET FOREIGN_KEY_CHECKS = 0`이다.
- 그런데 MySQL의 이 설정은 **연결(세션) 단위**라서, 커넥션 풀에서 매번 다른 연결을 쓰는 테스트에서는 한 번 실행해도 이후 쿼리에 적용되지 않는다. H2의 설정은 데이터베이스 전체에 적용됐다.

**해결**
- 해당 테스트 클래스에 `@TestPropertySource(properties = "spring.datasource.hikari.connection-init-sql=SET FOREIGN_KEY_CHECKS=0")`를 붙여, 그 테스트 컨텍스트의 모든 연결이 시작할 때 설정되게 했다. 다른 테스트에는 영향이 없다.

**재발 방지**
- 가능하면 FK 체인을 실제로 세팅하는 편이 낫다. 불가피할 때만 위 방식을 쓰고, 이유를 주석으로 남긴다.

---

## import 검색으로 찾지 못한 도메인 간 의존 위반

PR #139(이슈 #138)

**증상**
- 도메인 간 의존 위반을 `grep import`로 조사해 `storage → product` 1건이라고 정리했는데, ArchUnit을 도입하자 `inspection → lot` 위반이 하나 더 나왔다.

**원인**
- `inspection`은 `lot.application.port.out.LotPort`를 import했을 뿐이라 import 검색에서는 정상으로 보였다. 하지만 이 포트가 `lot`의 도메인 모델 `Lot`을 반환했고, 어댑터가 `Lot.getId()`를 호출해 도메인 모델이 새어 나갔다. import 문장에는 나타나지 않는다.

**해결**
- `FindLotUseCase`에 조회 메서드를 추가하고 어댑터가 UseCase의 `LotResult`만 쓰게 했다.
- 상대 도메인의 outbound 포트 사용을 아예 금지하도록 ArchUnit 규칙을 `port.in`만 허용으로 좁혔다.

**재발 방지**
- 의존 규칙은 import 검색이 아니라 `ArchitectureTest`(바이트코드 기준)로 검증한다.

---

## BEFORE_COMMIT 리스너 안에서 발행한 이벤트의 리스너가 실행되지 않음

**증상**
- 발주 배정 후 출고 전표를 만드는 체인에서, 이벤트를 발행했는데 다음 단계의 리스너가 호출되지 않는다. 오류도 나지 않는다.

**원인**
- `@TransactionalEventListener(phase = BEFORE_COMMIT)` 리스너 안에서 발행한 이벤트의 BEFORE_COMMIT 리스너는 실행되지 않는다. Spring의 `triggerBeforeCommit`이 동기화 스냅숏을 순회하기 때문이다.

**해결**
- 이 구간에서는 이벤트 대신 out port를 직접 호출한다 (`CreateOutboundFromAssignmentService`).

**재발 방지**
- 이 체인을 이벤트로 일원화하는 리팩토링은 하지 않는다. 상세: [agents/events.md](agents/events.md#금지-before_commit-중첩-발행)

---

## 도메인 객체를 수정했는데 DB에 반영되지 않음

**증상**
- 재고를 분할하는 할당 후 원본 재고의 차감이 DB에 반영되지 않아 재고 수량이 부풀려졌다.

**원인**
- 모든 persistence adapter가 Entity ↔ 도메인 POJO를 detached 방식으로 변환한다. JPA dirty checking이 동작하지 않으므로 도메인 객체를 바꾼 뒤 `port.save()`를 호출하지 않으면 변경이 사라진다.

**해결**
- 변경 후 `save`를 명시 호출하도록 서비스를 수정했다.

**재발 방지**
- 서비스 코드를 쓰거나 리뷰할 때 "변경 후 save 누락"을 확인한다. Mock 서비스 테스트로는 잡히지 않으므로 DB 반영이 중요한 로직은 `*RepositoryTest`나 `*IntegrationTest`로 검증한다. 상세: [agents/conventions.md](agents/conventions.md#영속성)
