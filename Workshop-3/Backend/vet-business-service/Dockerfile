# -----------------------------------------------------------------
# STAGE 1: THE BUILD STAGE (JDK)
# Using stable Java 21 LTS from Eclipse Temurin
# -----------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the Gradle configuration files first.
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Copy source code later
COPY src src

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the application
RUN ./gradlew bootJar --no-daemon

# -----------------------------------------------------------------
# STAGE 2: THE RUNTIME STAGE (JRE)
# Using the slim Alpine JRE for minimal production image size.
# -----------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS run

# Set the working directory
WORKDIR /app

# Copy the built JAR from the 'build' stage.
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]