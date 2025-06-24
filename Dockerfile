# Use an official OpenJDK 17 image as base
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR file into the image
COPY target/imageprocessor-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app runs on (default Spring Boot = 8080)
EXPOSE 8080

# Run the JAR
ENTRYPOINT ["java", "-jar", "app.jar"]