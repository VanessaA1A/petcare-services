package com.petcare.security;

/*
 * Comentario de modulo PetCare:
 * Seguridad del backend. Configura autenticacion, JWT y usuarios reconocidos por Spring Security.
 */

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        String secret = jwtConfig.getSecret();
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET no esta configurado o es demasiado corto (minimo 32 caracteres). " +
                "Define la variable de entorno JWT_SECRET antes de iniciar la aplicacion."
            );
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Integer userId, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtConfig.getExpirationMs());

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
