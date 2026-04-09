FROM eclipse-temurin:25-jdk-alpine
LABEL authors="Cody Zellmer-Johnson"
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]