package com.kcd.report.domain.pipeline.enums

/**
 * 파이프라인 상태머신 — CLAUDE.md §4.4.
 *
 * REQUESTED → COLLECTING → COLLECTED → GENERATING → GENERATED → NOTIFYING → COMPLETED
 *      └─→ FAILED ←──────┴──────────────┴─────────────┴──────────────┘
 */
enum class ReportRequestStatus {
    REQUESTED,
    COLLECTING,
    COLLECTED,
    GENERATING,
    GENERATED,
    NOTIFYING,
    COMPLETED,
    FAILED,
    ;

    fun isTerminal(): Boolean = this == COMPLETED || this == FAILED
}
