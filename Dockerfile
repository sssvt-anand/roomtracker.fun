# Build stage
FROM openjdk:8-jdk-alpine AS build
WORKDIR /app

# Install wget to download Maven
RUN apk add --no-cache wget

# Download and install Maven 3.9.6 (or another compatible version)
RUN wget https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz -O /tmp/maven.tar.gz && \
    tar xzvf /tmp/maven.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.6 /opt/maven && \
    rm /tmp/maven.tar.gz

# Set Maven environment variables
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

# Copy and build the project
COPY pom.xml .
COPY src ./src
RUN mvn clean package

# Run stage
FROM openjdk:8-jdk-alpine
VOLUME /tmp
WORKDIR /app
COPY --from=build /app/target/roomexpenseapp-0.0.1-SNAPSHOT.jar roomtrackerbackend.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "roomtrackerbackend.jar"]