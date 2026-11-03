# PROGRESS

## 현재 상태

- 단일 모듈 모놀리스. `main`이 기본 브랜치이며 마지막 안정 빌드는 테스트 720개 전체 통과.
- MSA 분리는 폐기하고 `5a48431`로 복원 완료 ([ADR-0001](docs/adr/0001-single-module-monolith.md)).
- 에이전트 하네스 문서 체계 도입 완료 (PR #128, F-001). CI 도입 완료 (PR #130, F-003).
- 열린 이슈: #144 (실패한 이벤트 수동 재처리 API, 진행 중).

## 세션 로그

### 2026-11-03

- 이슈 #144(F-009): 창고 배정·검사 전표 생성 실패 건을 본사 관리자가 조회·재처리·폐기하는 API(`/api/v1/admin/failed-assignments`, `/failed-inspections`). Flyway V3로 `status`/`retry_count`/`resolved_at` 추가, 실패 기록 행 잠금으로 동시 재처리 직렬화, 별도 트랜잭션 실행기로 실패해도 기록 보존, 멱등 보장(배정은 CONFIRMED·미배정일 때만, 전표는 중복 생성 안 함). DB 설계서 v1.6.0.
- PR #143 머지: JWT 인증과 역할별 접근 제어 (#142, F-006).
- 이슈 #142(F-006): JWT(Access/Refresh) 인증 + Redis 리프레시 토큰 + 역할별 접근 제어 구현. `auth` 도메인(로그인·재발급·로그아웃), `SecurityConfig` 역할 규칙(기본 거부), `AuthorizationMatrixTest`(컨트롤러 매핑과 정확히 일치 검사), 감사자를 로그인 사용자로 변경, `@Async` 보안 컨텍스트 전달. 기존 `POST /members/login`은 `/auth/login`으로 대체(ADR-0006).
- PR #141 머지: Testcontainers 전환과 격리 수준 수정 (#136, #140, F-008).
- 이슈 #136(F-008): DB 테스트 7개를 Testcontainers MySQL 8.0으로 전환하고 H2 제거. `MySqlContainerSupport`(JVM당 컨테이너 1개), `verify.sh`가 스킵 발생 시 실패. Testcontainers는 Docker 29 호환을 위해 1.21.4로 고정.
- 전환 중 이슈 #140 발견: MySQL 기본 격리 수준(REPEATABLE READ)에서 동시 입고 시 DOCKING 용량 갱신 유실. 격리 수준을 READ COMMITTED로 설정해 해결(ADR-0005). H2가 가려주던 운영 버그.
- PR #139 머지: ArchUnit 아키텍처 규칙 (#138, F-002).
- 이슈 #138(F-002): ArchUnit `ArchitectureTest` 도입(규칙 5개). 도입 중 발견한 위반 2건 수정: `storage`→`product`(outbound 포트·도메인 enum 직접 사용 → `FindProductUseCase`의 온도 코드 조회), `inspection`→`lot`(outbound 포트·도메인 모델 직접 사용 → `FindLotUseCase`). 테스트 27개 추가(총 776개).
- PR #137 머지: 비밀번호 규칙 도메인화 (#126, F-005).
- 이슈 #126(F-005): `RawPassword` 값 객체 도입. 가입과 본사 관리자 부트스트랩이 인코딩 전에 도메인에서 비밀번호 규칙을 검증하고, 웹 DTO는 규칙 상수를 참조. 테스트 16개 추가(총 749개).
- PR #135 머지: 본사 관리자 부트스트랩 (#134, F-007). 이슈 #136(F-008, Testcontainers 전환) 생성.
- 이슈 #134(F-007): 환경변수(`HQ_*`)로 최초 `ROLE_HEADQUARTERS` 계정을 기동 시 한 번 생성하는 부트스트랩 구현 (`HeadquartersBootstrapRunner`, `HeadquartersBootstrapService`). 테스트 8개 추가(총 733개).
- PR #133 머지: `ROLE_HEADQUARTERS` 직접 가입 차단 (#132, F-004).
- 이슈 #132(F-004)로 `ROLE_HEADQUARTERS` 직접 가입 차단: `Role.isSelfRegistrable()` 추가, 가입 유스케이스에서 `RoleNotAllowedForSignUpException`(422) 발생. 테스트 5개 추가(총 725개).
- 이슈 #129를 만들고 `.github/workflows/ci.yml` 추가 (PR마다 `scripts/verify.sh` 실행).
- 하네스 PR #128 머지, 이슈 선행 작성과 Assignees·라벨 규칙을 `docs/agents/git-workflow.md`에 추가.

- 멀티 모듈 상태를 분석하고 `5a48431`로 복원. `develop`/`main`을 되돌려 force push했고 `develop`과 옛 브랜치 43개를 삭제.
- DB 접속 정보를 환경변수로 전환(`DB_PASSWORD` 필수), 불필요한 `package.json` 제거 (PR #127 머지).
- 이슈 #109, #110, #114를 닫고 #126을 모놀리스 기준으로 재작성.
- `AGENTS.md`, `CLAUDE.md`(`@AGENTS.md` import), `docs/agents/`, `docs/adr/`, `scripts/verify.sh`, `feature_list.json`, `PROGRESS.md` 작성. 개인 선호는 gitignore된 `CLAUDE.local.md`로 이동.

## 다음 세션에서 할 일

1. F-009 마무리: PR 머지.
2. F-011: 창고 배정 실패 발주의 처리 정책 결정. `CONFIRMED` 발주는 취소할 수 없어 배정이 계속 실패하면 막다른 상태다.
3. F-010: 실패 건 자동 재시도 (수동 API가 쌓인 뒤 필요성을 판단).
4. 운영 준비: Actuator health, Dockerfile, 로컬 compose(MySQL + Redis), JaCoCo.
5. 조회 API 페이징과 인덱스 점검.
