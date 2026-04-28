-- ============================================================
-- Phase 5 — 핵심 도메인 스키마 초기화
--   * CLAUDE.md §3 패키지 구조와 1:1 매핑
--   * §6 DB 컨벤션 (snake_case / IDENTITY PK / TIMESTAMPTZ / UTC)
--   * N+1 방지: 모든 N-side(자식 측)에 (parent_id) 인덱스를 명시.
--               자주 함께 조회되는 쌍은 복합 인덱스.
-- ============================================================

-- ─────────────────────────────────────────────────────────────
-- partner / API Key / 정책
-- ─────────────────────────────────────────────────────────────

CREATE TABLE partner (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
COMMENT ON TABLE partner IS '파트너사. code 가 외부 식별자.';

CREATE TABLE partner_api_key (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partner_id  BIGINT       NOT NULL,
    api_key     VARCHAR(128) NOT NULL UNIQUE,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_partner_api_key_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id) ON DELETE CASCADE
);
COMMENT ON TABLE partner_api_key IS '파트너사 API Key — X-Api-Key 헤더 검증 대상.';
-- [WHY] 인증 핫패스: api_key UNIQUE 제약으로 인덱스가 자동 생성되어 별도 인덱스 불필요.
-- [WHY] N+1 방지: partner → 키 다건 조회 시 partner_id 인덱스 필요.
CREATE INDEX idx_partner_api_key_partner_id ON partner_api_key(partner_id);

CREATE TABLE partner_policy (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partner_id   BIGINT      NOT NULL,
    metric_type  VARCHAR(30) NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_partner_policy_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id) ON DELETE CASCADE,
    CONSTRAINT uq_partner_policy UNIQUE (partner_id, metric_type),
    CONSTRAINT chk_partner_policy_metric
        CHECK (metric_type IN ('SALES','CREDIT_GRADE','EMPLOYEE','OPERATING_YEARS'))
);
COMMENT ON TABLE partner_policy IS '파트너사별 노출 가능 지표(metric) 화이트리스트.';
-- [WHY] N+1 방지: Partner 단건 조회 후 정책 fetch 시 (partner_id) 인덱스로 단일 IO.
--        UNIQUE(partner_id, metric_type) 의 leading column 인덱스로 자동 활용 가능하지만,
--        명시적으로 (partner_id) 인덱스를 두어 조회 의도를 코드 리뷰 단계에서 분명히 한다.
CREATE INDEX idx_partner_policy_partner_id ON partner_policy(partner_id);

-- ─────────────────────────────────────────────────────────────
-- business_report / report_metric (조회 도메인)
-- ─────────────────────────────────────────────────────────────

CREATE TABLE business_report (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    registration_number VARCHAR(10) NOT NULL UNIQUE,
    generated_at        TIMESTAMPTZ NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    -- [WHY] §6 — 사업자번호는 하이픈 제거된 10자리 숫자만.
    CONSTRAINT chk_business_report_reg_number_format
        CHECK (registration_number ~ '^[0-9]{10}$')
);
COMMENT ON TABLE business_report IS '사업자번호 단위 사업 리포트(현재 유효본).';
-- [WHY] CLAUDE.md §6 필수 인덱스 — UNIQUE 제약이 동등비교 인덱스로 그대로 활용됨.
--        별도 인덱스 추가 X (중복 비용).

CREATE TABLE report_metric (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    report_id    BIGINT      NOT NULL,
    metric_type  VARCHAR(30) NOT NULL,
    value        JSONB       NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_report_metric_report
        FOREIGN KEY (report_id) REFERENCES business_report(id) ON DELETE CASCADE,
    CONSTRAINT uq_report_metric UNIQUE (report_id, metric_type),
    CONSTRAINT chk_report_metric_type
        CHECK (metric_type IN ('SALES','CREDIT_GRADE','EMPLOYEE','OPERATING_YEARS'))
);
COMMENT ON TABLE report_metric IS '리포트의 개별 지표. value 는 지표별 형태가 달라 JSONB 로.';
-- [WHY] CLAUDE.md §6 필수 인덱스 + N+1 방지.
--        JPA `@OneToMany`에 `JOIN FETCH r.metrics` / `@BatchSize`로 일괄 fetch 시
--        (report_id) 인덱스가 핵심. UNIQUE(report_id, metric_type) 의 prefix 로
--        커버되므로 별도 인덱스 X.

-- ─────────────────────────────────────────────────────────────
-- report_request (파이프라인 상태머신)
-- ─────────────────────────────────────────────────────────────

CREATE TABLE report_request (
    id                    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    registration_number   VARCHAR(10) NOT NULL,
    requester_partner_id  BIGINT      NOT NULL,
    status                VARCHAR(20) NOT NULL,
    failure_stage         VARCHAR(20),
    failure_reason        TEXT,
    completed_at          TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_report_request_partner
        FOREIGN KEY (requester_partner_id) REFERENCES partner(id) ON DELETE RESTRICT,
    CONSTRAINT chk_report_request_reg_number
        CHECK (registration_number ~ '^[0-9]{10}$'),
    CONSTRAINT chk_report_request_status
        CHECK (status IN ('REQUESTED','COLLECTING','COLLECTED',
                          'GENERATING','GENERATED','NOTIFYING',
                          'COMPLETED','FAILED'))
);
COMMENT ON TABLE report_request IS
    '리포트 생성 파이프라인 요청 + 상태머신(§4.4). failure_stage / failure_reason 로 실패 추적.';
