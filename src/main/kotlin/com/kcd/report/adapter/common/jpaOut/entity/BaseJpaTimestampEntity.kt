package com.kcd.report.adapter.common.jpaOut.entity

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

/**
 * created_at / updated_at 공통 컬럼 — JPA 어댑터 전용 추상 클래스.
 *
 * [WHY] 도메인 모델은 순수 Kotlin (createdAt/updatedAt 도 단순 Instant?). 영속화 시점에
 *       Hibernate 가 채우는 메커니즘은 어댑터 책임. 이 클래스는 adapter/.../jpaOut/entity 전용.
 */
@MappedSuperclass
abstract class BaseJpaTimestampEntity {
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.EPOCH
        protected set

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.EPOCH
        protected set
}
