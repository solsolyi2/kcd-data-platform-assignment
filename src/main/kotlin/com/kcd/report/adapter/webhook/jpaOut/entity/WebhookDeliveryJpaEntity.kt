package com.kcd.report.adapter.webhook.jpaOut.entity

import com.kcd.report.adapter.common.jpaOut.entity.BaseJpaTimestampEntity
import com.kcd.report.domain.webhook.enums.WebhookDeliveryStatus
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
@Table(name = "webhook_delivery")
class WebhookDeliveryJpaEntity protected constructor(
    @Column(name = "partner_id", nullable = false)
    val partnerId: Long,
    @Column(name = "report_id", nullable = false)
    val reportId: Long,
    @Column(name = "endpoint_url", nullable = false, length = 500)
    val endpointUrl: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: WebhookDeliveryStatus = WebhookDeliveryStatus.PENDING,
    @Column(name = "attempt_count", nullable = false)
    var attemptCount: Int = 0,
    @Column(name = "last_error", columnDefinition = "TEXT")
    var lastError: String? = null,
    @Column(name = "last_attempt_at")
    var lastAttemptAt: Instant? = null,
    @Column(name = "succeeded_at")
    var succeededAt: Instant? = null,
) : BaseJpaTimestampEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(
            partnerId: Long,
            reportId: Long,
            endpointUrl: String,
            status: WebhookDeliveryStatus = WebhookDeliveryStatus.PENDING,
            attemptCount: Int = 0,
            lastError: String? = null,
            lastAttemptAt: Instant? = null,
            succeededAt: Instant? = null,
        ): WebhookDeliveryJpaEntity =
            WebhookDeliveryJpaEntity(
                partnerId = partnerId,
                reportId = reportId,
                endpointUrl = endpointUrl,
                status = status,
                attemptCount = attemptCount,
                lastError = lastError,
                lastAttemptAt = lastAttemptAt,
                succeededAt = succeededAt,
            )
    }
}
