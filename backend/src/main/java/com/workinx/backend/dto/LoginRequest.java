package com.workinx.backend.dto;

import lombok.Data;

/**
 * Objeto de Transferencia de Datos (DTO) para la petición de inicio de sesión.
 * <p>
 * Mapea la solicitud HTTP JSON enviada al endpoint {@code POST /api/auth/login}.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Data
public class LoginRequest {
    /** Correo electrónico registrado del usuario. */
    private String correo;

    /** Contraseña en texto plano a verificar mediante hash BCrypt. */
    private String password;
}
