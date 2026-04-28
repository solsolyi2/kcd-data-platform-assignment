package com.kcd.report.adapter.outbox.jpaOut.repository

import com.kcd.report.adapter.outbox.jpaOut.entity.OutboxEventJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface OutboxEventJpaRepository : JpaRepository<OutboxEventJpaEntity, Long> {
    /**
     * [WHY] CLAUDE.md §4.1 — Poller 의 핵심 쿼리.
     *       FOR UPDATE SKIP LOCKED 로 다중 인스턴스에서도 동일 row 를 두 번 잡지 않는다.
     *       Partial index `idx_outbox_unprocessed` 와 짝.
     */
    @Query(
        value = """
            SELECT * FROM outbox_event
             WHERE processed_at IS NULL
             ORDER BY id ASC
             LIMIT :batchSize
             FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true,
    )
    fun findUnprocessedForUpdate(
        @Param("batchSize") batchSize: Int,
    ): List<OutboxEventJpaEntity>
}
