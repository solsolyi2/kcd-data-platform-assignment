package com.kcd.report.adapter.report.jpaOut.repository

import com.kcd.report.adapter.report.jpaOut.entity.ReportMetricJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ReportMetricJpaRepository : JpaRepository<ReportMetricJpaEntity, Long> {
    /**
     * [WHY] N+1 방지 — 한 리포트의 metric 전체를 단일 쿼리로 fetch.
     *       UNIQUE(report_id, metric_type) 의 prefix 인덱스 활용.
     */
    fun findAllByReportId(reportId: Long): List<ReportMetricJpaEntity>
}
