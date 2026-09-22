package com.workinx.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * RETO 2 CUMPLIDO: Manejador Global Centralizado de Excepciones (@RestControllerAdvice).
 * RETO 3 CUMPLIDO: Registro detallado con SLF4J Logger (WARN, ERROR).
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja las excepciones personalizadas de negocio ({@link AppException}).
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<Map<String, Object>> handleAppException(AppException ex) {
        log.warn("⚠️ WARN EXCEPCIÓN DE NEGOCIO: {}", ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(Map.of("mensaje", ex.getMessage()));
    }

    /**
     * Maneja excepciones cuando una ruta o recurso estático no es encontrado.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        log.warn("⚠️ WARN RUTA NO ENCONTRADA: {}", ex.getResourcePath());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", "Ruta no encontrada: " + ex.getResourcePath()));
    }

    /**
     * Maneja el exceso en el límite de tamaño de archivos subidos (Uploads > 5MB).
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.error("❌ ERROR ARCHIVO: Intento de subir archivo superior al límite de 5 MB.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", "El archivo excede el tamaño máximo permitido de 5 MB."));
    }

    /**
     * RETO 2 & RETO 4: Maneja errores de validación de campos en DTOs/Entities anotados con @Valid.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        log.error("❌ ERROR DE VALIDACIÓN @Valid: Se enviaron parámetros inválidos.");

        Map<String, String> detallesErrores = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            detallesErrores.put(fieldError.getField(), fieldError.getDefaultMessage());
            log.warn("⚠️ Validación fallida -> Campo '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("mensaje", "Error de validación en la solicitud.");
        body.put("detalles", detallesErrores);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Manejador genérico para capturar cualquier excepción imprevista en la plataforma.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("💥 ERROR CRÍTICO NO CONTROLADO: {}", ex.getMessage(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("mensaje", ex.getMessage() != null ? ex.getMessage() : "Error interno del servidor.");

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
