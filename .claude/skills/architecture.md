---
name: architecture
description: 헥사고날 아키텍처(Ports & Adapters) 레이어 구조와 의존성 방향 규칙. 새 기능 추가, 리팩토링, 아키텍처 관련 작업 시 참조한다.
---

# 헥사고날 아키텍처 & 의존성 방향 규칙

본 저장소(KCD 사업 리포트 데이터 파이프라인) 의 패키지 구조 단일 진실 원천.
CLAUDE.md §3 과 항상 일치해야 한다. (불일치 시 양쪽 동기화)

## 레이어 구조

```
src/main/kotlin/com/kcd/report/
├── ReportApplication.kt
├── config/
├── adapter/
│   └── {도메인}/
│       ├── restIn/                 # Driving Adapter — REST
│       │   ├── controller/
│       │   ├── dto/
│       │   └── mapper/
│       ├── kafkaIn/                # Driving Adapter — Kafka Consumer
│       │   ├── listener/
│       │   └── mapper/
│       ├── jpaOut/                 # Driven Adapter — JPA
│       │   ├── entity/
│       │   ├── repository/
│       │   ├── service/            # OutPort 구현체 (*JpaAdapter)
│       │   └── mapper/
│       ├── kafkaOut/               # Driven Adapter — KafkaTemplate
│       └── httpOut/                # Driven Adapter — RestClient
│
├── application/
│   └── {도메인}/
│       ├── service/                # UseCase 구현 (*Service)
│       ├── port/
│       │   ├── inbound/            # InPort — UseCase 인터페이스 (*UseCase)
│       │   └── outbound/           # OutPort — 영속성/외부 인터페이스 (*OutPort)
│       ├── dto/                    # Command / Query / Result
│       ├── mapper/
│       └── exception/
│
└── domain/
    └── {도메인}/
        ├── CLAUDE.md               # (선택) 도메인 상세 명세
        ├── model/                  # 순수 Kotlin 도메인 모델
        ├── vo/                     # 값 객체
        ├── enums/
        └── event/                  # 도메인 이벤트
```

## 본 프로젝트 도메인

`partner`, `report`, `pipeline`, `outbox`, `webhook`, `querylog`.

각 도메인은 위 트리 구조를 그대로 따른다 (해당 어댑터 종류가 없으면 디렉토리 생략 가능).

## 4가지 포트 역할

| 포트 | 위치 | 역할 | 예시 |
|------|------|------|------|
| InPort  | `application/{도메인}/port/inbound/`  | UseCase 인터페이스 | `AuthenticatePartnerUseCase` |
| OutPort | `application/{도메인}/port/outbound/` | 영속성 인터페이스 | `LoadPartnerOutPort` |
| Driving Adapter | `adapter/{도메인}/{rest|kafka}In/` | InPort 호출 | `ReportRequestController`, `ReportRequestedListener` |
| Driven Adapter  | `adapter/{도메인}/{jpa|kafka|http}Out/` | OutPort 구현 | `PartnerJpaAdapter`, `WebhookHttpAdapter` |

## 새 기능 추가 시 생성 순서

1. **Domain Model** (`domain/{도메인}/model/`) — 순수 Kotlin
2. **InPort 인터페이스** (`application/{도메인}/port/inbound/`)
3. **OutPort 인터페이스** (`application/{도메인}/port/outbound/`)
4. **UseCase 구현** (`application/{도메인}/service/`)
5. **JPA Entity + Driven Adapter** (`adapter/{도메인}/jpaOut/`) — entity / repository / service / mapper
6. **REST Controller / Kafka Listener** (`adapter/{도메인}/{rest|kafka}In/`)

## 의존성 방향 (단방향만 허용)

```
adapter ──→ application ──→ domain
```

- **domain**: 외부 의존성 없음. 순수 비즈니스 로직만. Spring/JPA/Jackson 어노테이션 **금지**.
- **application**: domain 에만 의존. 포트 인터페이스를 통해 외부와 소통. **adapter 패키지 import 금지.**
- **adapter**: application 의 포트를 구현하거나 호출.

### 역방향 의존성 절대 금지 — 예시

❌ domain 이 application 을 의존
```kotlin
// domain/partner/model/Partner.kt
import com.kcd.report.application.partner.dto.AuthenticatePartnerCommand   // 금지
```

❌ application 이 adapter 의 구현체를 의존
```kotlin
// application/partner/service/AuthenticatePartnerService.kt
import com.kcd.report.adapter.partner.jpaOut.repository.PartnerJpaRepository  // 금지
```

✅ application 은 OutPort 인터페이스만 의존
```kotlin
// application/partner/service/AuthenticatePartnerService.kt
import com.kcd.report.application.partner.port.outbound.LoadPartnerOutPort   // OK
```

### domain 에서 금지되는 것들

- Spring 어노테이션: `@Component`, `@Service`, `@Repository`, `@Configuration`, `@Bean`, `@Autowired` …
- JPA 어노테이션: `@Entity`, `@Id`, `@Column`, `@Table`, `@OneToMany` …
- 직렬화 어노테이션: `@JsonInclude`, `@JsonProperty` …
- application/adapter 패키지 import

도메인 모델은 JPA 와 분리된 순수 Kotlin. JPA 매핑은
`adapter/{도메인}/jpaOut/entity/{Domain}JpaEntity.kt` 에 별도 클래스로 두고,
`adapter/{도메인}/jpaOut/mapper/{Domain}EntityMapper.kt` 가 변환을 담당한다.

## 명명 규약

| 종류 | 접미사 | 예시 |
|---|---|---|
| 도메인 모델 | (없음) | `Partner`, `BusinessReport`, `ReportRequest` |
| JPA 엔티티 | `*JpaEntity` | `PartnerJpaEntity` |
| Spring Data 인터페이스 | `*JpaRepository` | `PartnerJpaRepository` |
| OutPort 구현 (Driven Adapter) | `*JpaAdapter` / `*HttpAdapter` / `*KafkaAdapter` | `PartnerJpaAdapter` |
| InPort 인터페이스 | `*UseCase` | `AuthenticatePartnerUseCase` |
| InPort 구현(서비스) | `*Service` | `AuthenticatePartnerService` |
| OutPort 인터페이스 | `*OutPort` 또는 동사 + `OutPort` | `LoadPartnerOutPort` |
| Mapper | `*EntityMapper` (entity↔domain), `*ResponseMapper` (DTO↔result) | `PartnerEntityMapper` |

## 체크리스트

- [ ] 새 파일이 올바른 레이어(adapter/application/domain)에 위치하는가?
- [ ] 도메인별 패키지 안에 있는가?
- [ ] InPort/OutPort 인터페이스가 application 레이어에 있는가?
- [ ] Adapter 가 adapter 레이어에 있는가?
- [ ] domain 패키지에 Spring/JPA/Jackson 어노테이션이 없는가?
- [ ] application 에서 adapter 패키지를 import 하지 않는가?
- [ ] 의존성 방향이 adapter → application → domain 인가?
- [ ] JPA 엔티티 ↔ 도메인 모델 매퍼가 있는가?
