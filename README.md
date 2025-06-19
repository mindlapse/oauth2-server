# OAuth2 Server

This repository contains an implementation of an OAuth2 server designed for setting up a `client_credentials` flow. It is recommended to deploy this server in a container behind a secure proxy for enhanced security. Key material, such as secrets and credentials, must be provided as environment variables to the container.

## Features

- OAuth2 Client Credentials Flow
- Token generation and validation
- Credentials and keys are passed as environment variables and persisted in memory (i.e. not stored in the image)

## Requirements

- **Java** (JDK 24 or higher)
- A containerization platform (e.g., Docker)

## Usage

Here are the steps to configure and run the server on `http://localhost:8080`:


- Build the Spring Boot jar: `./gradlew build`
- Create a Docker container for it: `docker build -t oauth2-server .`
- Create a public/private key-pair (RSA 2048) in PEM format, used for signing & validating tokens (JWKS)
- Create a YAML file for the supported clients with structure similar to the following example:

```
clients:
  - client_description: "Example Key"
    client_id: "30099642-f454-429f-9afa-a5e0bc9f17a9"
    client_secret: "30099642-f454-429f-9afa-a5e0bc9f17a9"
    scopes:
      - "read"
    grant_types:
      - "client_credentials"
```

run it with:
```bash
docker run -it \
  -p 8080:8080 \
  -e PUBLIC_KEY_PEM_BASE64=...\
  -e PRIVATE_KEY_PEM_BASE64=... \
  -e AUTHORIZED_CLIENTS_YAML_BASE64=... \
  oauth2-server 
```


## API Endpoints

- **POST /oauth2/token**: Obtain an access token using the client credentials flow.
- **GET /oauth2/jwks**: Obtain the key set


## License

This project is licensed under the [MIT License](LICENSE.md).

