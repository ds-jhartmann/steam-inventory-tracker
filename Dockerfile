FROM maven:3-eclipse-temurin-21-alpine AS maven

WORKDIR /build

COPY pom.xml .

COPY src src

# the --mount option requires BuildKit.
RUN --mount=type=cache,target=/root/.m2 mvn -B clean package -am -DskipTests


# Copy the jar and build image
FROM eclipse-temurin:21-jre-alpine AS steam-inventory-tracker

ARG UID=10000
ARG GID=1000

WORKDIR /app

COPY --chmod=755 --from=maven /build/target/steam-inventory-tracker-*.jar app.jar

USER ${UID}:${GID}

ENTRYPOINT ["java", "-jar", "app.jar"]
