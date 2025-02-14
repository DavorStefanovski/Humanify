# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the JAR file into the container
COPY target/*.jar app.jar

# Expose the application's port (change if necessary)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "app.jar"]
