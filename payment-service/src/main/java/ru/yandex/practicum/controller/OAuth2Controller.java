package ru.yandex.practicum.controller;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/oauth2")
public class OAuth2Controller {

    @Autowired
    private RegisteredClientRepository clientRepository;
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private KeyPair keyPair;
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8081}")
    private String issuerUri;

    @PostMapping("/token")
    public Mono<ResponseEntity<Map<String, Object>>> token(@RequestParam String grant_type,
                                                          @RequestParam(required = false) String client_id,
                                                          @RequestParam(required = false) String client_secret,
                                                          @RequestParam(required = false) String scope,
                                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return Mono.fromCallable(() -> {
            String actualClientId = client_id;
            String actualClientSecret = client_secret;
            if (StringUtils.hasText(authorization) && authorization.startsWith("Basic ")) {
                String credentials = new String(Base64.getDecoder().decode(authorization.substring(6)));
                String[] parts = credentials.split(":");
                if (parts.length == 2) {
                    actualClientId = parts[0];
                    actualClientSecret = parts[1];
                }
            }
            if (!StringUtils.hasText(actualClientId) || !StringUtils.hasText(actualClientSecret)) {
                return ResponseEntity.badRequest().body(Map.of("error", "invalid_client"));
            }
            RegisteredClient client = clientRepository.findByClientId(actualClientId);
            if (client == null || !actualClientSecret.equals(client.getClientSecret())) {
                return ResponseEntity.badRequest().body(Map.of("error", "invalid_client"));
            }
            if (!"client_credentials".equals(grant_type)) {
                return ResponseEntity.badRequest().body(Map.of("error", "unsupported_grant_type"));
            }
            JwtClaimsSet claims = JwtClaimsSet.builder()
                    .subject(actualClientId)
                    .issuer(issuerUri)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .claim("scope", scope != null ? scope : "payment_api")
                    .claim("authorities", "ROLE_CLIENT")
                    .build();
            String token = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
            return ResponseEntity.ok(Map.of(
                    "access_token", token,
                    "token_type", "Bearer",
                    "expires_in", 3600,
                    "scope", scope != null ? scope : "payment_api"
            ));
        });
    }

    @GetMapping("/jwks")
    public Mono<ResponseEntity<JWKSet>> jwks() {
        return Mono.fromCallable(() -> {
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            RSAKey rsaKey = new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("payment-service-key")
                    .build();
            JWKSet jwkSet = new JWKSet(rsaKey);
            return ResponseEntity.ok(jwkSet);
        });
    }

} 