package com.kcd.report.adapter.partner.jpaOut.repository

import com.kcd.report.adapter.partner.jpaOut.entity.PartnerApiKeyJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PartnerApiKeyJpaRepository : JpaRepository<PartnerApiKeyJpaEntity, Long> {
    /**
     * [WHY] 인증 핫패스 — UNIQUE 인덱스로 O(1).
     *       활성 여부(`revokedAt is null`)는 어댑터/도메인에서 검사.
     */
    fun findByApiKey(apiKey: String): PartnerApiKeyJpaEntity?
}
