FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl netcat-openbsd
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=5s --retries=24 --start-period=150s \
  CMD curl -sf http://localhost:8080/actuator/health/liveness | grep -q '"status":"UP"' || curl -sf http://localhost:8080/actuator/health | grep -q UP
ENTRYPOINT ["/docker-entrypoint.sh"]
