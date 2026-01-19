# Stage 1: Build the application
FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file from the build stage
COPY --from=build /app/target/events-service-*.jar app.jar

# Expose the application port
EXPOSE 8082

# Set the entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]

