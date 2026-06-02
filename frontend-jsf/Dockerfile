FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY frontend-jsf/pom.xml ./pom.xml
RUN mvn -q -B dependency:go-offline -DskipTests || true
COPY frontend-jsf/src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/target/gestao-fiscal-jsf-*.war ./app.war
COPY shared/theme ./shared/theme
COPY frontend-jsf/docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh
EXPOSE 8081
HEALTHCHECK --interval=15s --timeout=10s --retries=24 --start-period=180s \
  CMD curl -sf http://127.0.0.1:8081/actuator/health | grep -q UP
ENTRYPOINT ["/docker-entrypoint.sh"]
