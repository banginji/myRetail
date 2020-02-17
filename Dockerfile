FROM openjdk:13-jdk-alpine
COPY ./build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]