package com.kcd.report.adapter.report.jpaOut.entity

import com.kcd.report.adapter.common.jpaOut.entity.BaseJpaTimestampEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "business_report")
class BusinessReportJpaEntity protected constructor(
    @Column(name = "registration_number", nullable = false, unique = true, length = 10)
    val registrationNumber: String,
    @Column(name = "generated_at", nullable = false)
    val generatedAt: Instant,
) : BaseJpaTimestampEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(registrationNumber: String, generatedAt: Instant): BusinessReportJpaEntity =
            BusinessReportJpaEntity(registrationNumber = registrationNumber, generatedAt = generatedAt)
    }
}
