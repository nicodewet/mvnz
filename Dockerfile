FROM bellsoft/liberica-openjre-alpine-musl:22.0.1-10
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]