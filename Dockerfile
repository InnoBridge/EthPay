FROM maven:3.9.7-sapmachine-22 AS build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:22-slim
COPY --from=build /target/ethpay-0.0.1-SNAPSHOT.jar  app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Dhttps.protocols=TLSv1.2,TLSv1.3", "-Djdk.tls.client.protocols=TLSv1.2,TLSv1.3", "-Djavax.net.debug=ssl,handshake", "-jar", "app.jar"]
