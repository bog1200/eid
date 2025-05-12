#
# Build stage
#
FROM maven:3.9-eclipse-temurin-21-alpine AS build
COPY pom.xml /home/app/
RUN mvn -f /home/app/pom.xml dependency:go-offline
COPY src /home/app/src
RUN mvn -f /home/app/pom.xml clean package

# Compile Stage

FROM eclipse-temurin:21-jre
COPY --from=build /home/app/target/banana_node-0.0.1-SNAPSHOT.jar /myapp.jar
RUN sh -c 'touch /myapp.jar'
EXPOSE 8080
ENTRYPOINT ["java","-jar","/myapp.jar"]
