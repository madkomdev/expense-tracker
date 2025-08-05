# Simplified Dockerfile for pre-built Spring Boot JAR
FROM alpine/java:21-jre
LABEL authors="madhu.kommula"

# Create non-root user
RUN adduser -D app

# Set working directory  
WORKDIR /application

# Copy the pre-built JAR (assumes ./gradlew clean build was run locally)
COPY build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown app:app app.jar

# Switch to non-root user
USER app

# Expose the ports your Spring Boot app runs on
EXPOSE 9000 9001

# Environment variables for JVM optimization
ENV JAVA_OPTS="-XX:MaxRAMPercentage=80 -XX:+UseContainerSupport -XX:+UseG1GC"

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
