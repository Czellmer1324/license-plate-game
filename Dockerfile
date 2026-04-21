FROM eclipse-temurin:25-jdk as deps
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./

RUN ./gradlew dependencies --no-daemon

FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app
COPY --from=deps /root/.gradle /root/.gradle
COPY . .

RUN ./gradlew clean build -x test --no-daemon


FROM eclipse-temurin:25-jre
WORKDIR /app

LABEL authors="Cody Zellmer-Johnson"
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]