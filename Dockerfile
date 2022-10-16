FROM gradle:7.5-jdk17 AS builder
WORKDIR /app
COPY . /app
CMD gradle clean bootJar --no-daemon

FROM openjdk:17-alpine
COPY --from=builder app/build/libs/*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]

