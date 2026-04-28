package com.kcd.report.common.response

/**
 * 실패 응답 본문의 `error` 필드.
 *
 * [WHY] CLAUDE.md §5.1 — 모든 실패 응답은 `{ success: false, error: { code, message } }` 형태로 통일.
 */
data class ErrorPayload(
    val code: String,
    val message: String,
)
