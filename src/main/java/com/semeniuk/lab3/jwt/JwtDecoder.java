package com.semeniuk.lab3.jwt;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import com.semeniuk.lab3.openidconnect.OpenIdConnectProperties;
import org.springframework.web.client.RestTemplate;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

public class JwtDecoder {
    private final OpenIdConnectProperties openIdConnectProperties;

    public JwtDecoder(OpenIdConnectProperties casdoorProperties) {
        this.openIdConnectProperties = casdoorProperties;
    }

    public Claims decodeToken(String token) {
        try {

            RestTemplate restTemplate = new RestTemplate();
            String jwksJson = restTemplate.getForObject(openIdConnectProperties.getConnectEndpoint() + "/.well-known/jwks",
                    String.class);

            // System.out.println("JWKS JSON: " + jwksJson);

            JWKSet jwkSet = JWKSet.parse(jwksJson);
            List<JWK> keys = jwkSet.getKeys();

            if (keys == null || keys.isEmpty()) {
                throw new RuntimeException("No keys found in JWK Set");
            }

            JWK jwk = keys.get(0);
            RSAPublicKey key = (RSAPublicKey) jwk.toRSAKey().toPublicKey();

            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (Exception e) {
            System.err.println("Failed to decode token:" + e);
            throw new RuntimeException("Failed to decode token", e);
        }
    }

}
