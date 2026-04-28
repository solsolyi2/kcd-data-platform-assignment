package com.kcd.report.adapter.report.jpaOut.repository

import com.kcd.report.adapter.report.jpaOut.entity.BusinessReportJpaEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BusinessReportJpaRepository : JpaRepository<BusinessReportJpaEntity, Long> {
    fun findByRegistrationNumber(registrationNumber: String): BusinessReportJpaEntity?

    fun existsByRegistrationNumber(registrationNumber: String): Boolean
}
