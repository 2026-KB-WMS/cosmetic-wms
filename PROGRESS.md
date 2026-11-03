# PROGRESS

## 현재 상태

- 단일 모듈 모놀리스. `main`이 기본 브랜치이며 마지막 안정 빌드는 테스트 720개 전체 통과.
- MSA 분리는 폐기하고 `5a48431`로 복원 완료 ([ADR-0001](docs/adr/0001-single-module-monolith.md)).
- 에이전트 하네스 문서 체계를 도입하는 중 (`docs/agent-harness` 브랜치, F-001).
- 열린 이슈: #126 (회원 가입 권한 제한, 비밀번호 규칙 도메인화).

## 세션 로그

### 2026-11-03

- 멀티 모듈 상태를 분석하고 `5a48431`로 복원. `develop`/`main`을 되돌려 force push했고 `develop`과 옛 브랜치 43개를 삭제.
- DB 접속 정보를 환경변수로 전환(`DB_PASSWORD` 필수), 불필요한 `package.json` 제거 (PR #127 머지).
- 이슈 #109, #110, #114를 닫고 #126을 모놀리스 기준으로 재작성.
- `AGENTS.md`, `CLAUDE.md`(`@AGENTS.md` import), `docs/agents/`, `docs/adr/`, `scripts/verify.sh`, `feature_list.json`, `PROGRESS.md` 작성. 개인 선호는 gitignore된 `CLAUDE.local.md`로 이동.

## 다음 세션에서 할 일

1. F-001 마무리: 하네스 문서 PR 머지.
2. F-004: 가입 시 `ROLE_HEADQUARTERS` 직접 가입 차단 (보안 문제, 우선).
3. F-002: ArchUnit 아키텍처 테스트 도입. 먼저 `storage.adapter.out`의 `product.domain` 직접 import를 정리.
4. F-003: CI에서 `scripts/verify.sh` 실행.
