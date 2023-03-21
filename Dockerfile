FROM maven:3-openjdk-17-slim AS maven

WORKDIR /build

COPY pom.xml .

COPY src src

# the --mount option requires BuildKit.
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -am -DskipTests


# Copy the jar and build image
FROM eclipse-temurin:19-jre-alpine AS irs-api

RUN apk upgrade

ARG UID=10000
ARG GID=1000

WORKDIR /app

COPY --chmod=755 --from=maven /build/target/steam-inventory-tracker-*-exec.jar app.jar

USER ${UID}:${GID}

ENTRYPOINT ["java", "-Djava.util.logging.config.file=./logging.properties", "-jar", "app.jar"]
