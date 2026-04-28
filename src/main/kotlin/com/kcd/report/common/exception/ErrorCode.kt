package com.kcd.report.common.exception

import org.springframework.http.HttpStatus

/**
 * 도메인 에러 코드 카탈로그.
 *
 * [WHY] CLAUDE.md §5.2 — 도메인별 prefix 로 코드 일원화.
 *       `GlobalExceptionHandler` 에서 도메인 예외 → ErrorResponse 변환 매핑의 single source of truth.
 *
 * 새 코드 추가 시: prefix 충돌 없는지 확인하고, 영향받는 테스트 + README 에러 표 갱신.
 */
enum class ErrorCode(
    val code: String,
    val httpStatus: HttpStatus,
    val defaultMessage: String,
) {
    // ── 인증 (auth) ─────────────────────────────────────────
    AUTH_INVALID_API_KEY(
        "auth001",
        HttpStatus.UNAUTHORIZED,
        "유효하지 않은 API Key 입니다.",
    ),

    // ── 리포트 조회 (report) ────────────────────────────────
    REPORT_NOT_FOUND(
        "report001",
        HttpStatus.NOT_FOUND,
        "요청한 리포트를 찾을 수 없습니다.",
    ),

    // ── 파이프라인 (pipeline) ───────────────────────────────
    PIPELINE_DUPLICATE_REQUEST(
        "pipeline001",
        HttpStatus.CONFLICT,
        "이미 진행 중인 동일 사업자 요청이 있습니다.",
    ),
    PIPELINE_INVALID_TRANSITION(
        "pipeline002",
        HttpStatus.CONFLICT,
        "허용되지 않은 상태 전이입니다.",
    ),

    // ── 검증 (valid) ────────────────────────────────────────
    VALID_INVALID_REG_NUMBER(
        "valid001",
        HttpStatus.BAD_REQUEST,
        "유효하지 않은 사업자번호입니다.",
    ),
    VALID_REQUEST_BODY(
        "valid002",
        HttpStatus.BAD_REQUEST,
        "요청 본문이 유효하지 않습니다.",
    ),

    // ── 시스템 (sys) ────────────────────────────────────────
    SYS_INTERNAL(
        "sys001",
        HttpStatus.INTERNAL_SERVER_ERROR,
        "내부 오류가 발생했습니다.",
    ),
}
