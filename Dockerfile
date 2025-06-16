FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built jar into the image
COPY target/sporty-tech-test-*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]