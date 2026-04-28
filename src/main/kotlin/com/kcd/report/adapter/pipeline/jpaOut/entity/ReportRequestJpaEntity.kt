package com.kcd.report.adapter.pipeline.jpaOut.entity

import com.kcd.report.adapter.common.jpaOut.entity.BaseJpaTimestampEntity
import com.kcd.report.domain.pipeline.enums.ReportRequestStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "report_request")
class ReportRequestJpaEntity protected constructor(
    @Column(name = "registration_number", nullable = false, length = 10)
    val registrationNumber: String,
    @Column(name = "requester_partner_id", nullable = false)
    val requesterPartnerId: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ReportRequestStatus = ReportRequestStatus.REQUESTED,
    @Enumerated(EnumType.STRING)
    @Column(name = "failure_stage", length = 20)
    var failureStage: ReportRequestStatus? = null,
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    var failureReason: String? = null,
    @Column(name = "completed_at")
    var completedAt: Instant? = null,
) : BaseJpaTimestampEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(
            registrationNumber: String,
            requesterPartnerId: Long,
            status: ReportRequestStatus = ReportRequestStatus.REQUESTED,
            failureStage: ReportRequestStatus? = null,
            failureReason: String? = null,
            completedAt: Instant? = null,
        ): ReportRequestJpaEntity =
            ReportRequestJpaEntity(
                registrationNumber = registrationNumber,
                requesterPartnerId = requesterPartnerId,
                status = status,
                failureStage = failureStage,
                failureReason = failureReason,
                completedAt = completedAt,
            )
    }
}
