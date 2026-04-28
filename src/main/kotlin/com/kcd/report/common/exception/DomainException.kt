package com.kcd.report.common.exception

/**
 * 도메인 예외 베이스.
 *
 * [WHY] CLAUDE.md §5 — 컨트롤러/서비스가 던지는 예외는 모두 [ErrorCode]에 매핑.
 *       `GlobalExceptionHandler`가 [DomainException] 한 가지 타입으로 일괄 처리할 수 있도록
 *       모든 도메인 에러는 이 클래스를 상속한다.
 *
 * 외부 예외(IOException, DataAccessException 등)는 sys001 로 매핑되며, 이 계층에 속하지 않는다.
 */
abstract class DomainException(
    val errorCode: ErrorCode,
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message ?: errorCode.defaultMessage, cause)

// ── 인증 ────────────────────────────────────────────────────
class InvalidApiKeyException(
    message: String? = null,
) : DomainException(ErrorCode.AUTH_INVALID_API_KEY, message)

// ── 리포트 조회 ────────────────────────────────────────────
class ReportNotFoundException(
    registrationNumber: String,
) : DomainException(
        errorCode = ErrorCode.REPORT_NOT_FOUND,
        message = "리포트를 찾을 수 없습니다. (registrationNumber=$registrationNumber)",
    )

// ── 파이프라인 ─────────────────────────────────────────────
class DuplicateRequestException(
    registrationNumber: String,
) : DomainException(
        errorCode = ErrorCode.PIPELINE_DUPLICATE_REQUEST,
        message = "이미 진행 중인 요청이 있습니다. (registrationNumber=$registrationNumber)",
    )

class InvalidStatusTransitionException(
    from: String,
    to: String,
) : DomainException(
        errorCode = ErrorCode.PIPELINE_INVALID_TRANSITION,
        message = "상태 전이가 허용되지 않습니다. ($from → $to)",
    )

// ── 검증 ───────────────────────────────────────────────────
class InvalidRegistrationNumberException(
    value: String,
) : DomainException(
        errorCode = ErrorCode.VALID_INVALID_REG_NUMBER,
        message = "유효하지 않은 사업자번호입니다. (value=$value)",
    )
