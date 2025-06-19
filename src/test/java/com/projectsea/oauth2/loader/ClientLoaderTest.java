package com.projectsea.oauth2.loader;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.projectsea.oauth2.model.Client;

@SpringBootTest
@ActiveProfiles("test")
public class ClientLoaderTest {

    private static final String VALID_YAML_BASE64 = Base64.getEncoder().encodeToString(
        """
        clients:
          - client_description: "Test Client 1"
            client_id: "test-client-1"
            client_secret: "secret1"
            scopes:
              - "read"
              - "write"
            grant_types:
              - "authorization_code"
              - "refresh_token"
            redirect_uris:
            - "https://example.com/callback"
          - client_description: "Test Client 2"
            client_id: "test-client-2"
            client_secret: "secret2"
            scopes:
              - "read"
            grant_types:
              - "client_credentials"
        """.getBytes()
    );

    private static final String INVALID_YAML_BASE64 = Base64.getEncoder().encodeToString(
        """
        invalid_yaml_content:
          - some_key: "some_value"
        """.getBytes()
    );

    @InjectMocks
    private ClientLoader clientLoader;


    @Test
    void testExtractAuthorizedClients_ValidYaml() {
        ReflectionTestUtils.setField(clientLoader, "authorizedClientsYamlBase64", VALID_YAML_BASE64);
        List<Client> clients = clientLoader.extractAuthorizedClients();

        assertNotNull(clients);
        assertEquals(2, clients.size());

        Client client1 = clients.get(0);
        assertEquals("Test Client 1", client1.clientDescription());
        assertEquals("test-client-1", client1.clientId());
        assertEquals("secret1", client1.clientSecret());
        assertEquals(List.of("read", "write"), client1.scopes());
        assertEquals(List.of("authorization_code", "refresh_token"), client1.grantTypes());

        Client client2 = clients.get(1);
        assertEquals("Test Client 2", client2.clientDescription());
        assertEquals("test-client-2", client2.clientId());
        assertEquals("secret2", client2.clientSecret());
        assertEquals(List.of("read"), client2.scopes());
        assertEquals(List.of("client_credentials"), client2.grantTypes());
    }

    @Test
    void testExtractAuthorizedClients_InvalidYaml() {
        ReflectionTestUtils.setField(clientLoader, "authorizedClientsYamlBase64", INVALID_YAML_BASE64);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientLoader.extractAuthorizedClients();
        });

        assertEquals("Invalid YAML format: 'clients' key must be a list", exception.getMessage());
    }

    @Test
    void testExtractAuthorizedClients_MalformedBase64() {
        ReflectionTestUtils.setField(clientLoader, "authorizedClientsYamlBase64", "malformed_base64");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            clientLoader.extractAuthorizedClients();
        });

        assertTrue(exception.getMessage().contains("Invalid base64"));
    }
}