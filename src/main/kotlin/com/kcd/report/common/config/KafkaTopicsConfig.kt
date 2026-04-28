package com.kcd.report.common.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

/**
 * Kafka 토픽 명시적 선언.
 *
 * [WHY] docker-compose 의 KAFKA_AUTO_CREATE_TOPICS_ENABLE=false — 의도치 않은 토픽 자동 생성 방지.
 *       모든 토픽은 코드에서 [NewTopic] 빈으로 선언, KafkaAdmin 이 부팅 시 보장 생성.
 *
 * 명명 규약 (CLAUDE.md §7.2):
 *   report.requested → report.collected → report.generated → report.notify
 *
 * 파티션 수:
 *   - 동일 사업자 이벤트는 [registrationNumber] 키로 같은 파티션에 떨어진다 (§4.2 순서 보장).
 *   - 단일 노드 / 단일 ISR 환경 → replicas=1.
 *   - partitions=3 — 단일 사업자 토픽이 한 파티션에 몰리는 비대칭은 트레이드오프.
 *     (운영에서는 사업자 분포에 맞춰 파티션 수와 키 해싱 재검토.)
 */
@Configuration
class KafkaTopicsConfig {
    @Bean
    fun topicReportRequested(): NewTopic =
        TopicBuilder
            .name(TOPIC_REPORT_REQUESTED)
            .partitions(DEFAULT_PARTITIONS)
            .replicas(DEFAULT_REPLICAS)
            .build()

    @Bean
    fun topicReportCollected(): NewTopic =
        TopicBuilder
            .name(TOPIC_REPORT_COLLECTED)
            .partitions(DEFAULT_PARTITIONS)
            .replicas(DEFAULT_REPLICAS)
            .build()

    @Bean
    fun topicReportGenerated(): NewTopic =
        TopicBuilder
            .name(TOPIC_REPORT_GENERATED)
            .partitions(DEFAULT_PARTITIONS)
            .replicas(DEFAULT_REPLICAS)
            .build()

    @Bean
    fun topicReportNotify(): NewTopic =
        TopicBuilder
            .name(TOPIC_REPORT_NOTIFY)
            .partitions(DEFAULT_PARTITIONS)
            .replicas(DEFAULT_REPLICAS)
            .build()

    companion object {
        const val TOPIC_REPORT_REQUESTED = "report.requested"
        const val TOPIC_REPORT_COLLECTED = "report.collected"
        const val TOPIC_REPORT_GENERATED = "report.generated"
        const val TOPIC_REPORT_NOTIFY = "report.notify"

        private const val DEFAULT_PARTITIONS = 3
        private const val DEFAULT_REPLICAS = 1
    }
}
