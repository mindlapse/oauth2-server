# Use the official OpenJDK 24 image as the base image
FROM openjdk:24-jdk-slim

# Define a variable for the JAR file
ARG JAR_FILE=oauth2-server-0.0.1-SNAPSHOT.jar

# The following envs for PUBLIC_KEY_PEM_BASE64, PRIVATE_KEY_PEM_BASE64, and AUTHORIZED_CLIENTS_YAML_BASE64 will need to be provided
# by the container orchestration layer, thus the defaults are not set

#ENV PUBLIC_KEY_PEM_BASE64=
#ENV PRIVATE_KEY_PEM_BASE64=
#ENV AUTHORIZED_CLIENTS_YAML_BASE64=


# Expose the port the application will run on
EXPOSE 8080

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY build/libs/${JAR_FILE} /app/${JAR_FILE}


ENV JAR_FILE=${JAR_FILE}
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Use the shell script as the entrypoint
ENTRYPOINT ["/entrypoint.sh"]
