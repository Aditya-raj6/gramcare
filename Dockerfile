# Multi-stage build shared by every Spring Boot microservice.
# The build stage is identical for all services, so Docker builds it once and caches it.
#   docker build --build-arg MODULE=patient-service -t healthharbor/patient-service .
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY hh-common/pom.xml hh-common/
COPY discovery-server/pom.xml discovery-server/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY patient-service/pom.xml patient-service/
COPY healthcenter-service/pom.xml healthcenter-service/
COPY appointment-service/pom.xml appointment-service/
COPY inventory-service/pom.xml inventory-service/
COPY healthcamp-service/pom.xml healthcamp-service/
COPY sanitation-service/pom.xml sanitation-service/
COPY water-service/pom.xml water-service/
COPY dashboard-service/pom.xml dashboard-service/
RUN mvn -q -B dependency:go-offline -DexcludeArtifactIds=hh-common || true
COPY . .
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:21-jre
ARG MODULE
WORKDIR /app
COPY --from=build /workspace/${MODULE}/target/${MODULE}-1.0.0.jar app.jar
ENV JAVA_OPTS="-Xms64m -Xmx256m -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
