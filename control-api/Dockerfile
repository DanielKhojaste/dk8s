# syntax=docker/dockerfile:1

# ---- Stage 1: build the jar with the Maven Wrapper ----
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/
# The cache mount keeps downloaded dependencies between builds, so rebuilds are fast.
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q -DskipTests package \
 && cp target/*.jar app.jar

# ---- Stage 2: a small runtime image with just Java and the jar ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# Run as an unprivileged user instead of root.
RUN adduser -S -u 1001 dk8s
COPY --from=build /app/app.jar app.jar
USER dk8s
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
