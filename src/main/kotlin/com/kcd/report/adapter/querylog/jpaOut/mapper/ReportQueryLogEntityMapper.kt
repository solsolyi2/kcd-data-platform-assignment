package com.kcd.report.adapter.querylog.jpaOut.mapper

import com.kcd.report.adapter.querylog.jpaOut.entity.ReportQueryLogJpaEntity
import com.kcd.report.domain.querylog.model.ReportQueryLog

object ReportQueryLogEntityMapper {
    fun toDomain(entity: ReportQueryLogJpaEntity): ReportQueryLog =
        ReportQueryLog(
            id = entity.id,
            partnerId = entity.partnerId,
            registrationNumber = entity.registrationNumber,
            responseStatus = entity.responseStatus,
            queriedAt = entity.queriedAt,
            createdAt = entity.createdAt,
        )

    fun toEntity(domain: ReportQueryLog): ReportQueryLogJpaEntity =
        ReportQueryLogJpaEntity.of(
            partnerId = domain.partnerId,
            registrationNumber = domain.registrationNumber,
            responseStatus = domain.responseStatus,
        )
}
