# =====================================================================
# Production Dockerfile for CMS Spring Boot Backend on Render
# =====================================================================

# Stage 1: Build the backend JAR
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

# Tell settings.gradle.kts to only include :backend (ignores Android SDK requirements)
ENV BUILD_BACKEND_ONLY=true

# Copy Gradle files
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Copy backend module source
COPY backend backend

# Build the Spring Boot executable fat JAR
RUN chmod +x gradlew && ./gradlew :backend:bootJar --no-daemon -x test

# Stage 2: Minimal JRE runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the generated JAR
COPY --from=builder /workspace/backend/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8000
EXPOSE 8000

# Start the Spring Boot backend
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
