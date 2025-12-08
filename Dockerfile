## ===========================================
## Multi-stage Dockerfile for Vehicle Service
## ===========================================
#
## ============================================
## Stage 1: Build Stage
## ============================================
#FROM eclipse-temurin:17-jdk-alpine AS build
#WORKDIR /build
#
## Install Maven
#RUN apk add --no-cache maven
#
## Copy Maven configuration
#COPY pom.xml .
#
## Download dependencies (cached layer)
#RUN mvn dependency:go-offline -B
#
## Copy source code
#COPY src ./src
#
## Build the application (skip tests for faster builds)
#RUN mvn clean package -DskipTests
#
## ============================================
## Stage 2: Runtime Stage
## ============================================
#FROM eclipse-temurin:17-jre-alpine
#WORKDIR /app
#
## Install wget for health checks
#RUN apk add --no-cache wget
#
## Create non-root user for security
#RUN addgroup -S spring && adduser -S spring -G spring
#
## Copy JAR from build stage
#COPY --from=build /build/target/*.jar app.jar
#
## Create directories with proper permissions
#RUN mkdir -p /app/documents /app/logs && \
#    chown -R spring:spring /app
#
## Switch to non-root user
#USER spring:spring
#
## Expose port
#EXPOSE 8030
#
## Health check configuration
#HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
#  CMD wget --no-verbose --tries=1 --spider http://localhost:8030/api/v1/actuator/health || exit 1
#
## JVM optimization flags
#ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"
#
## Run the application
#ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
FROM  eclipse-temurin:17-jdk-alpine AS build
WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Copy Maven configuration
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
RUN mvn clean package -DskipTests

# ============================================
# Stage 2: Runtime Stage
# ============================================
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install wget for health checks
# Create non-root user for security
RUN apk add --no-cache wget && addgroup -S spring && adduser -S spring -G spring

# Copy JAR from build stage
COPY --from=build /build/target/*.jar app.jar

# Create directories with proper permissions (including uploaded-files)
RUN mkdir -p /app/documents /app/logs /uploaded-files/photos /uploaded-files/docs /uploaded-files/pdfs && \
    chown -R spring:spring /app /uploaded-files

# Switch to non-root user
USER spring:spring

# Expose port
EXPOSE 8030

## Set environment variable inside container
ENV SPRING_DATASOURCE_URL=jdbc:mysql://103.211.202.187:3306/adminmylstech_auto_adda_vehicle?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
ENV SPRING_DATASOURCE_USERNAME=adminmylstech_user
ENV SPRING_DATASOURCE_PASSWORD=Mylstech@100

# Health check configuration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8030/api/v1/actuator/health || exit 1

# JVM optimization flags
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]