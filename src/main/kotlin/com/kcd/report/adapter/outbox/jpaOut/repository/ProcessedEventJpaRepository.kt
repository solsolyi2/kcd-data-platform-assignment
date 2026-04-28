package com.kcd.report.adapter.outbox.jpaOut.repository

import com.kcd.report.adapter.outbox.jpaOut.entity.ProcessedEventJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProcessedEventJpaRepository : JpaRepository<ProcessedEventJpaEntity, Long> {
    fun existsByEventIdAndConsumerGroup(eventId: UUID, consumerGroup: String): Boolean
}
