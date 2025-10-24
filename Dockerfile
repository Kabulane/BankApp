# STAGE 1 — BUILD
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace
# On copie d'abord les pom.xml pour "chauffer" le cache Maven
COPY pom.xml ./
COPY backend/pom.xml backend/pom.xml
COPY backend/domain/pom.xml backend/domain/pom.xml
COPY backend/application/pom.xml backend/application/pom.xml
COPY backend/infrastructure/pom.xml backend/infrastructure/pom.xml
COPY backend/boot/pom.xml backend/boot/pom.xml
RUN mvn -B -q -e dependency:go-offline

# On ajoute ensuite les sources
COPY backend backend
ARG SKIP_TESTS=true
RUN mvn -B -e clean install -DskipTests=${SKIP_TESTS}

# STAGE 2 — RUNTIME
FROM eclipse-temurin:17-jre-alpine AS runtime
# Sécurité : utilisateur non-root
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app

# On ne copie que le JAR final — image minimale
COPY --from=builder /workspace/backend/boot/target/*.jar /app/app.jar

# Quelques variables utiles
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseZGC -XX:+ExitOnOutOfMemoryError" \
    SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --server.port=$SERVER_PORT"]
