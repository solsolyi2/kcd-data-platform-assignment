package com.kcd.report.common.config

import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskDecorator
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.ThreadPoolExecutor

/**
 * 비동기 실행기 정의.
 *
 * [WHY] CLAUDE.md §4.5 — 조회 이력 적재(`@Async`) 같은 부가 작업은 본 응답 트랜잭션과
 *       분리되어야 한다. 전용 풀로 격리해 메인 요청 처리 스레드를 점유하지 않게 한다.
 *
 * [TRADEOFF] CallerRunsPolicy → 풀이 가득 차면 호출 스레드(=요청 스레드)에서 실행.
 *            요청 응답이 살짝 느려져도 audit 로그는 유실되지 않는 쪽을 택했다.
 */
@Configuration
class AsyncConfig {
    @Bean(name = [QUERY_LOG_EXECUTOR])
    fun queryLogExecutor(): TaskExecutor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 2
            maxPoolSize = 8
            queueCapacity = 100
            setThreadNamePrefix("querylog-")
            // [WHY] 요청 스레드의 MDC(traceId 등)를 백그라운드 스레드에서도 유지하기 위해.
            setTaskDecorator(MdcCopyingTaskDecorator())
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }

    companion object {
        const val QUERY_LOG_EXECUTOR = "queryLogExecutor"
    }
}

/**
 * 호출 스레드의 MDC 컨텍스트를 비동기 작업 스레드로 복사.
 */
private class MdcCopyingTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        val contextMap = MDC.getCopyOfContextMap()
        return Runnable {
            val previous = MDC.getCopyOfContextMap()
            try {
                if (contextMap != null) MDC.setContextMap(contextMap) else MDC.clear()
                runnable.run()
            } finally {
                if (previous != null) MDC.setContextMap(previous) else MDC.clear()
            }
        }
    }
}
