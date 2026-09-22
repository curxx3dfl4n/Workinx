package com.workinx.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Componente de utilidad para la administración de Tokens JWT (JSON Web Tokens).
 * <p>
 * Encapsula la lógica de generación, firma criptográfica HMAC-SHA256 y parseo/verificación
 * de validez y tiempo de expiración de los tokens de autenticación de WorkInX.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Component
public class JwtUtil {

    /** Clave secreta simétrica utilizada para firmar los tokens JWT. */
    private final SecretKey key;

    /** Tiempo de expiración del token en milisegundos. */
    private final long expiration;

    /**
     * Constructor para inicialización del componente de seguridad JWT.
     *
     * @param secret Clave secreta inyectada desde {@code application.properties}
     * @param expiration Tiempo de vida del token inyectado desde {@code application.properties}
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        // Garantizar longitud mínima de 256 bits para el algoritmo HS256
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
    }

    /**
     * Genera un token de autenticación firmado conteniendo las declaraciones (claims) del usuario.
     *
     * @param id Identificador único del usuario
     * @param correo Correo electrónico del usuario
     * @param rol Rol del usuario (ej: candidato, empresa, admin)
     * @return Cadena de texto compacta representando el JWT firmado
     */
    public String generarToken(Long id, String correo, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("correo", correo);
        claims.put("rol", rol);

        return Jwts.builder()
                .claims(claims)
                .subject(correo)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * Parsea, decodifica y verifica la firma y expiración de un token JWT recibido.
     *
     * @param token Cadena del token JWT a validar
     * @return Reclamaciones ({@link Claims}) extraídas del token
     * @throws io.jsonwebtoken.JwtException Si el token es inválido, fue alterado o ha expirado
     */
    public Claims parsearToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
