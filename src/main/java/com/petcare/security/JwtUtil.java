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
import java.util.UUID;

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

        // El "jti" (JWT ID) garantiza que cada token generado sea unico incluso si dos tokens se
        // emiten para el mismo usuario dentro del mismo segundo (p.ej. registro seguido de login
        // inmediato): sin el, el token firmado salia byte-a-byte identico y el INSERT en
        // sesiones.token_sesion (columna UNIQUE) fallaba con un 500 por violar esa restriccion
        // (bug encontrado al escribir las pruebas de integracion del Bloque 6.1).
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("email", email)
                .setId(UUID.randomUUID().toString())
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
