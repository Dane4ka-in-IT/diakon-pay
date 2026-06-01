package dev.diakon.diakonpay.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    public String generateAccessToken(Integer userId) {
        return buildToken(userId, "access", accessExpirationMs);
    }

    public String generateRefreshToken(Integer userId) {
        return buildToken(userId, "refresh", refreshExpirationMs);
    }

    private String buildToken(Integer userId, String type, long expirationMs) {
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(String.valueOf(userId))
                .claim("type", type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    public Integer extractUserId(String token) {
        return Integer.valueOf(extractAllClaims(token).getSubject());
    }

    public String extractType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public long extractExpirationEpochSeconds(String token) {
        return extractAllClaims(token).getExpiration().toInstant().getEpochSecond();
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }
}
