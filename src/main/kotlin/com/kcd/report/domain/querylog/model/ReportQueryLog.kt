package com.kcd.report.domain.querylog.model

import java.time.Instant

class ReportQueryLog(
    val id: Long?,
    val partnerId: Long,
    val registrationNumber: String,
    val responseStatus: Int,
    val queriedAt: Instant = Instant.now(),
    val createdAt: Instant? = null,
) {
    companion object {
        fun create(partnerId: Long, registrationNumber: String, responseStatus: Int): ReportQueryLog =
            ReportQueryLog(
                id = null,
                partnerId = partnerId,
                registrationNumber = registrationNumber,
                responseStatus = responseStatus,
            )
    }
}
