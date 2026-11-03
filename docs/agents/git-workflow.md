# Git 워크플로우

## 브랜치

`main`이 기본 브랜치이며 모든 작업의 통합 지점이다. 작업 브랜치는 `main`에서 분기하고 PR도 `main`으로 올린다. `develop` 브랜치는 없다.

| 접두사 | 용도 | 예시 |
|---|---|---|
| `feat/[domain]-[feature]` | 신규 기능 | `feat/inventory-fefo-allocation-logic` |
| `fix/[description]` | 버그 수정 | `fix/recall-status-mismatch` |
| `refactor/[description]` | 동작 변경 없는 개선 | |
| `docs/[description]` | 문서 | |
| `chore/[description]` | 빌드, 설정 | |

## 커밋

형식: `<type>(<scope>): <description>`

| Type | 용도 |
|---|---|
| `feat` | 새로운 기능 |
| `fix` | 버그 수정, 데이터 정합성 결함 해결 |
| `refactor` | 비즈니스 로직 변경 없는 개선 |
| `test` | 테스트 추가/수정 |
| `docs` | 문서 변경 |
| `chore` | 빌드, 설정 등 |
| `perf` | 성능 개선 |

- 설명은 "~한다"체가 아니라 명령문/명사형이다 (예: "FEFO 할당 로직 구현"). 제목 끝에 마침표를 넣지 않는다.
- 본문은 `-` 항목으로 나눈다.
- 하나의 커밋은 하나의 논리적 변경만 담는다. 관련 없는 변경을 묶지 않는다.
- 테스트가 실패한 상태로 커밋하지 않는다 (`scripts/verify.sh` 통과 후 커밋).
- Claude-Session 트레일러/링크는 커밋, 이슈, PR 어디에도 넣지 않는다.

예: `feat(inventory): 유통기한 기반 FEFO 할당 로직 구현`

## 이슈와 Pull Request

모든 작업은 이슈로 시작한다. 이슈 없이 브랜치를 만들거나 PR을 올리지 않는다.

1. **이슈 작성**: 작업 전에 이슈를 만든다. 템플릿(`.github/ISSUE_TEMPLATE/`)의 섹션을 따르고 제목은 `[FEATURE]`, `[BUG]`, `[REFACTOR]`, `[DOCS]`, `[CHORE]`, `[TEST]` 접두사를 쓴다.
2. **브랜치**: `main`에서 분기한다.
3. **PR**: `.github/PULL_REQUEST_TEMPLATE.md` 형식을 따르고 "연관된 이슈"에 `Closes #번호`를 적는다.
4. **머지 후** 작업 브랜치를 삭제한다.

### Assignees와 라벨 (이슈, PR 공통)

이슈와 PR 모두 **Assignees와 라벨을 반드시 지정**한다.

- Assignees: 작업을 요청한 사용자 본인(`gh api user -q .login`). 지정 대상이 불분명하면 사용자에게 묻는다.
- 라벨은 작업 Type에 맞춰 하나를 붙인다. 저장소의 라벨 이름은 이모지를 포함하므로 정확히 입력한다.

| 작업 Type | 라벨 |
|---|---|
| `feat` | `🛠️ feat` |
| `fix` | `🐞 bug` |
| `refactor`, `perf` | `🌱 refactor` |
| `docs` | `📝 documentation` |
| `chore` | `⚙️ chore` |
| `test` | `🧪 test` |

```bash
gh issue create --title "[FEATURE] ..." --body-file <파일> --label "🛠️ feat" --assignee <login>
gh pr create --base main --title "..." --body-file <파일> --label "🛠️ feat" --assignee <login>
```

PR의 라벨은 연결된 이슈와 같게 한다.

## 에이전트 작업 규칙

- 사용자가 요청하기 전에는 커밋/push/PR 생성을 하지 않는다. 단, 작업을 시작할 때 이슈를 먼저 만드는 것은 위 규칙에 따라 상시 허용된다 (사용자가 작업을 요청한 경우에 한한다).
- `main`에 직접 커밋하지 않는다.
- force push, 브랜치 삭제, 이력 재작성은 사용자가 명시적으로 요청한 경우에만 한다. 한 번의 승인이 다른 작업으로 확장되지 않는다.
- 비밀 정보 파일(`.env`, `*.pem`)을 스테이징하지 않는다. `git add -A` 전에 `git status`로 확인한다.
