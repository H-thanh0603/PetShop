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
# Run as a non-root user; /app/uploads must be writable for product/user images.
# A named volume mounted at /app/uploads inherits this ownership on first use.
RUN useradd --system --uid 1001 appuser \
    && mkdir -p /app/uploads \
    && chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.war"]
