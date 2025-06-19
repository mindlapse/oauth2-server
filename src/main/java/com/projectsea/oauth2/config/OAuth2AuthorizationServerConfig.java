package com.projectsea.oauth2.config;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.projectsea.oauth2.loader.ClientLoader;
import com.projectsea.oauth2.loader.KeyLoader;
import com.projectsea.oauth2.model.Client;

@Configuration

public class OAuth2AuthorizationServerConfig {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthorizationServerConfig.class);

    @Bean
    public JWKSource<SecurityContext> jwkSource(KeyLoader keyLoader) {
        RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) keyLoader.getPublicKey())
                .privateKey((RSAPrivateKey) keyLoader.getPrivateKey())
                .keyID(UUID.randomUUID().toString())
                .build();

        // Create a JWKSet with the RSA key
        JWKSet jwkSet = new JWKSet(rsaKey);

        logger.info("JWKSet created with RSA key");

        // Return a JWKSource backed by the JWKSet
        return (jwkSelector, _) -> jwkSelector.select(jwkSet);
    }

    
    /**
     * This bean configures the registered clients for the OAuth2 Authorization Server.
     * It uses the ClientLoader to extract client information from a YAML file.
     *
     * @param clientLoader The ClientLoader instance to load client data.
     * @return A RegisteredClientRepository containing the registered clients.
     * @throws Exception If there is an error during client registration.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(ClientLoader clientLoader) throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        List<Client> clients = clientLoader.extractAuthorizedClients();
        

        List<RegisteredClient> registeredClients = clients.stream().map((client) -> {
            RegisteredClient.Builder builder = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(client.clientId())
                .clientSecret(passwordEncoder.encode(client.clientSecret()))
                .clientName(client.clientDescription());

            
            logger.info("Registering client: {}", client.clientDescription());

            client.scopes().forEach(builder::scope);
            client.grantTypes().forEach(grantType -> {
                if ("authorization_code".equalsIgnoreCase(grantType)) {
                    builder.authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.AUTHORIZATION_CODE);
                } else if ("refresh_token".equalsIgnoreCase(grantType)) {
                    builder.authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.REFRESH_TOKEN);
                } else if ("client_credentials".equalsIgnoreCase(grantType)) {
                    builder.authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS);
                }
            });
            // Add redirect URIs if provided
            if (client.redirectUris() != null) {
                client.redirectUris().forEach(builder::redirectUri);
            }

            return builder.build();
        }).toList();


        return new InMemoryRegisteredClientRepository(registeredClients);
    }
    

    /**
     * This bean configures the security filter chain for the OAuth2 Authorization Server.
     * It uses the OAuth2AuthorizationServerConfigurer to set up the authorization server.
     *
     * @param http The HttpSecurity instance to configure.
     * @return A SecurityFilterChain configured for the OAuth2 Authorization Server.
     * @throws Exception If there is an error during security configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        
        http.with(OAuth2AuthorizationServerConfigurer.authorizationServer(), Customizer.withDefaults());
        http.formLogin(Customizer.withDefaults());
    
        return http.build();
    }
    


}