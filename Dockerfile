# Use a base Java image
FROM openjdk:17-jdk-slim

# Add Author info
LABEL maintainer="j-sol.co.kr"

# command java -version
RUN java -version

# Set the working directory
WORKDIR /home/hellbot

# Copy the Java application into the container
CMD ["ls", "-al"]
ARG JAR_FILE=bot/build/libs/hellbot.jar
ARG COMMON_ENV_FILE=common.env
ARG DEV_ENV_FILE=dev.env
ARG PROD_ENV_FILE=prod.env

# Add the application's jar to the container
ADD ${JAR_FILE} hellbot.jar
ADD ${COMMON_ENV_FILE} common.env
ADD ${DEV_ENV_FILE} dev.env
ADD ${PROD_ENV_FILE} prod.env
# Set the entry point to the Java executable that runs the application
ENTRYPOINT ["java", "-jar", "hellbot.jar", "--spring.profiles.active=prod"]

# Expose port 8080
EXPOSE 10100

# docker build -t hellbot .
