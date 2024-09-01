# Stage 1: Build the application
FROM maven:3-eclipse-temurin-21-alpine AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml ./
COPY src ./src

# Run Maven to build the project
RUN mvn clean package

# Stage 2: Run the application
FROM openjdk:21-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/target/vs-challenge-0.0.1-SNAPSHOT.jar /app/vs-challenge.jar

# Expose port 8080
EXPOSE 8080

# Define the command to run the application
CMD ["java", "-jar", "/app/vs-challenge.jar"]
