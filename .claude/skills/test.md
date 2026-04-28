---
name: test
description: KCD 저장소 테스트 실행/작성 가이드. JUnit 5 + Kotest assertions + Testcontainers(Postgres + Kafka), 단위·슬라이스·통합 분류, 필수 시나리오(중복 요청/재시도/DLT/정책 필터링/Webhook 격리/조회이력 격리)를 보장. 테스트 실행, 실패 분석, 새 테스트 작성 시 사용.
---

# test — KCD 파이프라인 테스트 운용

본 저장소(Spring Boot 3.5 + Kotlin) 의 테스트 실행 / 작성 / 디버깅을 표준화한다.
H2 / EmbeddedKafka **금지** (CLAUDE.md §8.3 — 실제 Postgres/Kafka 와 동작 차이 발생).

## 분류 (CLAUDE.md §8.1)

| 종류 | 도구 | 대상 |
|---|---|---|
| 단위 | JUnit 5 + Kotest assertions + MockK | 도메인 로직 / 정책 필터링 / 상태 머신 전이 |
| 슬라이스 | `@DataJpaTest`, `@WebMvcTest` | JPA Repository, Controller 만 |
| 통합 | `@SpringBootTest` + Testcontainers | 실제 Postgres + Kafka. E2E 시나리오 |

네이밍: `should_동작_when_조건` (한국어 동사 가능). 클래스명 = `{대상}Test`.

## 실행 명령

```bash
# 전체 테스트
./gradlew test

# 특정 클래스
./gradlew test --tests "com.kcd.report.pipeline.ReportRequestServiceTest"

# 통합만 (네이밍 컨벤션으로 분리 — 향후 task 추가)
./gradlew test --tests "*IntegrationTest"

# 한 메서드만, 디버그 로그
./gradlew test --tests "ClassName.methodName" --info
```

## 작성 가이드

- 도메인 단위 테스트는 Spring 컨텍스트 띄우지 말 것 (느림).
- Testcontainers 는 `@DynamicPropertySource` 로 datasource / kafka 설정 주입:

```kotlin
companion object {
    @JvmStatic
    @Container
    val postgres = PostgreSQLContainer("postgres:16-alpine").apply {
        withDatabaseName("kcd_report_test")
    }

    @JvmStatic
    @Container
    val kafka = KafkaContainer(DockerImageName.parse("apache/kafka:3.7.1"))

    @DynamicPropertySource @JvmStatic
    fun props(reg: DynamicPropertyRegistry) {
        reg.add("spring.datasource.url", postgres::getJdbcUrl)
        reg.add("spring.datasource.username", postgres::getUsername)
        reg.add("spring.datasource.password", postgres::getPassword)
        reg.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers)
    }
}
```

- Kafka assertion 은 `Awaitility.await()` 패턴으로 **데드락 없이** 대기.
- DB 검증은 `@Sql` 또는 helper repository 로. 스레드 안에서 `@Transactional` 주의.

## 필수 시나리오 (CLAUDE.md §8.2)

PR/Phase 종료 전 다음이 통과해야 한다 (최소 1건씩 존재).

- [ ] 정상 플로우: 요청 → 수집 → 생성 → 알림 → 조회 가능
- [ ] 동일 사업자 중복 요청 → 동일 reportId 반환 (DuplicateRequestException 또는 reuse)
- [ ] 수집 실패 후 재시도 성공
- [ ] 수집 N회 실패 → DLT 진입 + 상태 FAILED
- [ ] 파트너 A 정책 = SALES,CREDIT_GRADE,EMPLOYEE 만 노출
- [ ] 파트너 B 정책 = SALES,OPERATING_YEARS 만 노출
- [ ] Webhook A 실패 시 B는 정상 발송 (격리)
- [ ] 조회이력 저장 실패해도 조회 응답은 200

## 실패 분석 절차

1. `build/reports/tests/test/index.html` 또는 콘솔 출력으로 실패 클래스/메서드 식별.
2. 단일 케이스 재실행: `./gradlew test --tests "ClassName.methodName" --info`
3. 5대 NFR 관점 자가 점검 (`code-review` 스킬과 동일):
   - 이벤트 유실/누락/유령/순서/멱등 중 어떤 것이 깨졌나?
4. Testcontainers 가 시작되지 않으면: `colima` / Docker Desktop 동작 여부 확인.
5. flaky 의심 시: 같은 케이스 3회 반복 → 안정성 확인 후 commit.

## Anti-pattern

- `Thread.sleep(...)` 대기 — `Awaitility` 또는 `kafka-test` 의 `containerProperties` 사용.
- `H2` / `EmbeddedKafka` 사용 — §8.3 금지.
- 단일 거대 통합 테스트 — 시나리오별로 분리해야 실패 원인 식별 가능.
- 테스트 메서드끼리 상태 공유 — 각 테스트는 독립 상태에서 시작해야.

## 빠른 명령 모음

```bash
# 빌드 + 전체 테스트 (실패 시 stack trace)
./gradlew clean build --info

# 컨테이너 사전 풀업 (Testcontainers 첫 실행 가속)
docker pull postgres:16-alpine
docker pull apache/kafka:3.7.1

# ktlint
./gradlew ktlintCheck
./gradlew ktlintFormat   # 자동 수정
```
