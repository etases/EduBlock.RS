# BUILD

FROM maven:3.8.6-eclipse-temurin-17-alpine AS build

WORKDIR /project/app

COPY src src

COPY pom.xml .

COPY config.yml .

RUN mvn clean package

# DEPLOY

FROM gcr.io/distroless/java17-debian11:latest

WORKDIR /project/app

COPY --from=build /project/app/target/EduBlock.RS.jar edublock-rs.jar

COPY --from=build /project/app/config.yml config.yml

EXPOSE 7070

ENTRYPOINT [ "java", "-jar", "/project/app/edublock-rs.jar" ]
