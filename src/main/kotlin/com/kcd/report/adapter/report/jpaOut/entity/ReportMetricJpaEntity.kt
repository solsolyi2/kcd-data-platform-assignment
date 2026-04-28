package com.kcd.report.adapter.report.jpaOut.entity

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
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "report_metric")
class ReportMetricJpaEntity protected constructor(
    @Column(name = "report_id", nullable = false)
    val reportId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 30)
    val metricType: MetricType,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value", nullable = false, columnDefinition = "jsonb")
    val value: Map<String, Any?>,
) : BaseJpaTimestampEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(reportId: Long, metricType: MetricType, value: Map<String, Any?>): ReportMetricJpaEntity =
            ReportMetricJpaEntity(reportId = reportId, metricType = metricType, value = value)
    }
}
