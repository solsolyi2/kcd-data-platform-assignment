package com.kcd.report.domain.partner.model

import java.time.Instant

/**
 * 파트너사 도메인 모델 (순수 Kotlin).
 *
 * [WHY] §3.4 — Spring/JPA 어노테이션 없음. 어댑터(JpaEntity)와 분리.
 */
class Partner(
    val id: Long?,
    val code: String,
    val name: String,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    companion object {
        fun create(code: String, name: String): Partner = Partner(id = null, code = code, name = name)
    }
}
