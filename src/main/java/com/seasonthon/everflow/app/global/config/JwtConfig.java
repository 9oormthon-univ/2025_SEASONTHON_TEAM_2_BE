package com.seasonthon.everflow.app.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder googleJwtDecoder() {
        String jwkSetUri = "https://www.googleapis.com/oauth2/v3/certs";
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
    @Bean
    public JwtDecoder kakaoJwtDecoder() {
        String kakaoJwkSetUri = "https://kauth.kakao.com/.well-known/jwks.json";
        return NimbusJwtDecoder.withJwkSetUri(kakaoJwkSetUri).build();
    }
    @Bean
    public JwtDecoder appleJwtDecoder() {
        String appleJwkSetUri = "https://appleid.apple.com/auth/keys";
        return NimbusJwtDecoder.withJwkSetUri(appleJwkSetUri).build();
    }
}
