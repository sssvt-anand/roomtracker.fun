# Build stage
FROM openjdk:8-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apk add --no-cache maven && mvn clean package

# Run stage
FROM openjdk:8-jdk-alpine
VOLUME /tmp
WORKDIR /app
COPY --from=build /app/target/roomexpenseapp-0.0.1-SNAPSHOT.jar roomtrackerbackend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "roomtrackerbackend.jar"]