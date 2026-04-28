package com.kcd.report.common.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

/**
 * MDC 트레이싱 필터.
 *
 * [WHY] CLAUDE.md §7.4 — 모든 요청 진입 시 traceId / registrationNumber / reportId 를 MDC 에 세팅.
 *       JSON 로그에서 한 요청을 식별할 수 있도록 한다.
 *
 * 클라이언트가 `X-Request-Id` 를 보내면 그것을 traceId 로 채택, 없으면 UUID 생성.
 * 응답 헤더에도 동일 값을 echo 하여 분산 환경에서 추적 가능하게 한다.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class MdcFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val traceId =
            request.getHeader(HEADER_REQUEST_ID)?.takeIf { it.isNotBlank() }
                ?: UUID.randomUUID().toString()

        try {
            MDC.put(MDC_TRACE_ID, traceId)
            response.setHeader(HEADER_REQUEST_ID, traceId)
            filterChain.doFilter(request, response)
        } finally {
            // [WHY] 스레드 풀 재사용으로 MDC 누수 방지.
            MDC.clear()
        }
    }

    companion object {
        const val HEADER_REQUEST_ID = "X-Request-Id"
        const val MDC_TRACE_ID = "traceId"
        const val MDC_REGISTRATION_NUMBER = "registrationNumber"
        const val MDC_REPORT_ID = "reportId"
    }
}
