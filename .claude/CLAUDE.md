# CLAUDE.md

이 문서는 KCD 데이터사업팀 과제(소상공인 사업 리포트 데이터 파이프라인) 개발 시
Claude 및 모든 협업자가 따라야 할 **프로젝트 지침**입니다.
코드를 작성하거나 수정하기 전에 반드시 이 문서를 참고하세요.

---

## 1. 프로젝트 개요

### 1.1 한 줄 요약
사업자등록번호 기반 사업 리포트를 **비동기 파이프라인**으로 생성하고,
파트너사별 정책에 따라 **필터링된 데이터를 서빙**하는 금융권 수준의 데이터 API.

### 1.2 핵심 비기능 요구사항 (절대 타협 금지)
1. **이벤트 유실 불가** — DB 상태가 바뀌면 Kafka 이벤트는 반드시 발행된다.
2. **이벤트 누락 방지** — 앱이 DB 커밋 직후 죽어도 이벤트는 누락되지 않는다.
3. **유령 이벤트 불가** — DB에 반영되지 않은 변경에 대한 이벤트는 절대 발행되지 않는다.
4. **순서 보장** — 동일 사업자에 대한 이벤트는 발생 순서대로 처리된다.
5. **At-Least-Once** — 중복 전달은 허용하되, Consumer는 멱등하게 처리한다.

> 위 5가지는 **모든 코드 리뷰의 1순위 기준**이다.
> 새 코드를 작성할 때마다 "이 변경이 위 제약을 깨지 않는가?"를 자문할 것.

---

## 2. 기술 스택 (고정)

| 항목 | 선택 | 비고 |
|---|---|---|
| 언어 | **Kotlin** | 과제 권장 사항. Java 혼용 금지. |
| 프레임워크 | Spring Boot 3.x | |
| 빌드 | Gradle (Kotlin DSL) | `build.gradle.kts` |
| JDK | 21 (LTS) | |
| ORM | Spring Data JPA + Hibernate | |
| DB | PostgreSQL 16 | |
| 마이그레이션 | Flyway | `V{n}__{description}.sql` |
| 메시징 | Apache Kafka (KRaft 모드) | Zookeeper 사용 X |
| Kafka 클라이언트 | Spring Kafka | |
| 테스트 | JUnit 5 + Kotest assertions + Testcontainers | |
| 직렬화 | Jackson (Kotlin module) | |
| 로깅 | SLF4J + Logback (JSON 포맷) | |
| 컨테이너 | Docker / Docker Compose | |

**금지 사항:**
- Lombok 사용 금지 (Kotlin은 data class로 충분)
- Spring Security 사용 금지 (인증은 인터셉터로 직접 구현 — 과제 범위에 부합)
- 라이브러리 신규 추가 시 사유를 PR/커밋 메시지에 명시

---

## 3. 패키지 구조 (Hexagonal — Ports & Adapters)

엄격한 헥사고날(Ports & Adapters). 의존성 방향은 **adapter → application → domain** 단방향만 허용.
도메인은 어떤 외부 의존성(Spring/JPA/Jackson 등)도 가지지 않는 **순수 Kotlin**.

### 3.1 레이어 트리 (com.kcd.report)

