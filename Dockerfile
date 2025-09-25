## Use Java 17 to match your pom.xml (not 21)
#FROM eclipse-temurin:17-jdk-alpine
#
## Set working directory inside container
#WORKDIR /app
#
## Copy the Spring Boot executable JAR with correct artifact name
#COPY target/VehicleApi-0.0.1-SNAPSHOT.jar app.jar
#
## Expose the default port
#EXPOSE 8030
## Set environment variable inside container
#ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/auto_adda_vehicle?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true
#
## Run the app
#ENTRYPOINT ["java", "-jar", "/app/app.jar"]


FROM eclipse-temurin:17-jdk-focal AS builder
WORKDIR /build

# Install Maven
RUN apt-get update && apt-get install -y maven


# Copy pom and download dependencies offline
COPY pom.xml .
RUN mvn dependency:go-offline -B -DskipTests


# Copy source and build
COPY src/ src/
RUN mvn clean package -DskipTests \
    && mv target/*.jar target/app.jar

# Step 2: Run stage
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Copy built jar
COPY --from=builder /build/target/app.jar .

EXPOSE 8030

ENTRYPOINT ["java", "-jar", "app.jar"]
