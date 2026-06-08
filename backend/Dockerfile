# ── Stage 1: build ────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /build

# Copy Maven wrapper and POM first for dependency layer caching
COPY diakonpay/.mvn/  .mvn/
COPY diakonpay/mvnw   diakonpay/pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source and build
COPY diakonpay/src/ src/
RUN ./mvnw package -DskipTests -q

# ── Stage 2: runtime ──────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
