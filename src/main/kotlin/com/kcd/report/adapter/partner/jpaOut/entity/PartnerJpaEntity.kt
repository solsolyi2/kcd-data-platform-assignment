package com.kcd.report.adapter.partner.jpaOut.entity

import com.kcd.report.adapter.common.jpaOut.entity.BaseJpaTimestampEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "partner")
class PartnerJpaEntity protected constructor(
    @Column(nullable = false, unique = true, length = 50)
    val code: String,
    @Column(nullable = false, length = 100)
    val name: String,
) : BaseJpaTimestampEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    companion object {
        fun of(code: String, name: String): PartnerJpaEntity = PartnerJpaEntity(code = code, name = name)
    }
}
