FROM openjdk:18-alpine
COPY build/libs/*.jar application.jar
EXPOSE 80
ENTRYPOINT ["java", "-jar", "application.jar"]
