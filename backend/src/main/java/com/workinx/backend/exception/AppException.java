package com.workinx.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada de dominio para el control de errores de la aplicación.
 * <p>
 * Permite asociar un mensaje claro de error con un estado HTTP específico
 * ({@link HttpStatus}) para ser capturado y formateado por el controlador global.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
public class AppException extends RuntimeException {

    /** Estado HTTP asociado al error de negocio. */
    private final HttpStatus status;

    /**
     * Construye una excepción de aplicación con mensaje y código HTTP.
     *
     * @param message Descripción amigable del error para el usuario
     * @param status Código de estado HTTP correspondiente (ej: BAD_REQUEST, CONFLICT)
     */
    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    /**
     * Obtiene el código de estado HTTP asociado.
     *
     * @return Objeto {@link HttpStatus}
     */
    public HttpStatus getStatus() {
        return status;
    }
}
