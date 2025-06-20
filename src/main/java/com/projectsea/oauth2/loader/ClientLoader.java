package com.projectsea.oauth2.loader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.projectsea.oauth2.model.Client;

@Component
public class ClientLoader {

    private static final Logger logger = LoggerFactory.getLogger(ClientLoader.class);

    @Value("${oauth2.authorized-clients-yaml-base64}")
    private String authorizedClientsYamlBase64;

    /**
     * 
     * create a method to parse a yaml base64-encoded string in this form to extract to a List<Client>
     */
    public List<Client> extractAuthorizedClients() throws IllegalArgumentException{

        // Check if the base64 string is empty
        if (authorizedClientsYamlBase64 == null || authorizedClientsYamlBase64.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 string for authorized clients YAML is empty");
        }

        // Decode the base64-encoded YAML 
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] decodedYamlBytes;
        try {
            decodedYamlBytes = decoder.decode(authorizedClientsYamlBase64.replace("\n", ""));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid base64 string for authorized clients YAML", e);
        }
        
        // Parse the YAML string into a Map
        Yaml yaml = new Yaml();
        Map<String, Object> yamlMap;
        try (InputStream inputStream = new java.io.ByteArrayInputStream(decodedYamlBytes)) {
            yamlMap = yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse YAML", e);
        }

        // Extract the list of clients from the clients key
        Object clientsObj = yamlMap.get("clients");
        if (!(clientsObj instanceof Iterable<?>)) {
            throw new IllegalArgumentException("Invalid YAML format: 'clients' key must be a list");
        }
        Iterable<?> clients = (Iterable<?>) clientsObj;
        // Create a list to hold the parsed Client objects
        List<Client> clientList = new ArrayList<>();
        

        // Iterate through the clients and parse each one
        for (Object clientObj : clients) {
            if (!(clientObj instanceof Map<?, ?> clientMap)) {
                throw new IllegalArgumentException("Invalid YAML format: each client must be a map");
            }

            // ID & Secret
            String clientDescription = (String) clientMap.get("client_description");
            String clientId = (String) clientMap.get("client_id");
            String clientSecret = (String) clientMap.get("client_secret");

            // Scopes
            List<String> scopes = clientMap.get("scopes") instanceof List<?> 
                ? ((List<?>) clientMap.get("scopes")).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList()
                : new ArrayList<>();

            // Grant Types
            List<String> grantTypes = clientMap.getOrDefault("grant_types", null) instanceof List<?> 
                ? ((List<?>) clientMap.get("grant_types")).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList()
                : new ArrayList<>();

            // Redirect URIs (optional)
            List<String> redirectUris = clientMap.getOrDefault("redirect_uris", null) instanceof List<?> 
                ? ((List<?>) clientMap.get("redirect_uris")).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .toList()
                : new ArrayList<>();

            clientList.add(new Client(clientDescription, clientId, clientSecret, scopes, grantTypes, redirectUris));
        }

        logger.info("Loaded {} clients from YAML", clientList.size());

        return clientList;
    }

}
