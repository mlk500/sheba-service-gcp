#FROM eclipse-temurin:17-jdk
#VOLUME /tmp
#COPY --from=build /target/app-0.0.1-SNAPSHOT.jar sheba-server.jar
#ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "sheba-server.jar"]

FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-oracle
VOLUME /tmp
COPY --from=build /target/app-0.0.1-SNAPSHOT.jar sheba-server.jar
ENTRYPOINT ["java", "-jar", "sheba-server.jar"]