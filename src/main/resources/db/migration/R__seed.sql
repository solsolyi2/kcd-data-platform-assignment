-- ============================================================
-- R__seed.sql — Repeatable migration
--   * 매 마이그레이션마다 재실행됨. ON CONFLICT 로 멱등 보장.
--   * CLAUDE.md §9.4 — 파트너 2개 + API Key + 정책 자동 적재.
--   * 테스트용 API Key는 README 에 명시.
-- ============================================================

-- ── 파트너사 ────────────────────────────────────────────────
INSERT INTO partner (code, name) VALUES
    ('A_BANK',      'A은행'),
    ('B_GUARANTEE', 'B보증재단')
ON CONFLICT (code) DO NOTHING;

-- ── API Keys (테스트용 고정값) ─────────────────────────────
-- [WHY] 운영에서는 절대 이런 평문 / 예측 가능한 키를 쓰지 않음.
--        과제 채점/시연용으로만 README 에 노출.
INSERT INTO partner_api_key (partner_id, api_key)
SELECT p.id, 'test-key-A_BANK-0001'
FROM partner p
WHERE p.code = 'A_BANK'
ON CONFLICT (api_key) DO NOTHING;

INSERT INTO partner_api_key (partner_id, api_key)
SELECT p.id, 'test-key-B_GUARANTEE-0001'
FROM partner p
WHERE p.code = 'B_GUARANTEE'
ON CONFLICT (api_key) DO NOTHING;

-- ── 정책: A은행 = SALES, CREDIT_GRADE, EMPLOYEE ────────────
INSERT INTO partner_policy (partner_id, metric_type)
SELECT p.id, m.metric_type
FROM   partner p
CROSS JOIN (VALUES ('SALES'),('CREDIT_GRADE'),('EMPLOYEE')) AS m(metric_type)
WHERE  p.code = 'A_BANK'
ON CONFLICT (partner_id, metric_type) DO NOTHING;

-- ── 정책: B보증재단 = SALES, OPERATING_YEARS ───────────────
INSERT INTO partner_policy (partner_id, metric_type)
SELECT p.id, m.metric_type
FROM   partner p
CROSS JOIN (VALUES ('SALES'),('OPERATING_YEARS')) AS m(metric_type)
WHERE  p.code = 'B_GUARANTEE'
ON CONFLICT (partner_id, metric_type) DO NOTHING;
