package com.kcd.report.adapter.partner.jpaOut.entity

import com.kcd.report.adapter.common.jpaOut.entity.BaseJpaTimestampEntity
import com.kcd.report.domain.partner.enums.MetricType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "partner_policy")
class PartnerPolicyJpaEntity protected constructor(
    @Column(name = "partner_id", nullable = false)
    val partnerId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 30)
    val metricType: MetricType,
) : BaseJpaTimestampEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(partnerId: Long, metricType: MetricType): PartnerPolicyJpaEntity =
            PartnerPolicyJpaEntity(partnerId = partnerId, metricType = metricType)
    }
}
