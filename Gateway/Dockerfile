#
# Build stage
#
FROM maven:3.9-eclipse-temurin-17-alpine AS build
COPY src /home/app/src
COPY pom.xml /home/app
RUN mvn -f /home/app/pom.xml clean package

# Compile Stage

FROM eclipse-temurin:17-jre
COPY --from=build /home/app/target/gateway-0.0.1-SNAPSHOT.jar /myapp.jar
RUN sh -c 'touch /myapp.jar'
ENTRYPOINT ["java","-jar","/myapp.jar"]
