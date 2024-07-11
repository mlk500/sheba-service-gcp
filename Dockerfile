FROM eclipse-temurin:17-jdk
VOLUME /tmp
COPY --from=build /target/app-0.0.1-SNAPSHOT.jar sheba-server.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "sheba-server.jar"]