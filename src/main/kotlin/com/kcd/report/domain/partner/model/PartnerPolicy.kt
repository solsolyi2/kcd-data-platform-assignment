package com.kcd.report.domain.partner.model

import com.kcd.report.domain.partner.enums.MetricType
import java.time.Instant

class PartnerPolicy(
    val id: Long?,
    val partnerId: Long,
    val metricType: MetricType,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    companion object {
        fun create(partnerId: Long, metricType: MetricType): PartnerPolicy =
            PartnerPolicy(id = null, partnerId = partnerId, metricType = metricType)
    }
}
