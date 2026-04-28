package com.kcd.report.adapter.partner.jpaOut.mapper

import com.kcd.report.adapter.partner.jpaOut.entity.PartnerApiKeyJpaEntity
import com.kcd.report.adapter.partner.jpaOut.entity.PartnerJpaEntity
import com.kcd.report.adapter.partner.jpaOut.entity.PartnerPolicyJpaEntity
import com.kcd.report.domain.partner.model.Partner
import com.kcd.report.domain.partner.model.PartnerApiKey
import com.kcd.report.domain.partner.model.PartnerPolicy

/**
 * Partner 관련 entity ↔ domain 변환.
 *
 * [WHY] §3.4 — adapter 계층의 매퍼. application 은 도메인 모델만 본다.
 */
object PartnerEntityMapper {
    fun toDomain(entity: PartnerJpaEntity): Partner =
        Partner(
            id = entity.id,
            code = entity.code,
            name = entity.name,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: Partner): PartnerJpaEntity = PartnerJpaEntity.of(code = domain.code, name = domain.name)

    fun toDomain(entity: PartnerApiKeyJpaEntity): PartnerApiKey =
        PartnerApiKey(
            id = entity.id,
            partnerId = entity.partnerId,
            apiKey = entity.apiKey,
            revokedAt = entity.revokedAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: PartnerApiKey): PartnerApiKeyJpaEntity =
        PartnerApiKeyJpaEntity.of(partnerId = domain.partnerId, apiKey = domain.apiKey, revokedAt = domain.revokedAt)

    fun toDomain(entity: PartnerPolicyJpaEntity): PartnerPolicy =
        PartnerPolicy(
            id = entity.id,
            partnerId = entity.partnerId,
            metricType = entity.metricType,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: PartnerPolicy): PartnerPolicyJpaEntity =
        PartnerPolicyJpaEntity.of(partnerId = domain.partnerId, metricType = domain.metricType)
}
