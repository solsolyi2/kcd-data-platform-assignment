package com.kcd.report.adapter.querylog.jpaOut.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "report_query_log")
class ReportQueryLogJpaEntity protected constructor(
    @Column(name = "partner_id", nullable = false)
    val partnerId: Long,
    @Column(name = "registration_number", nullable = false, length = 10)
    val registrationNumber: String,
    @Column(name = "response_status", nullable = false)
    val responseStatus: Int,
    @Column(name = "queried_at", nullable = false)
    val queriedAt: Instant = Instant.now(),
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(partnerId: Long, registrationNumber: String, responseStatus: Int): ReportQueryLogJpaEntity =
            ReportQueryLogJpaEntity(
                partnerId = partnerId,
                registrationNumber = registrationNumber,
                responseStatus = responseStatus,
            )
    }
}
