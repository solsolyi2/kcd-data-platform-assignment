package com.kcd.report.adapter.partner.jpaOut.repository

import com.kcd.report.adapter.partner.jpaOut.entity.PartnerPolicyJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PartnerPolicyJpaRepository : JpaRepository<PartnerPolicyJpaEntity, Long> {
    /**
     * [WHY] N+1 방지 — 한 파트너의 정책 전체를 단일 쿼리로 fetch.
     */
    fun findAllByPartnerId(partnerId: Long): List<PartnerPolicyJpaEntity>
}
