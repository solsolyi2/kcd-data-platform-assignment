package com.kcd.report

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

// [WHY] 단일 모듈 헥사고날 라이트 — CLAUDE.md §3.
//        - @EnableScheduling: OutboxPoller 가 @Scheduled로 실행되어야 함.
//        - @EnableAsync     : 조회 이력 적재 등 본 응답 트랜잭션과 분리 (CLAUDE.md §4.5).
@SpringBootApplication
@EnableScheduling
@EnableAsync
class ReportApplication

fun main(args: Array<String>) {
    runApplication<ReportApplication>(*args)
}
