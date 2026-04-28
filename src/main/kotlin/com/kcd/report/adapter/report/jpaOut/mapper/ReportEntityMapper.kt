package com.kcd.report.adapter.report.jpaOut.mapper

import com.kcd.report.adapter.report.jpaOut.entity.BusinessReportJpaEntity
import com.kcd.report.adapter.report.jpaOut.entity.ReportMetricJpaEntity
import com.kcd.report.domain.report.model.BusinessReport
import com.kcd.report.domain.report.model.ReportMetric

object ReportEntityMapper {
    fun toDomain(entity: BusinessReportJpaEntity, metrics: List<ReportMetric> = emptyList()): BusinessReport =
        BusinessReport(
            id = entity.id,
            registrationNumber = entity.registrationNumber,
            generatedAt = entity.generatedAt,
            metrics = metrics,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: BusinessReport): BusinessReportJpaEntity =
        BusinessReportJpaEntity.of(registrationNumber = domain.registrationNumber, generatedAt = domain.generatedAt)

    fun toDomain(entity: ReportMetricJpaEntity): ReportMetric =
        ReportMetric(
            id = entity.id,
            reportId = entity.reportId,
            metricType = entity.metricType,
            value = entity.value,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: ReportMetric): ReportMetricJpaEntity =
        ReportMetricJpaEntity.of(reportId = domain.reportId, metricType = domain.metricType, value = domain.value)
}
