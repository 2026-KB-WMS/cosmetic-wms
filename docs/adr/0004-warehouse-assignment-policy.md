# ADR-0004: 창고 배정 정책 (`WeightBasedAssignmentPolicy`)

- 상태: Accepted
- 날짜: 2026-07-03
- 이슈: #78

## 배경

발주 확정 시 어느 창고에서 출고할지 자동으로 정해야 한다. 이슈 원문은 "SKU 충족률 가중치 합산"이었다.

## 결정

- **전량 충족을 하드 필터로 승격한다.** 부분 충족 창고가 배정되면 이후 outbound FEFO 할당에서 `OutboundInsufficientStockException`으로 깨지기 때문이다. 대신 "SKU 충족률" 가중치는 **재고 여유율**(요청 대비 가용 배수, 3배에서 포화)로 재해석해 유지한다.
- **가중치 기본값**: 주행거리 0.5 / FEFO 임박 재고 0.3 / 재고 여유율 0.2. 하버사인 1차 필터로 상위 5개를 추린다. 생성자 파라미터로 조정할 수 있다.
- **폴백**: Routing API 호출이 1건이라도 실패하면 shortlist 전체를 하버사인 거리로 폴백한다. 주행거리와 직선거리를 섞어 비교하면 주행거리가 항상 크므로 불공정해진다.
- **도메인 순수성**: 정책이 application 포트(`RoutingPort`)에 의존하지 않도록 `DrivingDistanceProvider` 함수형 인터페이스를 도메인에 정의하고, 서비스가 `routingPort::drivingDistanceMeters`를 주입한다.

## 결과

- 지오코딩/라우팅은 Kakao API를 쓴다. `KAKAO_REST_API_KEY`가 없으면 창고·가맹점 등록이 502로 실패한다.
- 배정은 `OrderConfirmedEvent` 후 `AFTER_COMMIT` 비동기로 실행되고 재시도한다 ([events.md](../agents/events.md)).