```
src/main/kotlin/com/kcd/report/
├── ReportApplication.kt
│
├── config/                            # 횡단 설정 (Jackson/Async/Kafka topics/MDC filter/WebMvc)
│
├── adapter/                           # 외부 세계와의 어댑터
│   ├── partner/
│   │   └── jpaOut/                    # Driven Adapter (영속성)
│   │       ├── entity/                # PartnerJpaEntity, PartnerApiKeyJpaEntity, PartnerPolicyJpaEntity
│   │       ├── repository/            # *JpaRepository (Spring Data)
│   │       ├── service/               # *JpaAdapter — application 의 OutPort 구현체
│   │       └── mapper/                # entity ↔ domain model
│   │
│   ├── report/
│   │   ├── restIn/                    # Driving Adapter — REST
│   │   │   ├── controller/            # ReportQueryController
│   │   │   ├── dto/                   # 요청/응답 DTO
│   │   │   └── mapper/                # DTO ↔ application command/result
│   │   └── jpaOut/                    # entity / repository / service / mapper
│   │
│   ├── pipeline/
│   │   ├── restIn/                    # ReportRequestController, ReportStatusController
│   │   ├── kafkaIn/                   # Driving Adapter — Kafka Consumer
│   │   │   ├── listener/              # @KafkaListener 빈
│   │   │   └── mapper/                # Kafka payload ↔ application command
│   │   └── jpaOut/                    # ReportRequest 영속성
│   │
│   ├── outbox/
│   │   ├── jpaOut/                    # OutboxEvent / ProcessedEvent 영속성
│   │   ├── kafkaOut/                  # Driven Adapter — KafkaTemplate 호출 어댑터
│   │   └── scheduler/                 # OutboxPollerScheduler (@Scheduled)
│   │
│   ├── webhook/
│   │   ├── kafkaIn/                   # WebhookConsumer (report.notify 구독)
│   │   ├── httpOut/                   # Driven Adapter — RestClient 로 파트너 HTTP 호출
│   │   └── jpaOut/                    # WebhookDelivery 이력
│   │
│   └── querylog/
│       └── jpaOut/                    # ReportQueryLog 영속성
│
├── application/                       # 유스케이스 + 포트 정의
│   ├── partner/
│   │   ├── service/                   # AuthenticatePartnerUseCase, GetPartnerPolicyUseCase
│   │   ├── port/
│   │   │   ├── inbound/               # InPort — UseCase 인터페이스
│   │   │   └── outbound/              # OutPort — 영속성/외부 시스템 인터페이스
│   │   ├── dto/                       # Command / Query / Result (포트 입출력 타입)
│   │   ├── mapper/                    # 내부 변환 헬퍼 (필요할 때)
│   │   └── exception/                 # application 레벨 예외 (없으면 생략)
│   ├── report/
│   ├── pipeline/                      # ReportRequest UseCase + Collection/Generation/Notification 핸들러
│   ├── outbox/                        # OutboxAppender InPort + OutPort (Kafka publish)
│   ├── webhook/                       # WebhookSendUseCase + WebhookDeliveryOutPort
│   └── querylog/                      # QueryLogRecorder UseCase
│
└── domain/                            # 순수 도메인 (Spring/JPA 어노테이션 절대 X)
    ├── partner/
    │   ├── CLAUDE.md                  # (선택) 도메인 상세 명세
    │   ├── model/                     # Partner, PartnerApiKey, PartnerPolicy (pure Kotlin)
    │   ├── vo/                        # ApiKey 등 값 객체
    │   └── enums/                     # MetricType
    ├── report/
    │   ├── model/                     # BusinessReport, ReportMetric
    │   ├── vo/                        # RegistrationNumber (validation 포함)
    │   └── enums/
    ├── pipeline/
    │   ├── model/                     # ReportRequest (상태머신 메서드 포함)
    │   ├── enums/                     # ReportRequestStatus
    │   └── event/                     # 도메인 이벤트 페이로드 정의
    ├── outbox/
    │   └── model/                     # OutboxEvent, ProcessedEvent (도메인 모델)
    ├── webhook/
    │   ├── model/                     # WebhookDelivery
    │   └── enums/                     # WebhookDeliveryStatus
    └── querylog/
        └── model/                     # ReportQueryLog
```

> **공통(common) 위치**: `ApiResponse`, `DomainException`, `ErrorCode`, `BaseTimestamp`(JPA용 — `adapter/`).
> 응답/예외/에러코드는 도메인 영역의 가치를 표현하므로 `domain/common/` 또는 `application/common/` 에.
> 진짜 횡단 인프라(Filter/Interceptor/Async/Config)는 `config/`.

### 3.2 4가지 포트 역할

