package com.kcd.report.adapter.pipeline.jpaOut.mapper

import com.kcd.report.adapter.pipeline.jpaOut.entity.ReportRequestJpaEntity
import com.kcd.report.domain.pipeline.model.ReportRequest

object ReportRequestEntityMapper {
    fun toDomain(entity: ReportRequestJpaEntity): ReportRequest =
        ReportRequest(
            id = entity.id,
            registrationNumber = entity.registrationNumber,
            requesterPartnerId = entity.requesterPartnerId,
            status = entity.status,
            failureStage = entity.failureStage,
            failureReason = entity.failureReason,
            completedAt = entity.completedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: ReportRequest): ReportRequestJpaEntity =
        ReportRequestJpaEntity.of(
            registrationNumber = domain.registrationNumber,
            requesterPartnerId = domain.requesterPartnerId,
            status = domain.status,
            failureStage = domain.failureStage,
            failureReason = domain.failureReason,
            completedAt = domain.completedAt,
        )
}
