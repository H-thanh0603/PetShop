# Multi-stage build: compile the bootable WAR then run it on a slim JRE.
FROM gradle:8.14-jdk21 AS build
WORKDIR /workspace
COPY settings.gradle* build.gradle gradlew ./
COPY gradle ./gradle
COPY src ./src
RUN gradle bootWar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/build/libs/petshop-boot.war app.war
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
