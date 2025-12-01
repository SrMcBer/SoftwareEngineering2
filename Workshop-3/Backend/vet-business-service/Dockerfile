#
# Build stage
#
FROM amazoncorretto:24 AS builder
WORKDIR /app
COPY .gradle .gradle
COPY gradlew .
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src src
RUN chmod +x gradlew
RUN ./gradlew build -x test

#
# Package stage
#
FROM amazoncorretto:24 AS runner
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]