| 포트 | 위치 | 역할 |
|---|---|---|
| **InPort**  | `application/{도메인}/port/inbound/`  | UseCase 인터페이스 (어댑터가 호출) |
| **OutPort** | `application/{도메인}/port/outbound/` | 영속성/외부시스템 인터페이스 (UseCase 가 호출) |
| **Driving Adapter** | `adapter/{도메인}/{rest|kafka}In/` | 외부 입력 → InPort 호출 |
| **Driven Adapter**  | `adapter/{도메인}/{jpa|kafka|http}Out/` | OutPort 구현 → 외부 시스템 호출 |

### 3.3 새 기능 추가 순서

1. `domain/{도메인}/model/` — 도메인 모델 (또는 VO/enum/event)
2. `application/{도메인}/port/inbound/` — InPort 인터페이스
3. `application/{도메인}/port/outbound/` — OutPort 인터페이스
4. `application/{도메인}/service/` — UseCase 구현 (도메인 + 포트 의존)
5. `adapter/{도메인}/jpaOut/` — JPA Entity + Repository + OutPort 구현 + Mapper
6. `adapter/{도메인}/restIn/` 또는 `kafkaIn/` — Driving Adapter (Controller / Consumer)

### 3.4 의존성 방향 (단방향만 허용)

```
adapter ──→ application ──→ domain
```

**역방향 / 동일 레벨 우회 절대 금지:**

- domain → application/adapter 의 어떤 클래스도 import 금지.
- application → adapter 의 구현 클래스 import 금지. (포트 인터페이스만 의존)
- adapter 끼리 직접 호출 금지. 다른 도메인을 부르고 싶으면 application 의 InPort 를 호출.

#### domain 패키지에서 금지

- Spring 어노테이션: `@Component`, `@Service`, `@Repository`, `@Configuration`, `@Bean`, `@Autowired` …
- JPA 어노테이션: `@Entity`, `@Id`, `@Column`, `@Table`, `@OneToMany` …
- Jackson / Web 어노테이션: `@JsonInclude`, `@JsonProperty`, `@RequestBody` …
- application/adapter 의 클래스 import.

도메인 모델은 **JPA 와 무관한 순수 Kotlin** 이며, JPA 매핑은 `adapter/{도메인}/jpaOut/entity/{Domain}JpaEntity.kt` 에 별도 클래스로 둔다. 둘 사이는 `adapter/{도메인}/jpaOut/mapper/{Domain}EntityMapper.kt` 가 변환.

### 3.5 명명 규약

- 도메인 모델: `Partner`, `BusinessReport`, `ReportRequest` (접미사 없음).
- JPA 엔티티: `PartnerJpaEntity`, `BusinessReportJpaEntity` (`*JpaEntity` 접미사).
- Repository: `PartnerJpaRepository` (Spring Data 인터페이스).
- OutPort 구현: `PartnerJpaAdapter` (`*Adapter` 접미사).
- InPort 인터페이스: `AuthenticatePartnerUseCase` (`*UseCase` 접미사).
- 서비스(InPort 구현): `AuthenticatePartnerService` (`*Service` 접미사).
- Mapper: `PartnerEntityMapper` (도메인 ↔ entity), `ReportResponseMapper` (DTO ↔ application result).

### 3.6 체크리스트 (PR/커밋 전)

- [ ] 새 파일이 올바른 레이어(adapter/application/domain)에 위치한다.
- [ ] 도메인별 패키지 안에 있다.
- [ ] InPort/OutPort 인터페이스가 application 레이어에 있다.
- [ ] domain 패키지에 Spring/JPA/Jackson 어노테이션이 없다.
- [ ] application 에서 adapter 패키지를 import 하지 않는다.
- [ ] 의존성 방향이 adapter → application → domain 이다.
- [ ] 같은 도메인에 JPA 엔티티 ↔ 도메인 모델 매퍼가 있다.

---

## 4. 핵심 설계 원칙

### 4.1 Transactional Outbox 패턴 (필수)

**규칙:** Kafka로 직접 `kafkaTemplate.send()` 하는 코드는 **금지**한다.
모든 도메인 이벤트는 다음 흐름을 따른다.

