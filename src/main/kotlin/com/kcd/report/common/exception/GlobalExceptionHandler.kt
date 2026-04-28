package com.kcd.report.common.exception

import com.kcd.report.common.response.ApiResponse
import com.kcd.report.common.response.ErrorPayload
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

/**
 * 전역 예외 핸들러.
 *
 * [WHY] CLAUDE.md §5.2 — 도메인 예외를 [ApiResponse.Failure] 로 변환하는 단일 진입점.
 *       서비스/컨트롤러가 try/catch 로 응답 포맷팅을 하지 않도록 한다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    // ── 도메인 예외 ──────────────────────────────────────────
    @ExceptionHandler(DomainException::class)
    fun handleDomain(ex: DomainException): ResponseEntity<ApiResponse<Nothing>> {
        // [WHY] 도메인 예외는 비즈니스 시그널 — INFO 또는 WARN. 스택트레이스 X.
        log.warn(
            "domain exception: code={} message={}",
            ex.errorCode.code,
            ex.message,
        )
        return ResponseEntity
            .status(ex.errorCode.httpStatus)
            .body(
                ApiResponse.fail(
                    ErrorPayload(
                        code = ex.errorCode.code,
                        message = ex.message ?: ex.errorCode.defaultMessage,
                    ),
                ),
            )
    }

    // ── @Valid 실패 ──────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        val detail =
            ex.bindingResult.fieldErrors.joinToString("; ") {
                "${it.field}: ${it.defaultMessage}"
            }
        log.warn("request validation failed: {}", detail)
        val code = ErrorCode.VALID_REQUEST_BODY
        return ResponseEntity
            .status(code.httpStatus)
            .body(
                ApiResponse.fail(
                    ErrorPayload(
                        code = code.code,
                        message = if (detail.isBlank()) code.defaultMessage else detail,
                    ),
                ),
            )
    }

    // ── 잘못된 파라미터 / 타입 / 누락 ────────────────────────
    @ExceptionHandler(
        MissingServletRequestParameterException::class,
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class,
    )
    fun handleBadRequest(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("bad request: {}", ex.message)
        val code = ErrorCode.VALID_REQUEST_BODY
        return ResponseEntity
            .status(code.httpStatus)
            .body(
                ApiResponse.fail(
                    ErrorPayload(
                        code = code.code,
                        message = ex.message ?: code.defaultMessage,
                    ),
                ),
            )
    }

    // ── 기타 모든 예외 → sys001 ──────────────────────────────
    @ExceptionHandler(Exception::class)
    fun handleUnknown(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
        // [WHY] 정상적이라면 닿으면 안 되는 분기. ERROR + 스택트레이스로 즉시 인지.
        log.error("unhandled exception", ex)
        val code = ErrorCode.SYS_INTERNAL
        return ResponseEntity
            .status(code.httpStatus)
            .body(
                ApiResponse.fail(
                    ErrorPayload(
                        code = code.code,
                        message = code.defaultMessage,
                    ),
                ),
            )
    }
}
