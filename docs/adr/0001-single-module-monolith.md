# ADR-0001: 단일 모듈 모놀리스 유지

- 상태: Accepted
- 날짜: 2026-11-03

## 배경

2026-07~08월에 프로젝트를 `service-ordering`, `service-auth`, `libs/common`, `libs/contracts`로 나누고 Kafka 기반 서비스 간 통신으로 전환하는 멀티 모듈/MSA 분리를 시도했다. 분리는 완료되지 않았고 WMS 도메인(inbound, inspection, inventory, lot, outbound, storage, partner)은 어느 모듈에도 속하지 않은 채 남았다.

## 결정

분리를 폐기하고 마지막 단일 모듈 상태(`5a48431`)로 복원한다. 분리 관련 코드, Kafka 구성, Docker 구성, 브랜치와 이력은 제거했다. 프로젝트는 하나의 Gradle 모듈, 하나의 Spring Boot 애플리케이션이다.

## 결과

- 도메인 간 협력은 in-process Spring 이벤트와 포트 호출이다 ([events.md](../agents/events.md)). 서비스 간 REST/Kafka 통신, Outbox, 서비스별 DB 분리는 없다.
- 도메인 경계(hexagonal 패키지 + ID 참조)는 그대로 유지한다. 나중에 분리가 필요해지더라도 경계를 따라 나눌 수 있다.
- 멀티 모듈/MSA를 다시 도입하려면 새 ADR로 결정한다. 이 결정을 모른 채 `libs/`, `service-*` 구조나 Kafka 의존성을 추가하지 않는다.
