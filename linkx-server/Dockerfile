FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml ./
COPY linkx-server/pom.xml ./linkx-server/pom.xml
COPY linkx-server/src ./linkx-server/src

RUN mvn -pl linkx-server -am clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

ENV TZ=Asia/Shanghai

COPY --from=builder /build/linkx-server/target/*.jar /app/linkx-server.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/linkx-server.jar"]
