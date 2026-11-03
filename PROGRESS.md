# PROGRESS

## 현재 상태

- 단일 모듈 모놀리스. `main`이 기본 브랜치이며 마지막 안정 빌드는 테스트 720개 전체 통과.
- MSA 분리는 폐기하고 `5a48431`로 복원 완료 ([ADR-0001](docs/adr/0001-single-module-monolith.md)).
- 에이전트 하네스 문서 체계 도입 완료 (PR #128, F-001). CI 도입 완료 (PR #130, F-003).
- 열린 이슈: #136 (Testcontainers 전환, 진행 중), #140 (격리 수준 버그, 진행 중).

## 세션 로그

### 2026-11-03

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

1. F-008 마무리: PR 머지. 배포 전 운영 DB의 격리 수준 설정이 이미 지정돼 있는지 확인(ADR-0005).
2. F-006: API 인증/인가 체계 도입. 설계 결정 필요(방식, 엔드포인트별 역할).
