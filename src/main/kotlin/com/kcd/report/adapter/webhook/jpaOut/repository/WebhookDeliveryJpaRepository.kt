package com.kcd.report.adapter.webhook.jpaOut.repository

import com.kcd.report.adapter.webhook.jpaOut.entity.WebhookDeliveryJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface WebhookDeliveryJpaRepository : JpaRepository<WebhookDeliveryJpaEntity, Long> {
    fun findAllByReportId(reportId: Long): List<WebhookDeliveryJpaEntity>

    fun findAllByPartnerId(partnerId: Long): List<WebhookDeliveryJpaEntity>
}
