# Use a base Java image
FROM openjdk:17-jdk-slim

# Add Author info
LABEL maintainer="j-sol.co.kr"

# Set the working directory
WORKDIR /home/hellbot

# Copy the Java application into the container
ARG JAR_FILE=build/libs/hellbot.jar

# Add the application's jar to the container
ADD ${JAR_FILE} hellbot.jar

# Set the entry point to the Java executable that runs the application
ENTRYPOINT ["java", "-jar", "hellbot.jar"]

# Expose port 8080
EXPOSE 10100

# docker build -t hellbot .