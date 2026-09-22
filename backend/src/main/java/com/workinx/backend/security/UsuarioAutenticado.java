package com.workinx.backend.security;

/**
 * Record inmutable que representa la identidad del usuario autenticado en la sesión actual.
 * <p>
 * Extraído y construido por {@link JwtAuthFilter} a partir de las declaraciones (claims) del token JWT.
 * Inyectable en controladores mediante {@code @AuthenticationPrincipal}.
 * </p>
 *
 * @param id     Identificador único del usuario en la base de datos
 * @param correo Correo electrónico del usuario
 * @param rol    Rol asignado en el sistema (ej: candidato, empresa, admin)
 * @author Equipo WorkInX - SENA ADSO 2026
 */
public record UsuarioAutenticado(Long id, String correo, String rol) {}
