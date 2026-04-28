package com.kcd.report.common.response

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * API 공통 응답 포맷.
 *
 * [WHY] CLAUDE.md §5.1 — 성공/실패 응답을 한 가지 sealed 타입으로 통일하여,
 *       컨트롤러는 항상 [ApiResponse]만 반환하고, 클라이언트는 `success` 필드 하나로 분기 가능.
 *
 * JSON 직렬화 시 `null` 필드는 생략 → 성공: `{ "success": true, "data": ... }`,
 * 실패: `{ "success": false, "error": ... }`.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
sealed class ApiResponse<out T>(
    val success: Boolean,
) {
    class Success<T>(
        val data: T?,
    ) : ApiResponse<T>(success = true)

    class Failure(
        val error: ErrorPayload,
    ) : ApiResponse<Nothing>(success = false)

    companion object {
        fun <T> ok(data: T): ApiResponse<T> = Success(data)

        fun ok(): ApiResponse<Unit> = Success(null)

        fun fail(error: ErrorPayload): ApiResponse<Nothing> = Failure(error)

        fun fail(
            code: String,
            message: String,
        ): ApiResponse<Nothing> = Failure(ErrorPayload(code = code, message = message))
    }
}
