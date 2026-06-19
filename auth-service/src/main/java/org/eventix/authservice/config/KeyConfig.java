package org.eventix.authservice.config;

import org.eventix.authservice.security.KeyLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class KeyConfig {

    @Bean
    public PrivateKey privateKey() throws Exception {
        return KeyLoader.loadPrivateKey("keys/private.pem");
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        return KeyLoader.loadPublicKey("keys/public.pem");
    }
}