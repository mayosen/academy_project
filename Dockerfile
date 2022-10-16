FROM openjdk:17-alpine
COPY build/libs/*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]