```
[Service.method @Transactional]
   1. 도메인 엔티티 변경 (save/update)
   2. OutboxAppender.append(topic, key, payload)   ← 같은 트랜잭션
   ↓ COMMIT
[OutboxPoller @Scheduled]
   3. SELECT ... FOR UPDATE SKIP LOCKED
   4. kafkaTemplate.send()
   5. processed_at = now()
```

**이유:**
- 비즈니스 변경과 이벤트 발행 의도가 같은 트랜잭션 → 유령 이벤트 방지
- DB만 살아있으면 폴러가 재시도 → 누락 방지
- Kafka 장애 시에도 outbox에 쌓여있다가 복구되면 발행됨

### 4.2 Kafka 파티션 키 = `registrationNumber`

동일 사업자에 대한 이벤트가 같은 파티션에 들어가야 순서 보장이 된다.
**모든 Producer 코드는 `registrationNumber`를 key로 보낸다.**

### 4.3 Consumer 멱등성

At-Least-Once 환경에서 중복 메시지는 항상 발생할 수 있다.
모든 Consumer는 다음 둘 중 **하나 이상**으로 멱등성을 보장한다.

1. **상태 머신 검증**: `if (request.status != EXPECTED_PREV_STATE) return`
2. **`processed_event` 테이블**: `event_id` Unique 제약 → 중복 시 skip

### 4.4 상태 머신

`ReportRequest.status`는 다음 전이만 허용한다.

```
REQUESTED ──→ COLLECTING ──→ COLLECTED ──→ GENERATING ──→ GENERATED ──→ NOTIFYING ──→ COMPLETED
     │             │              │             │              │             │
     └─→ FAILED ←──┴──────────────┴─────────────┴──────────────┴─────────────┘
        (어느 단계든 실패하면 FAILED + failureStage + failureReason)
```

도메인 객체에 전이 메서드를 두고, 잘못된 전이 시 예외를 던진다.

```kotlin
fun startCollecting() {
    require(status == REQUESTED) { "Cannot collect from $status" }
    status = COLLECTING
    updatedAt = Instant.now()
}
```

### 4.5 트랜잭션 경계

- **HTTP 요청 핸들러**: 가능한 짧게. 외부 호출(Kafka, Webhook) 절대 포함하지 않는다.
- **Consumer 핸들러**: DB 작업 + Outbox append만 트랜잭션. Kafka 발행은 별도 폴러.
- **조회 이력 적재**: 본 응답 트랜잭션과 분리 (`@Async` 또는 별도 outbox 발행).

### 4.6 Webhook 독립성

A은행 Webhook 실패가 B보증재단을 지연시키면 안 된다.
→ 알림 단계는 **fan-out**: 파트너사별로 별도 메시지를 발행하고, Consumer는 파티션 단위로 병렬 처리.

```
report.generated (1건)
   ↓ NotificationDispatcher
report.notify (N건, key=partnerId)
   ↓ WebhookConsumer (파트너별 병렬)
실제 HTTP 호출
```

---

## 5. API 설계 규약

### 5.1 공통 응답 포맷

성공:
```json
{ "success": true, "data": { ... } }
```

실패:
```json
{ "success": false, "error": { "code": "auth001", "message": "..." } }
```

`ApiResponse<T>` sealed class로 통일한다.

### 5.2 에러 코드 체계

| 도메인 | 코드 prefix | 예시 |
|---|---|---|
| 인증 | `auth` | `auth001` 유효하지 않은 API Key |
| 리포트 조회 | `report` | `report001` 리포트를 찾을 수 없음 |
| 파이프라인 | `pipeline` | `pipeline001` 중복 요청 / `pipeline002` 잘못된 상태 전이 |
| 검증 | `valid` | `valid001` 유효하지 않은 사업자번호 |
| 시스템 | `sys` | `sys001` 내부 오류 |

`GlobalExceptionHandler`에서 도메인 예외 → ErrorResponse 변환 일원화.

### 5.3 인증 규약

