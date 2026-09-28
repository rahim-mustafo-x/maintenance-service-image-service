# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy AS build

WORKDIR /build

COPY --chmod=0755 mvnw mvnw
COPY .mvn/ .mvn/
COPY pom.xml pom.xml

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline -DskipTests

COPY src/ src/

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests && \
    mv target/$(./mvnw help:evaluate \
        -Dexpression=project.artifactId \
        -q \
        -DforceStdout)-$(./mvnw help:evaluate \
        -Dexpression=project.version \
        -q \
        -DforceStdout).jar target/app.jar


FROM eclipse-temurin:17-jre-jammy AS final

ARG UID=10001

RUN adduser \
    --disabled-password \
    --gecos "" \
    --home "/nonexistent" \
    --shell "/sbin/nologin" \
    --no-create-home \
    --uid "${UID}" \
    appuser

WORKDIR /app

COPY --from=build /build/target/app.jar app.jar

USER appuser

EXPOSE 7878

ENTRYPOINT ["java", "-jar", "app.jar"]