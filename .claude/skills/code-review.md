---
name: code-review
description: KCD 사업 리포트 파이프라인 코드 리뷰. CLAUDE.md §1.2 5대 NFR(이벤트 유실/누락/유령/순서/멱등)을 1순위로 검사하고, §10 체크리스트와 N+1, 트랜잭션 경계, 인덱스 적합성을 확인. 새/변경된 코드를 작성·수정한 직후 또는 PR 단위로 사용.
---

# code-review — KCD 파이프라인 전용 코드 리뷰

이 스킬은 본 저장소(KCD 사업 리포트 데이터 파이프라인)의 **고유한** 리뷰 기준에 맞게
코드를 점검한다. 일반 린트나 스타일은 이미 ktlint가 처리하므로 여기선 다루지 않는다.

## 1순위 — CLAUDE.md §1.2 운영 제약 5가지 (절대 타협 금지)

각 변경에 대해 다음 5개 질문에 모두 "통과"여야 한다. 하나라도 의심스러우면 BLOCKING.

1. **이벤트 유실 불가** — DB 상태 변경 후 Kafka 이벤트가 반드시 발행되는가?
   * Outbox 경유인가? `kafkaTemplate.send()` 직접 호출은 **금지**.
2. **이벤트 누락 방지** — 앱이 DB 커밋 직후 죽어도 이벤트는 다시 발행되는가?
   * Outbox row 가 같은 `@Transactional` 안에 들어갔는가?
3. **유령 이벤트 불가** — DB 미반영 변경에 대해 이벤트가 발행되지 않는가?
   * 외부 호출(Kafka, Webhook)이 트랜잭션 **밖**에 있는가? (CLAUDE.md §4.5)
4. **순서 보장** — 동일 사업자 이벤트가 같은 파티션인가?
   * Producer key = `registrationNumber` 인가? (§4.2)
5. **At-Least-Once 멱등** — 중복 메시지를 안전하게 skip 하는가?
   * 상태머신 검증 또는 `processed_event` UNIQUE 둘 중 하나 이상 적용? (§4.3)

## 2순위 — §10 체크리스트

- [ ] 새 이벤트 발행 코드가 `OutboxAppender` 경유
- [ ] Producer key `registrationNumber`
- [ ] Consumer 멱등성 (둘 중 하나)
- [ ] 상태 전이가 도메인 객체(`ReportRequest`)에서 검증
- [ ] HTTP 핸들러/서비스 트랜잭션 안에 외부 호출 없음
- [ ] 응답 포맷이 `ApiResponse` 통일
- [ ] 인증 로직이 컨트롤러/서비스에 새지 않음 (Interceptor + ArgumentResolver)
- [ ] 핵심 결정에 `// [WHY]` / `[CONSIDER]` / `[TRADEOFF]` 태그
- [ ] 단위 + 통합 테스트 (특히 실패/재시도/멱등 시나리오)

## N+1 점검

- 새 `@OneToMany` / 컬렉션 fetch 가 추가되면:
  - JPQL 에 `JOIN FETCH` 또는 `@EntityGraph` 또는 `@BatchSize` 적용?
  - 자식 측에 `(parent_id)` 인덱스가 V*__*.sql 에 정의되어 있는가?
- 반복문 안에서 repository 호출은 즉시 BLOCKING.
- 정책 필터링은 Partner + Policy 를 한 번에 fetch (정책 N건 N+1 금지).

## 트랜잭션 / 동시성

- `@Transactional` 범위 안에 Kafka publish, HTTP 호출, 긴 sleep 없음.
- 외부 호출은 commit 후 별도 폴러/`@Async` 로.
- DB 락: `SELECT ... FOR UPDATE SKIP LOCKED` 가 outbox 폴러에 적용되는가? (§4.1)

## DB / 마이그레이션

- 새 컬럼 추가 → V{n}__... NOT NULL 인 경우 default 값 또는 backfill 전략 명시.
- 모든 테이블에 `created_at`, `updated_at` (TIMESTAMPTZ).
- 시간은 항상 UTC.
- 인덱스 추가 사유가 마이그레이션 주석에 있는가? (§6)

## 인증 / API

- API 응답이 `ApiResponse<T>` 만 반환하는가? `ResponseEntity<Map>` 같은 임시 형태 금지.
- 도메인 예외만 던지고 컨트롤러/서비스는 try-catch 로 응답 포맷팅 안 함.
- API Key 가 평문 로그에 노출되지 않는가? 마스킹(`abc***xyz`).

## 리뷰 출력 형식

```
[BLOCKING]   §x.y 위반: <설명> — 위치 file:line
[NEEDS-FIX]  N+1 위험: <설명> — 위치 file:line
[CONSIDER]   향후 개선: <설명>
[OK]         정상 — <간단 요약>
```

각 항목은 **CLAUDE.md 어느 절** 위반인지 명시한다. 추측이 아닌 근거 기반 리뷰.

## 사용 흐름

1. 변경된 파일 목록 확보 (`git diff --name-only main...HEAD` 또는 staged).
2. 위 1순위 질문 5개를 각 파일에 적용.
3. 트랜잭션 / N+1 / DB / API 영역으로 확장 검토.
4. BLOCKING이 0건이면 OK, 그 외엔 수정 후 재검토.
