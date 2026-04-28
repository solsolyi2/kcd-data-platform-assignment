package com.kcd.report.domain.partner.enums

/**
 * 파트너 노출 가능 지표 / 리포트 지표 타입.
 *
 * partner_policy.metric_type, report_metric.metric_type 두 곳에서 공유.
 * 새 지표 추가 시 V*__*.sql 의 CHECK 제약과 동시 변경.
 */
enum class MetricType {
    SALES,
    CREDIT_GRADE,
    EMPLOYEE,
    OPERATING_YEARS,
}
