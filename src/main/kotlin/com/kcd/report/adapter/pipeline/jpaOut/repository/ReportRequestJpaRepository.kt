package com.kcd.report.adapter.pipeline.jpaOut.repository

import com.kcd.report.adapter.pipeline.jpaOut.entity.ReportRequestJpaEntity
import com.kcd.report.domain.pipeline.enums.ReportRequestStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ReportRequestJpaRepository : JpaRepository<ReportRequestJpaEntity, Long> {
    /**
     * [WHY] §10 — 동일 사업자에 대해 진행 중(non-terminal) 요청이 있는지 검사.
     *       DB partial UNIQUE 인덱스가 race-safe 보장. 이 쿼리는 사용자에게 기존 ID 를
     *       반환하기 위한 read path.
     */
    fun findFirstByRegistrationNumberAndStatusNotIn(
        registrationNumber: String,
        statuses: Collection<ReportRequestStatus>,
    ): ReportRequestJpaEntity?
}
