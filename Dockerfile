#FROM eclipse-temurin:17-jdk
#VOLUME /tmp
#COPY --from=build /target/app-0.0.1-SNAPSHOT.jar sheba-server.jar
#ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "sheba-server.jar"]

#FROM maven:3.8.5-openjdk-17 AS build
#COPY . .
#RUN mvn clean package -DskipTests
#
#FROM openjdk:17-oracle
#VOLUME /tmp
#COPY --from=build /target/app-0.0.1-SNAPSHOT.jar sheba-server.jar
#ENTRYPOINT ["java", "-jar", "sheba-server.jar"]

# Build stage
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM openjdk:17-oracle
WORKDIR /app
COPY --from=build /app/target/app-0.0.1-SNAPSHOT.jar sheba-server.jar
ENV MAIN_ADMIN_USERNAME=tal
ENV MAIN_ADMIN_PASSWORD=2024
ENV JWT_SECRET_KEY=bd279fb047279c287f52be609c28bf863e9699f44c5593a593a3fdcc2b37f0b7
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "sheba-server.jar"]