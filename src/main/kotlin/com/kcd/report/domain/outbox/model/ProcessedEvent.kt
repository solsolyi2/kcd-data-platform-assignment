package com.kcd.report.domain.outbox.model

import java.time.Instant
import java.util.UUID

/**
 * Consumer 멱등성 — CLAUDE.md §4.3.
 *
 * (event_id, consumer_group) UNIQUE 위반 시 INSERT 실패 → 메시지 중복 skip.
 */
class ProcessedEvent(
    val id: Long?,
    val eventId: UUID,
    val consumerGroup: String,
    val processedAt: Instant = Instant.now(),
) {
    companion object {
        fun of(eventId: UUID, consumerGroup: String): ProcessedEvent =
            ProcessedEvent(id = null, eventId = eventId, consumerGroup = consumerGroup)
    }
}
