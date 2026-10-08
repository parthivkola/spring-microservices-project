# build context = repo root
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn -pl analytics-service -am package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/analytics-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java","-jar","app.jar"]
