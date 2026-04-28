package com.kcd.report.domain.pipeline.model

import com.kcd.report.common.exception.InvalidStatusTransitionException
import com.kcd.report.domain.pipeline.enums.ReportRequestStatus
import java.time.Instant

/**
 * 파이프라인 요청 + 상태머신 — 순수 도메인 모델 (Spring/JPA 어노테이션 없음).
 *
 * [WHY] CLAUDE.md §4.4 — 모든 상태 전이는 메서드로 캡슐화. 잘못된 전이 시
 *       [InvalidStatusTransitionException] (도메인 예외).
 */
class ReportRequest(
    val id: Long?,
    val registrationNumber: String,
    val requesterPartnerId: Long,
    var status: ReportRequestStatus = ReportRequestStatus.REQUESTED,
    var failureStage: ReportRequestStatus? = null,
    var failureReason: String? = null,
    var completedAt: Instant? = null,
    val createdAt: Instant? = null,
    var updatedAt: Instant? = null,
) {
    fun startCollecting() = transition(ReportRequestStatus.REQUESTED, ReportRequestStatus.COLLECTING)

    fun finishCollecting() = transition(ReportRequestStatus.COLLECTING, ReportRequestStatus.COLLECTED)

    fun startGenerating() = transition(ReportRequestStatus.COLLECTED, ReportRequestStatus.GENERATING)

    fun finishGenerating() = transition(ReportRequestStatus.GENERATING, ReportRequestStatus.GENERATED)

    fun startNotifying() = transition(ReportRequestStatus.GENERATED, ReportRequestStatus.NOTIFYING)

    fun complete() {
        transition(ReportRequestStatus.NOTIFYING, ReportRequestStatus.COMPLETED)
        completedAt = Instant.now()
    }

    /**
     * [WHY] At-Least-Once 환경에서 같은 실패가 두 번 도착해도 두 번째 호출을 안전 처리.
     *       종료 상태(COMPLETED/FAILED)면 무시.
     */
    fun fail(stage: ReportRequestStatus, reason: String) {
        if (status.isTerminal()) return
        failureStage = stage
        failureReason = reason
        status = ReportRequestStatus.FAILED
    }

    private fun transition(from: ReportRequestStatus, to: ReportRequestStatus) {
        if (status != from) throw InvalidStatusTransitionException(from = status.name, to = to.name)
        status = to
    }

    companion object {
        fun create(registrationNumber: String, requesterPartnerId: Long): ReportRequest =
            ReportRequest(id = null, registrationNumber = registrationNumber, requesterPartnerId = requesterPartnerId)
    }
}
