package util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JWTUtil {

    private static final long EXPIRACION = 1000 * 60 * 60 * 8;

    private static SecretKey getKey() {
        String secret = System.getenv("JWT_SECRET");

        if (secret == null || secret.isBlank()) {
            throw new RuntimeException("La variable JWT_SECRET no está definida");
        }

        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public static String generarToken(String email, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRACION))
                .signWith(getKey())
                .compact();
    }

    public static Claims validarToken(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean esValido(String token) {
        try {
            validarToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}