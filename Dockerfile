# ──────────────────────────────────────────────────────────────
# Multi-stage build — Gradle build → JRE 실행 이미지
# [WHY] 빌드 환경(JDK + Gradle)을 런타임 이미지에서 제거 → 이미지 슬림화.
# ──────────────────────────────────────────────────────────────

# ── Stage 1: build ──
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# [WHY] 의존성 그래프만 먼저 복사 → Docker 레이어 캐시 적중률 ↑
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 소스 복사 후 부트 jar 빌드
COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

# ── Stage 2: runtime ──
FROM eclipse-temurin:21-jre-alpine

# [WHY] 비루트 실행 — 컨테이너 보안 권장.
RUN addgroup -S app && adduser -S app -G app
USER app

WORKDIR /app
COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

# [WHY] PID 1 시그널 처리 — Spring Boot graceful shutdown 보장.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
