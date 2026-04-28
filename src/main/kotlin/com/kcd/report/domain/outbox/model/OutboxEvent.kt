package com.kcd.report.domain.outbox.model

import java.time.Instant
import java.util.UUID

/**
 * Transactional Outbox 이벤트 — CLAUDE.md §4.1 (도메인 모델, 순수 Kotlin).
 */
class OutboxEvent(
    val id: Long?,
    val eventId: UUID,
    val aggregateType: String,
    val aggregateId: String,
    val topic: String,
    /**
     * §4.2 — 동일 사업자 이벤트는 같은 파티션 → 순서 보장. 거의 모든 케이스에서
     *        partitionKey = registrationNumber.
     */
    val partitionKey: String,
    val payload: Map<String, Any?>,
    val headers: Map<String, Any?> = emptyMap(),
    var retryCount: Int = 0,
    var lastError: String? = null,
    var processedAt: Instant? = null,
    val createdAt: Instant? = null,
) {
    fun markProcessed(at: Instant = Instant.now()) {
        processedAt = at
        lastError = null
    }

    fun markFailed(error: String) {
        retryCount += 1
        lastError = error.take(MAX_ERROR_LEN)
    }

    companion object {
        private const val MAX_ERROR_LEN = 4000

        fun create(
            aggregateType: String,
            aggregateId: String,
            topic: String,
            partitionKey: String,
            payload: Map<String, Any?>,
            headers: Map<String, Any?> = emptyMap(),
            eventId: UUID = UUID.randomUUID(),
        ): OutboxEvent =
            OutboxEvent(
                id = null,
                eventId = eventId,
                aggregateType = aggregateType,
                aggregateId = aggregateId,
                topic = topic,
                partitionKey = partitionKey,
                payload = payload,
                headers = headers,
            )
    }
}
