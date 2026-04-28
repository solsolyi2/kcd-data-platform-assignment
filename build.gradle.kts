// [WHY] Spring Boot 3.5.x + Kotlin 1.9.25 + JDK 21.
//        CLAUDE.md §2 고정 스택 — 임의 변경 금지.
plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    id("org.springframework.boot") version "3.5.14"
    id("io.spring.dependency-management") version "1.1.7"
    // [WHY] CLAUDE.md §7.1 — Kotlin 코드 스타일 강제. 빌드/CI에서 자동 검사.
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "com.kcd"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // ── Spring Boot 코어 ─────────────────────────────────────
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // ── 메시징 / 영속성 ─────────────────────────────────────
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    // ── Kotlin / Jackson ────────────────────────────────────
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // ── 로깅 (JSON) ─────────────────────────────────────────
    // [WHY] CLAUDE.md §7.4 — JSON 포맷 + MDC. logstash-logback-encoder가 사실상 표준.
    implementation("net.logstash.logback:logstash-logback-encoder:7.4")

    // ── 테스트 ─────────────────────────────────────────────
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        // [WHY] Kotest assertions 사용 위해 mockito-core는 필요하지만,
        //        AssertJ/Hamcrest는 충돌 없음. 별도 제외 X.
    }
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.kafka:spring-kafka-test")

    // [WHY] CLAUDE.md §2 — JUnit 5 + Kotest assertions + Testcontainers 명시.
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.kotest.extensions:kotest-assertions-spring:1.1.3")

    // ── Testcontainers ─────────────────────────────────────
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:kafka")
    testImplementation("org.testcontainers:postgresql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        // [WHY] JSR-305 strict 모드 — Spring 의 @NonNull 어노테이션을 Kotlin null-safety로 인식.
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

// [WHY] JPA Entity는 final이면 Lazy Loading 프록시가 작동하지 않음.
//        kotlin-jpa 플러그인이 no-arg 생성자를 자동 생성, allOpen이 final 제거.
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // [WHY] Testcontainers 가 docker-in-docker / colima 환경에서도 안정 동작.
    systemProperty("testcontainers.reuse.enable", "true")
}

// [WHY] CLAUDE.md §7.1 ktlint — Kotlin 공식 스타일 + 우리 컨벤션.
ktlint {
    version.set("1.3.1")
    android.set(false)
    ignoreFailures.set(false)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}
