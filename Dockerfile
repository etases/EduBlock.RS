# BUILD

FROM maven:3.8.6-eclipse-temurin-17-alpine AS build

WORKDIR /context

COPY src src

COPY pom.xml .

RUN mvn -e clean package
 
# DEPLOY

FROM gcr.io/distroless/java17-debian11:latest

WORKDIR /jar

COPY --from=build /context/target/EduBlock.RS.jar edublock-rs.jar

WORKDIR /data

VOLUME ["/data"]

EXPOSE 7070

ENTRYPOINT [ "java", "-DRS_CONFIG_USE_SYSTEM=true", "-jar", "/jar/edublock-rs.jar" ]
