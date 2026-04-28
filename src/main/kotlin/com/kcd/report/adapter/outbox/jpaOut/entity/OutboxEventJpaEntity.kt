package com.kcd.report.adapter.outbox.jpaOut.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

/**
 * [WHY] outbox_event 는 V1 스키마에 updated_at 이 없다 (append + mark-processed 패턴).
 *        BaseJpaTimestampEntity 상속하지 않고 createdAt 만 둔다.
 */
@Entity
@Table(name = "outbox_event")
class OutboxEventJpaEntity protected constructor(
    @Column(name = "event_id", nullable = false, unique = true)
    val eventId: UUID,
    @Column(name = "aggregate_type", nullable = false, length = 50)
    val aggregateType: String,
    @Column(name = "aggregate_id", nullable = false, length = 64)
    val aggregateId: String,
    @Column(nullable = false, length = 100)
    val topic: String,
    @Column(name = "partition_key", nullable = false, length = 64)
    val partitionKey: String,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    val payload: Map<String, Any?>,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    val headers: Map<String, Any?> = emptyMap(),
    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,
    @Column(name = "last_error", columnDefinition = "TEXT")
    var lastError: String? = null,
    @Column(name = "processed_at")
    var processedAt: Instant? = null,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.EPOCH
        protected set

    companion object {
        fun of(
            eventId: UUID,
            aggregateType: String,
            aggregateId: String,
            topic: String,
            partitionKey: String,
            payload: Map<String, Any?>,
            headers: Map<String, Any?> = emptyMap(),
            retryCount: Int = 0,
            lastError: String? = null,
            processedAt: Instant? = null,
        ): OutboxEventJpaEntity =
            OutboxEventJpaEntity(
                eventId = eventId,
                aggregateType = aggregateType,
                aggregateId = aggregateId,
                topic = topic,
                partitionKey = partitionKey,
                payload = payload,
                headers = headers,
                retryCount = retryCount,
                lastError = lastError,
                processedAt = processedAt,
            )
    }
}