- 모든 비즈니스 API는 `X-Api-Key` 헤더 필수.
- 컨트롤러는 `@AuthenticatedPartner partner: Partner` 파라미터를 받는다.
- 인증 로직은 `ApiKeyInterceptor` + ArgumentResolver에 격리. 컨트롤러/서비스에서 직접 헤더를 읽지 않는다.

---

## 6. DB 컨벤션

- 테이블/컬럼명: `snake_case`
- PK: `id BIGINT GENERATED ALWAYS AS IDENTITY`
- 모든 테이블에 `created_at`, `updated_at` (TIMESTAMP WITH TIME ZONE)
- 시간은 항상 **UTC** 저장, 응답 시 ISO-8601
- 사업자번호: `VARCHAR(10)` (하이픈 제거된 숫자만)
- 외래키는 명시적으로 정의, ON DELETE는 명시
- 인덱스 추가 시 사유를 마이그레이션 파일 주석에 남긴다

**필수 인덱스:**
- `business_report(registration_number)` — 조회 핫패스
- `report_metric(report_id)` — JOIN
- `outbox_event(processed_at, id)` — 폴러 쿼리
- `report_request(registration_number, status)` — 중복 요청 검사

---

## 7. 코딩 컨벤션

### 7.1 Kotlin 스타일
- ktlint / detekt 기본 규칙 따른다.
- nullable 최소화. 도메인 필드는 가능한 non-null + 기본값.
- `data class`로 DTO/이벤트 페이로드 정의.
- 도메인 엔티티는 JPA 호환을 위해 `class` + `protected constructor`.
- 확장 함수 남용 금지. 도메인 의미가 명확할 때만.

### 7.2 명명
- 이벤트 페이로드: `{Domain}{Action}Event` (예: `ReportRequestedEvent`)
- 토픽: `report.requested`, `report.collected`, `report.generated`, `report.notify`
- Kafka 그룹 ID: `{service}-{topic}` (예: `collector-report-requested`)

### 7.3 주석 태그 (의무)
설계 결정이 들어간 코드에는 반드시 다음 태그 중 하나를 단다.

- `// [WHY] ...` — 왜 이렇게 작성했는가
- `// [CONSIDER] ...` — 향후 개선/주의 포인트
- `// [TRADEOFF] ...` — 다른 방식 대비 절충점

예:
```kotlin
// [WHY] outbox 폴러는 SKIP LOCKED로 다중 인스턴스 동시 폴링을 안전 처리.
// 같은 row를 두 인스턴스가 잡으면 이벤트 중복 발행 → at-least-once는 OK이지만 불필요한 낭비.
val events = outboxRepository.findUnprocessedForUpdateSkipLocked(limit = 100)
```

### 7.4 로깅
- 모든 Consumer/요청 진입 시 MDC에 `traceId`, `registrationNumber`, `reportId` 세팅.
- 로그 레벨:
  - INFO — 단계 시작/완료
  - WARN — 재시도 가능한 실패
  - ERROR — DLT 진입, 예상치 못한 예외
- API Key는 항상 마스킹 (`abc***xyz`).

---

## 8. 테스트 정책

### 8.1 분류
- **단위 테스트**: 도메인 로직, 정책 필터링, 상태 머신 전이.
- **슬라이스 테스트**: `@DataJpaTest`, `@WebMvcTest`로 레이어 단위 검증.
- **통합 테스트**: `@SpringBootTest` + Testcontainers(Postgres + Kafka).

### 8.2 필수 시나리오
- [ ] 정상 플로우: 요청 → 수집 → 생성 → 알림 → 조회 가능
- [ ] 동일 사업자 중복 요청 → 동일 reportId 반환
- [ ] 수집 실패 후 재시도 성공
- [ ] 수집 N회 실패 → DLT 진입 + 상태 FAILED
- [ ] 파트너 A는 SALES 보이고, 파트너 B는 SALES + OPERATING_YEARS만 보임
- [ ] Webhook A 실패 시 B는 정상 발송
- [ ] 조회 이력 저장 실패해도 조회 응답은 성공

