FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY gnx-ai-training-domain/pom.xml gnx-ai-training-domain/pom.xml
COPY gnx-ai-training-core/pom.xml gnx-ai-training-core/pom.xml
COPY gnx-ai-training-infrastructure/pom.xml gnx-ai-training-infrastructure/pom.xml
COPY gnx-ai-training-api/pom.xml gnx-ai-training-api/pom.xml
COPY gnx-ai-training-app/pom.xml gnx-ai-training-app/pom.xml
COPY gnx-ai-training-domain/src gnx-ai-training-domain/src
COPY gnx-ai-training-core/src gnx-ai-training-core/src
COPY gnx-ai-training-infrastructure/src gnx-ai-training-infrastructure/src
COPY gnx-ai-training-api/src gnx-ai-training-api/src
COPY gnx-ai-training-app/src gnx-ai-training-app/src
RUN mvn -pl gnx-ai-training-app -am clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/gnx-ai-training-app/target/gnx-ai-training-app-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
