# Use the official Java image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/my-spring-boot-app-0.0.1.jar /app

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "my-spring-boot-app-0.0.1.jar"]

