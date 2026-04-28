package com.kcd.report.domain.report.model

import java.time.Instant

class BusinessReport(
    val id: Long?,
    val registrationNumber: String,
    val generatedAt: Instant,
    val metrics: List<ReportMetric> = emptyList(),
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    companion object {
        fun create(registrationNumber: String, generatedAt: Instant = Instant.now()): BusinessReport =
            BusinessReport(id = null, registrationNumber = registrationNumber, generatedAt = generatedAt)
    }
}
