package com.kcd.report.domain.partner.model

import java.time.Instant

class PartnerApiKey(
    val id: Long?,
    val partnerId: Long,
    val apiKey: String,
    val revokedAt: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    fun isActive(): Boolean = revokedAt == null

    fun revoke(at: Instant = Instant.now()): PartnerApiKey =
        if (revokedAt != null) this else copyWith(revokedAt = at)

    private fun copyWith(revokedAt: Instant?): PartnerApiKey =
        PartnerApiKey(
            id = id,
            partnerId = partnerId,
            apiKey = apiKey,
            revokedAt = revokedAt,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    companion object {
        fun create(partnerId: Long, apiKey: String): PartnerApiKey =
            PartnerApiKey(id = null, partnerId = partnerId, apiKey = apiKey)
    }
}
