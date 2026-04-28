package com.kcd.report.adapter.querylog.jpaOut.repository

import com.kcd.report.adapter.querylog.jpaOut.entity.ReportQueryLogJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ReportQueryLogJpaRepository : JpaRepository<ReportQueryLogJpaEntity, Long> {
    fun findAllByPartnerId(partnerId: Long): List<ReportQueryLogJpaEntity>
}
