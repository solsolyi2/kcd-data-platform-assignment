package com.kcd.report.adapter.partner.jpaOut.repository

import com.kcd.report.adapter.partner.jpaOut.entity.PartnerJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PartnerJpaRepository : JpaRepository<PartnerJpaEntity, Long> {
    fun findByCode(code: String): PartnerJpaEntity?
}
