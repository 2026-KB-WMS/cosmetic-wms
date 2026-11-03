# 테스트

변경하거나 추가한 동작에는 반드시 그 동작을 검증하는 테스트를 함께 작성한다. 컴파일만 되는 것은 완료가 아니다.

## 계층별 테스트

각 계층의 테스트는 책임이 겹치지 않게 분리한다.

| 접미사 | 어노테이션 | Spring Context | 목적 |
|---|---|---|---|
| `*EntityTest` | 없음 | 없음 | 도메인 로직과 불변식 (도메인 모델 대상) |
| `*ServiceTest` | `@ExtendWith(MockitoExtension.class)` | 없음 | 서비스 로직 (포트는 Mock) |
| `*ControllerTest` | `@WebMvcTest` + `RestDocsSupport` | 슬라이스 | API 계약과 REST Docs 스니펫 생성 |
| `*RepositoryTest` | `@DataJpaTest` 또는 `@SpringBootTest` | 슬라이스/전체 | JPA 쿼리 (H2) |
| `*IntegrationTest` | `@SpringBootTest` | 전체 | 여러 도메인에 걸친 이벤트 플로우 (`integration/`) |

테스트 파일은 `src/test/java/com/kb/cosmetic_wms/{domain}/`에 둔다. 계층 하위 패키지로 나누지 않고 도메인 폴더에 평평하게 둔다 (영속성 어댑터 테스트는 `{domain}/adapter/out/`).

## 작성 규칙

- **Given-When-Then** 구조로 읽기 쉽게 쓴다. 한 테스트는 한 가지 동작만 검증한다.
- 메서드 이름은 한국어 문장이다. `@DisplayName`은 쓰지 않는다. 예: `재고_생성_시_수량이_음수이면_예외를_던진다`
- 검증은 AssertJ를 쓴다 (`assertThat`, `assertThatThrownBy(...).isInstanceOf(...).hasMessage(...)`).
- 예외는 타입과 메시지를 함께 검증한다.
- 현실적인 시나리오를 쓴다. 경계값, 상태 전이 위반, 중복, 부재, 동시성 같은 엣지 케이스를 의식적으로 찾는다. 테스트를 쓸 때 어떤 케이스가 빠졌는지도 함께 보고한다.
- 도메인 모델에 불변식을 추가하면 `*EntityTest`에, 서비스 분기를 추가하면 `*ServiceTest`에, API 계약을 바꾸면 `*ControllerTest`에 테스트를 추가한다.
- "변경 후 `port.save()` 누락" 같은 영속성 버그는 Mock 서비스 테스트로 잡히지 않는다. 변경이 DB에 반영되어야 하는 로직은 `*RepositoryTest`나 `*IntegrationTest`로 검증한다 (예: `InventoryAllocationPersistenceIntegrationTest`).

## 픽스처

`{domain}/fixture/` 패키지에 둔다.

- 도메인 모델: `XxxTestBuilder` (빌더 방식, 기본값 제공)
- 요청 DTO: `XxxDtoBuilder`
- REST Docs 헬퍼: `XxxDocumentUtils`

기본값을 바꿀 때는 이를 사용하는 모든 테스트가 영향을 받으므로 `scripts/verify.sh`로 확인한다.

## REST Docs → OpenAPI

`*ControllerTest`는 `RestDocsSupport`를 상속한다 (MockMvc, REST Docs, Spring Security, UTF-8 필터 구성). 보안이 필요 없는 테스트는 `@BeforeEach`에서 직접 MockMvc를 구성한다.

- `document(...)`에 `ResourceSnippetParameters`를 넘긴다 (tag, summary, schema).
- 태그 이름은 `ApiDocs.*`, 스키마 이름은 `ApiSchemas.*` 상수를 쓴다. 새 도메인이면 상수를 추가한다.
- `./gradlew test`가 스니펫을 만들고 `./gradlew openapi3`가 `build/api-spec/openapi3.yaml`로 컴파일한다. `./gradlew build`는 이를 `src/main/resources/static/docs`로 복사한다 (gitignore 대상).

## 테스트 환경

- 테스트 DB는 H2 (`MODE=MySQL`)이고 Flyway 마이그레이션을 그대로 적용한다. 설정은 `src/test/resources/application.yml`.
- 마이그레이션에 MySQL 전용 구문을 쓰면 H2에서 깨질 수 있다. 새 마이그레이션은 H2에서도 통과하는지 `scripts/verify.sh`로 확인한다.
- 성능/부하 시나리오는 `k6/scenarios/`에 있으며 `./gradlew test`에 포함되지 않는다. k6는 별도로 설치해서 쓴다.
