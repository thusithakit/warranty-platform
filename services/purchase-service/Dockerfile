
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN groupadd --system spring \
    && useradd --system --gid spring spring

COPY --from=builder /app/target/purchase-service-0.0.1-SNAPSHOT.jar app.jar

RUN chown spring:spring app.jar

USER spring

EXPOSE 4003

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
