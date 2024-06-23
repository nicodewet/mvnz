FROM bellsoft/liberica-openjre-alpine-musl:22.0.1-10
RUN addgroup -S spring && adduser -S spring -G spring

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Switch to non-root user
USER spring:spring

# Expose the application port
EXPOSE 8080

# Define the entry point for the application
ENTRYPOINT ["java","-jar","/app.jar"]