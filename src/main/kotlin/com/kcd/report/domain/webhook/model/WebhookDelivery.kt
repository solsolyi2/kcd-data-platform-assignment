package com.kcd.report.domain.webhook.model

import com.kcd.report.domain.webhook.enums.WebhookDeliveryStatus
import java.time.Instant

class WebhookDelivery(
    val id: Long?,
    val partnerId: Long,
    val reportId: Long,
    val endpointUrl: String,
    var status: WebhookDeliveryStatus = WebhookDeliveryStatus.PENDING,
    var attemptCount: Int = 0,
    var lastError: String? = null,
    var lastAttemptAt: Instant? = null,
    var succeededAt: Instant? = null,
    val createdAt: Instant? = null,
    var updatedAt: Instant? = null,
) {
    fun recordAttempt(at: Instant = Instant.now()) {
        attemptCount += 1
        lastAttemptAt = at
    }

    fun markSucceeded(at: Instant = Instant.now()) {
        status = WebhookDeliveryStatus.SUCCEEDED
        succeededAt = at
        lastError = null
    }

    fun markFailed(error: String) {
        status = WebhookDeliveryStatus.FAILED
        lastError = error.take(MAX_ERROR_LEN)
    }

    fun markExhausted(error: String) {
        status = WebhookDeliveryStatus.EXHAUSTED
        lastError = error.take(MAX_ERROR_LEN)
    }

    companion object {
        private const val MAX_ERROR_LEN = 4000

        fun create(partnerId: Long, reportId: Long, endpointUrl: String): WebhookDelivery =
            WebhookDelivery(id = null, partnerId = partnerId, reportId = reportId, endpointUrl = endpointUrl)
    }
}