### 8.3 통합 테스트는 반드시 Testcontainers
인메모리 H2 / Embedded Kafka 사용 금지 (실제 동작과 차이가 큼).

---

## 9. 운영/실행

### 9.1 로컬 실행
```bash
docker compose up -d        # postgres + kafka
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 9.2 전체 컨테이너 실행
```bash
docker compose --profile app up --build
```

### 9.3 환경 프로파일
- `local`: 호스트에서 직접 실행 (DB/Kafka는 컨테이너)
- `docker`: 앱도 컨테이너 안에서 실행
- `test`: Testcontainers 자동 설정

### 9.4 시드 데이터
앱 기동 시 또는 Flyway `R__seed.sql`로 다음을 자동 적재:
- 파트너사 2개: `A은행`, `B보증재단`
- 각 파트너 API Key 1개씩
- 정책: A은행 = `SALES, CREDIT_GRADE, EMPLOYEE` / B보증재단 = `SALES, OPERATING_YEARS`

테스트용 API Key는 README에 명시한다.

---

## 10. 작업 진행 시 체크리스트

새 기능을 구현할 때마다 PR/커밋 전에 확인:

- [ ] 운영 제약 5가지를 깨지 않는가?
- [ ] 새 이벤트 발행 코드가 Outbox를 거치는가?
- [ ] Kafka 파티션 키에 `registrationNumber`를 사용했는가?
- [ ] Consumer가 멱등한가?
- [ ] 상태 전이가 도메인 객체 안에서 검증되는가?
- [ ] 외부 호출이 트랜잭션 안에 있지 않은가?
- [ ] 에러 응답이 공통 포맷을 따르는가?
- [ ] 인증/비즈니스 로직이 분리되어 있는가?
- [ ] 테스트가 작성되었는가? (특히 실패/재시도/멱등)
- [ ] `[WHY]` / `[CONSIDER]` 주석이 핵심 결정에 달려있는가?

---

## 11. 제출 산출물 체크리스트

- [ ] GitHub Repository (public 또는 초대)
- [ ] `README.md`
  - 실행 방법 (docker compose 한 번에 기동되도록)
  - 아키텍처 다이어그램
  - 핵심 설계 결정 + 트레이드오프 설명
  - 테스트 API Key 안내
  - 동작 시나리오 예시 (curl 또는 HTTP 파일)
- [ ] DDL
- [ ] `docker-compose.yml`로 서버 + Kafka가 한 번에 기동
- [ ] 통합 테스트가 `./gradlew test`로 통과
- [ ] `CLAUDE.md` (이 문서) — 협업/AI 개발 지침으로 함께 제출

---

## 12. 의사결정 로그 (DECISION LOG)

추후 설계 결정이 추가될 때마다 이 섹션에 append-only로 기록한다.
형식: `날짜 | 결정 | 사유 | 대안`

| 날짜 | 결정 | 사유 | 대안 |
|---|---|---|---|
| 2026-04-28 | 이벤트 일관성 = Transactional Outbox | 5가지 운영 제약 중 4가지를 한 번에 해결 | CDC(Debezium) — 인프라 부담 / 직접 발행 — 유령 이벤트 위험 |
| 2026-04-28 | 순서 보장 = `registrationNumber` 파티션 키 | 동일 사업자 이벤트 단일 파티션화로 순서 자동 보장 | 글로벌 단일 파티션 — 처리량 저하 |
| 2026-04-28 | Webhook fan-out 토픽 분리 | 파트너간 장애 격리 | 단일 Consumer 순차 호출 — 요구사항 위반 |
| 2026-04-28 | 인증 = Interceptor + ArgumentResolver | 비즈니스 로직 분리 + Spring Security 오버엔지니어링 회피 | Spring Security |
| 2026-04-28 | 컨테이너 = Kafka KRaft 모드 | Zookeeper 운영 부담 제거, 최신 베스트 프랙티스 | Zookeeper 모드 |

---

이 문서는 살아있는 문서다.
설계가 바뀌면 이 문서를 먼저 업데이트하고, 그 다음 코드를 바꾼다.
