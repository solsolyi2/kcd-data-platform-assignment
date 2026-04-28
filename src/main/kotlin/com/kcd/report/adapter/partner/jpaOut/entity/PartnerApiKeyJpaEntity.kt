package com.kcd.report.adapter.partner.jpaOut.entity

import com.kcd.report.adapter.common.jpaOut.entity.BaseJpaTimestampEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "partner_api_key")
class PartnerApiKeyJpaEntity protected constructor(
    @Column(name = "partner_id", nullable = false)
    val partnerId: Long,
    @Column(name = "api_key", nullable = false, unique = true, length = 128)
    val apiKey: String,
    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,
) : BaseJpaTimestampEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(partnerId: Long, apiKey: String, revokedAt: Instant? = null): PartnerApiKeyJpaEntity =
            PartnerApiKeyJpaEntity(partnerId = partnerId, apiKey = apiKey, revokedAt = revokedAt)
    }
}
