FROM eclipse-temurin:22.0.1_8-jre-ubi9-minimal
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]