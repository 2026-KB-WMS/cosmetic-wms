# AGENTS.md

화장품 전용 통합 창고 관리 시스템(WMS). Lot(제조 로트) 단위 생애주기 추적과 FEFO(선유통기한 선출고) 원칙을 시스템적으로 강제하는 것이 핵심 목표다.

단일 모듈 모놀리스다. 멀티 모듈/MSA 분리는 폐기되었으며 다시 도입하지 않는다 ([ADR-0001](docs/adr/0001-single-module-monolith.md)).

- **Stack**: Java 21, Spring Boot 3.5.0, Spring Data JPA, Spring Security, Flyway, MySQL(운영), Testcontainers MySQL(테스트)
- **Build**: Gradle (Groovy DSL)

## 작업 전 읽을 것

작업 성격에 맞는 문서만 읽는다. 전부 읽을 필요는 없다.

| 상황 | 문서 |
|---|---|
| 새 코드를 어느 패키지/계층에 둘지 | [docs/agents/architecture.md](docs/agents/architecture.md) |
| 엔티티, 에러, DTO, 네이밍, 주석 규칙 | [docs/agents/conventions.md](docs/agents/conventions.md) |
| 테스트 작성 | [docs/agents/testing.md](docs/agents/testing.md) |
| 이벤트 추가/수정 | [docs/agents/events.md](docs/agents/events.md) |
| 도메인 용어, 재고 상태, 식별 체계 | [docs/agents/domain-glossary.md](docs/agents/domain-glossary.md) |
| 브랜치, 커밋, PR, 이슈 | [docs/agents/git-workflow.md](docs/agents/git-workflow.md) |
| 설계 결정의 배경 | [docs/adr/](docs/adr/) |
| 이미 겪은 문제, 에러 메시지로 원인 찾기 | [docs/troubleshooting.md](docs/troubleshooting.md) |
| DB 스키마 | [docs/db/v1.5.0_database.md](docs/db/v1.5.0_database.md) (최신 버전) |

문서와 코드가 다르면 **코드가 사실**이다. 불일치를 발견하면 문서를 고치거나 사용자에게 알린다.

## 명령어

```bash
scripts/verify.sh                  # 전체 검증 (clean build + 테스트 + OpenAPI 생성). 완료 선언 전 필수. Docker 필요
./gradlew test                     # 전체 테스트
./gradlew test --tests "com.kb.cosmetic_wms.inventory.InventoryEntityTest"
./gradlew openapi3                 # REST Docs 스니펫 → build/api-spec/openapi3.yaml
DB_PASSWORD=<password> ./gradlew bootRun
```

Swagger UI: `http://localhost:8080/swagger-ui/index.html` (스펙은 `/docs/openapi3.yaml`).

## 완료 기준 (Definition of Done)

1. 변경하거나 추가한 동작에는 그 동작을 검증하는 테스트가 있다.
2. `scripts/verify.sh`가 통과한다. 실패한 상태로 완료를 선언하거나 커밋하지 않는다. CI(`.github/workflows/ci.yml`)가 PR마다 같은 스크립트를 실행하며, 실패한 PR은 머지하지 않는다.
3. 건너뛴 검증이 있으면 사실 그대로 보고한다.
4. 문서와 어긋나게 만든 부분이 있으면 같은 변경에서 문서를 갱신한다.
5. 원인이 직관적이지 않았거나 재현이 어려웠던 문제를 해결했다면 [docs/troubleshooting.md](docs/troubleshooting.md)에 기록한다.

## 반드시 지킬 규칙

아래는 어기면 버그나 구조 붕괴로 이어지는 규칙이다. 상세와 근거는 링크된 문서에 있다.

**아키텍처** ([상세](docs/agents/architecture.md))
- 의존 방향은 `adapter → application → domain` 한 방향이다. `domain`은 Spring/JPA를 import하지 않는다. `application`은 `adapter`를 import하지 않는다.
- 다른 도메인과의 연결은 두 가지뿐이다. (1) `adapter/out`이 상대 도메인의 `application.port.in`을 호출한다. (2) `adapter/in/event`가 상대 도메인의 `domain.event`를 구독한다. 그 외 도메인 간 import는 금지한다. 이 규칙은 `ArchitectureTest`(ArchUnit)가 자동으로 검증한다. 실패하면 규칙을 완화하지 말고 코드를 고친다.
- 다른 도메인은 객체가 아니라 **ID(`Long`)로 참조**한다.
- 컨트롤러에 비즈니스 로직을 두지 않는다. 트랜잭션 경계는 application service에 둔다.