-- [WHY] CLAUDE.md §6 필수 인덱스 — 중복 요청 검사 핫패스.
CREATE INDEX idx_report_request_reg_status
    ON report_request(registration_number, status);
-- [WHY] §10 — 동일 사업자에 대해 진행 중 요청은 1건만.
--        부분 UNIQUE 인덱스: 종료 상태(COMPLETED/FAILED)는 새 요청 허용.
CREATE UNIQUE INDEX uq_report_request_inflight
    ON report_request(registration_number)
    WHERE status NOT IN ('COMPLETED','FAILED');
-- [WHY] partner 가 자신이 만든 요청 이력 조회.
CREATE INDEX idx_report_request_partner ON report_request(requester_partner_id);

-- ─────────────────────────────────────────────────────────────
-- outbox_event (Transactional Outbox — §4.1 핵심)
-- ─────────────────────────────────────────────────────────────

CREATE TABLE outbox_event (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id        UUID         NOT NULL UNIQUE,
    aggregate_type  VARCHAR(50)  NOT NULL,
    aggregate_id    VARCHAR(64)  NOT NULL,
    topic           VARCHAR(100) NOT NULL,
    -- [WHY] §4.2 — 동일 사업자 이벤트는 같은 파티션 → 순서 보장.
    partition_key   VARCHAR(64)  NOT NULL,
    payload         JSONB        NOT NULL,
    headers         JSONB        NOT NULL DEFAULT '{}'::jsonb,
    retry_count     INT          NOT NULL DEFAULT 0,
    last_error      TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    processed_at    TIMESTAMPTZ
);
COMMENT ON TABLE outbox_event IS
    'Transactional Outbox — 비즈니스 변경 + 발행 의도를 같은 트랜잭션에 묶어 일관성 보장.';
-- [WHY] CLAUDE.md §6 필수 인덱스 — Poller 의 SELECT ... FOR UPDATE SKIP LOCKED 쿼리.
--        Partial index: 미처리 이벤트만 인덱싱 → 인덱스 크기/유지비용 최소.
CREATE INDEX idx_outbox_unprocessed
    ON outbox_event(id)
    WHERE processed_at IS NULL;
-- [CONSIDER] 운영 환경에서는 처리완료 이벤트를 일정 기간 후 archive 테이블로 이관 (cron).

-- ─────────────────────────────────────────────────────────────
-- processed_event (Consumer 멱등성 — §4.3)
-- ─────────────────────────────────────────────────────────────

CREATE TABLE processed_event (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id        UUID         NOT NULL,
    consumer_group  VARCHAR(100) NOT NULL,
    processed_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_processed_event UNIQUE (event_id, consumer_group)
);
COMMENT ON TABLE processed_event IS
    'Consumer 멱등성 — At-Least-Once 환경에서 (event_id, consumer_group) 중복 INSERT 시 conflict → skip.';
-- [WHY] UNIQUE(event_id, consumer_group) 가 멱등 INSERT 시 동등 비교에 사용됨 → 별도 인덱스 X.

-- ─────────────────────────────────────────────────────────────
-- webhook_delivery
-- ─────────────────────────────────────────────────────────────

CREATE TABLE webhook_delivery (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partner_id      BIGINT       NOT NULL,
    report_id       BIGINT       NOT NULL,
    endpoint_url    VARCHAR(500) NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    attempt_count   INT          NOT NULL DEFAULT 0,
    last_error      TEXT,
    last_attempt_at TIMESTAMPTZ,
    succeeded_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT fk_webhook_delivery_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id) ON DELETE RESTRICT,
    CONSTRAINT fk_webhook_delivery_report
        FOREIGN KEY (report_id) REFERENCES business_report(id) ON DELETE CASCADE,
    CONSTRAINT chk_webhook_delivery_status
        CHECK (status IN ('PENDING','SUCCEEDED','FAILED','EXHAUSTED'))
);
COMMENT ON TABLE webhook_delivery IS '파트너 Webhook 발송 이력 + 재시도 카운트.';
-- [WHY] N+1 방지 — partner 별 / report 별 발송 이력 조회.
CREATE INDEX idx_webhook_delivery_partner_id ON webhook_delivery(partner_id);
CREATE INDEX idx_webhook_delivery_report_id  ON webhook_delivery(report_id);

-- ─────────────────────────────────────────────────────────────
-- report_query_log (조회 audit — 비동기 적재 §4.5)
-- ─────────────────────────────────────────────────────────────

CREATE TABLE report_query_log (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    partner_id          BIGINT      NOT NULL,
    registration_number VARCHAR(10) NOT NULL,
    response_status     INT         NOT NULL,
    queried_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fk_query_log_partner
        FOREIGN KEY (partner_id) REFERENCES partner(id) ON DELETE RESTRICT
);
COMMENT ON TABLE report_query_log IS
    '리포트 조회 audit — 본 응답 트랜잭션과 분리하여 @Async 적재. 적재 실패가 응답을 막지 않는다.';
-- [WHY] partner 별 audit 조회 + 시간 정렬 핫패스.
CREATE INDEX idx_query_log_partner_time
    ON report_query_log(partner_id, queried_at DESC);
