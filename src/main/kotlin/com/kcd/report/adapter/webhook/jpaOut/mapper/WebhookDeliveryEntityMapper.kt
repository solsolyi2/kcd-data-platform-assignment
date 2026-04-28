package com.kcd.report.adapter.webhook.jpaOut.mapper

import com.kcd.report.adapter.webhook.jpaOut.entity.WebhookDeliveryJpaEntity
import com.kcd.report.domain.webhook.model.WebhookDelivery

object WebhookDeliveryEntityMapper {
    fun toDomain(entity: WebhookDeliveryJpaEntity): WebhookDelivery =
        WebhookDelivery(
            id = entity.id,
            partnerId = entity.partnerId,
            reportId = entity.reportId,
            endpointUrl = entity.endpointUrl,
            status = entity.status,
            attemptCount = entity.attemptCount,
            lastError = entity.lastError,
            lastAttemptAt = entity.lastAttemptAt,
            succeededAt = entity.succeededAt,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt,
        )

    fun toEntity(domain: WebhookDelivery): WebhookDeliveryJpaEntity =
        WebhookDeliveryJpaEntity.of(
            partnerId = domain.partnerId,
            reportId = domain.reportId,
            endpointUrl = domain.endpointUrl,
            status = domain.status,
            attemptCount = domain.attemptCount,
            lastError = domain.lastError,
            lastAttemptAt = domain.lastAttemptAt,
            succeededAt = domain.succeededAt,
        )
}