**영속성** ([상세](docs/agents/conventions.md#영속성))
- 도메인 모델은 순수 POJO이고 JPA Entity와 분리되어 있다. 매핑은 detached 방식이라 **dirty checking이 동작하지 않는다.** 도메인 객체를 바꾼 뒤에는 반드시 `port.save()`를 명시 호출한다.
- 스키마 변경은 Flyway 마이그레이션으로만 한다. `ddl-auto`는 `validate`다.

**이벤트** ([상세](docs/agents/events.md))
- `BEFORE_COMMIT` 리스너 안에서 `BEFORE_COMMIT` 리스너가 필요한 이벤트를 발행하지 않는다. 리스너가 조용히 실행되지 않는다. 이 구간에서는 out port를 직접 호출한다.

**보안**
- 비밀번호, API 키 등 비밀 정보를 코드나 설정 기본값에 하드코딩하지 않는다. 환경변수로 주입한다 (`DB_PASSWORD`, `KAKAO_REST_API_KEY`).
- `.env`, `*.pem`, `application-secret.*`는 커밋하지 않는다.

**코드 스타일**
- 주석은 코드로 표현할 수 없는 제약이나 의도만 쓴다. 코드가 하는 일을 반복하는 주석, 시점·계획을 가리키는 주석("나중에 추가", "Step 2에서 이동")은 쓰지 않는다.
- 기존 패턴을 분석한 뒤 그에 맞춘다. 벗어날 때는 이유를 설명한다.
- 요청받지 않은 리팩토링, 추상화, 프레임워크 도입을 하지 않는다.

## Git

- `main`이 기본 브랜치다. 작업 브랜치는 `main`에서 따고 PR도 `main`으로 올린다.
- 브랜치: `feat/[domain]-[feature]`, `fix/[description]`, `refactor/...`, `docs/...`, `chore/...`
- 커밋: `<type>(<scope>): <description>` — 제목은 명령문/명사형, 끝에 마침표 없음, 본문은 `-` 항목. 하나의 커밋은 하나의 논리적 변경만 담는다.
- **모든 작업은 이슈로 시작한다.** 작업 전에 이슈를 작성하고, PR 본문의 연관 이슈에 `- Closes #번호` 형식(앞에 `-`)으로 연결한다. 이슈와 PR에는 **Assignees와 라벨(작업 Type별)을 반드시 지정**한다. 매핑은 [git-workflow.md](docs/agents/git-workflow.md#assignees와-라벨-이슈-pr-공통)에 있다.
- **이슈 체크박스를 작업과 함께 갱신한다.** 항목을 끝내는 대로 `- [x]`로 바꾸고, 범위가 바뀌면 항목을 고치며 사유를 코멘트로 남긴다. 상세는 [git-workflow.md](docs/agents/git-workflow.md#이슈-체크박스-관리).
- 커밋, 이슈, PR 어디에도 Claude-Session 트레일러/링크를 넣지 않는다.
- 사용자가 요청하기 전에는 커밋하거나 push하지 않는다. `main`에 직접 커밋하지 않는다. force push는 사용자가 명시적으로 요청한 경우에만 한다.

상세: [docs/agents/git-workflow.md](docs/agents/git-workflow.md)

## 작업 상태 추적

여러 세션에 걸치는 작업은 아래 두 파일로 이어간다.

- `feature_list.json`: 작업 목록 (`id`, `name`, `description`, `status`, `dependencies`)
- `PROGRESS.md`: 현재 상태, 날짜별 세션 로그(최근 3개 날짜만 유지, 나머지는 `docs/progress/archive.md`), 다음 세션에서 할 일

세션 시작 시 `AGENTS.md` → `docs/` → `feature_list.json` → `PROGRESS.md` 순으로 읽는다. 작업 하나를 골라 `in_progress`로 바꾸고, 구현·검증 후 `completed`로 바꾸고 `PROGRESS.md`에 기록한다.
