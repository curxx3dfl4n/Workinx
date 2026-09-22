package com.workinx.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro interceptor de seguridad para la validación de tokens JWT en solicitudes HTTP.
 * <p>
 * Se ejecuta una vez por petición ({@link OncePerRequestFilter}). Extrae la cabecera
 * {@code Authorization: Bearer <token>}, parsea las credenciales del usuario y
 * establece el objeto de autenticación dentro del {@link SecurityContextHolder}.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /** Componente auxiliar de administración de JWT. */
    private final JwtUtil jwtUtil;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param jwtUtil Utilidad de decodificación y validación JWT
     */
    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Procesa la solicitud HTTP entrante validando la presencia y firma del token Bearer.
     *
     * @param request Solicitud HTTP recibida
     * @param response Respuesta HTTP a generar
     * @param filterChain Cadena de filtros de Spring Security
     * @throws ServletException Si ocurre un error en el servlet
     * @throws IOException Si ocurre un error de E/S
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);

            try {
                Claims claims = jwtUtil.parsearToken(token);

                Long id = ((Number) claims.get("id")).longValue();
                String correo = claims.get("correo", String.class);
                String rol = claims.get("rol", String.class);

                UsuarioAutenticado usuario = new UsuarioAutenticado(id, correo, rol);

                // Crear autenticación con rol para Spring Security
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()))
                );

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (JwtException e) {
                // Token inválido o expirado — se deja el contexto vacío.
                // Spring Security rechazará la petición si la ruta requiere autenticación.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
