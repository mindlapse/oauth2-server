package com.projectsea.oauth2.loader;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeyLoader {
    
    private static final Logger logger = LoggerFactory.getLogger(KeyLoader.class);

    @Value("${oauth2.private-key-pem-base64}")
    private String privateKeyPEMBase64;

    @Value("${oauth2.public-key-pem-base64}")
    private String publicKeyPEMBase64;

    public PrivateKey getPrivateKey() {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(privateKeyPEMBase64);
            String key = new String(decodedBytes)
                .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

            privateKeyPEMBase64 = null;
            byte[] keyBytes = Base64.getDecoder().decode(key);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    public PublicKey getPublicKey() {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(publicKeyPEMBase64);
            String key = new String(decodedBytes)
                .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);

            logger.debug("Decoded public key successfully: {}", new String(keyBytes));

            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }
}
