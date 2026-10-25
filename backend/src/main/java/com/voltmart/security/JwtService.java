package com.voltmart.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 64) {
            bytes = Decoders.BASE64.decode(java.util.Base64.getEncoder().encodeToString(bytes));
        }
        this.key = Keys.hmacShaKeyFor(pad(bytes));
        this.expirationMs = expirationMs;
    }

    private byte[] pad(byte[] bytes) {
        if (bytes.length >= 64) {
            return bytes;
        }
        byte[] padded = new byte[64];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }

    public String generate(UUID userId, String email, List<String> roles) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claims(Map.of("email", email, "roles", roles))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
