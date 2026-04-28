package com.kcd.report.adapter.outbox.jpaOut.mapper

import com.kcd.report.adapter.outbox.jpaOut.entity.OutboxEventJpaEntity
import com.kcd.report.adapter.outbox.jpaOut.entity.ProcessedEventJpaEntity
import com.kcd.report.domain.outbox.model.OutboxEvent
import com.kcd.report.domain.outbox.model.ProcessedEvent

object OutboxEventEntityMapper {
    fun toDomain(entity: OutboxEventJpaEntity): OutboxEvent =
        OutboxEvent(
            id = entity.id,
            eventId = entity.eventId,
            aggregateType = entity.aggregateType,
            aggregateId = entity.aggregateId,
            topic = entity.topic,
            partitionKey = entity.partitionKey,
            payload = entity.payload,
            headers = entity.headers,
            retryCount = entity.retryCount,
            lastError = entity.lastError,
            processedAt = entity.processedAt,
            createdAt = entity.createdAt,
        )

    fun toEntity(domain: OutboxEvent): OutboxEventJpaEntity =
        OutboxEventJpaEntity.of(
            eventId = domain.eventId,
            aggregateType = domain.aggregateType,
            aggregateId = domain.aggregateId,
            topic = domain.topic,
            partitionKey = domain.partitionKey,
            payload = domain.payload,
            headers = domain.headers,
            retryCount = domain.retryCount,
            lastError = domain.lastError,
            processedAt = domain.processedAt,
        )

    fun toDomain(entity: ProcessedEventJpaEntity): ProcessedEvent =
        ProcessedEvent(
            id = entity.id,
            eventId = entity.eventId,
            consumerGroup = entity.consumerGroup,
            processedAt = entity.processedAt,
        )

    fun toEntity(domain: ProcessedEvent): ProcessedEventJpaEntity =
        ProcessedEventJpaEntity.of(eventId = domain.eventId, consumerGroup = domain.consumerGroup)
}
