package com.kcd.report.adapter.outbox.jpaOut.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "processed_event",
    uniqueConstraints = [UniqueConstraint(name = "uq_processed_event", columnNames = ["event_id", "consumer_group"])],
)
class ProcessedEventJpaEntity protected constructor(
    @Column(name = "event_id", nullable = false)
    val eventId: UUID,
    @Column(name = "consumer_group", nullable = false, length = 100)
    val consumerGroup: String,
    @Column(name = "processed_at", nullable = false)
    val processedAt: Instant = Instant.now(),
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(eventId: UUID, consumerGroup: String): ProcessedEventJpaEntity =
            ProcessedEventJpaEntity(eventId = eventId, consumerGroup = consumerGroup)
    }
}
