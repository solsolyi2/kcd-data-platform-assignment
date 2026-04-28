package com.kcd.report.domain.report.model

import com.kcd.report.domain.partner.enums.MetricType
import java.time.Instant

class ReportMetric(
    val id: Long?,
    val reportId: Long,
    val metricType: MetricType,
    val value: Map<String, Any?>,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    companion object {
        fun create(reportId: Long, metricType: MetricType, value: Map<String, Any?>): ReportMetric =
            ReportMetric(id = null, reportId = reportId, metricType = metricType, value = value)
    }
}
