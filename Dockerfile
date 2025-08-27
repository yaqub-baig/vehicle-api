# Use Java 17 to match your pom.xml (not 21)
FROM eclipse-temurin:17-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy the Spring Boot executable JAR with correct artifact name
COPY target/VehicleApi-0.0.1-SNAPSHOT.jar app.jar

# Expose the default port
EXPOSE 8030
# Set environment variable inside container
ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/auto_adda_vehicle?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true

# Run the app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]