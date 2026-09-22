package com.workinx.microservicio.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones centralizado para los controladores REST del microservicio.
 * <p>
 * Implementa el patrón Controller Advice mediante {@link RestControllerAdvice}, interceptando
 * anomalías y fallos de validación en tiempo de ejecución para transformarlos en respuestas HTTP
 * estandarizadas y predecibles con formato JSON. Esto evita la fuga de trazas internas del servidor
 * (stack traces) hacia el cliente consumidor y unifica el modelo de errores de la API.
 * </p>
 * <p>
 * Emplea SLF4J para clasificar la severidad de cada incidente:
 * <ul>
 *   <li><b>ERROR:</b> Fallas de validación de esquemas y errores internos no controlados.</li>
 *   <li><b>WARN:</b> Incumplimientos puntuales de restricciones de campo e inexistencia de recursos.</li>
 * </ul>
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see org.springframework.web.bind.annotation.RestControllerAdvice
 * @see org.springframework.web.bind.annotation.ExceptionHandler
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Logger SLF4J para registrar incidencias de validación y fallos de ejecución.
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Intercepta y procesa errores de validación de Bean Validation (desencadenados por {@code @Valid}).
     * <p>
     * Itera sobre la colección de {@link FieldError} para recopilar el nombre del atributo defectuoso
     * y el mensaje de error configurado en la anotación (ej. @NotBlank, @Size, @Pattern), consolidándolos
     * en un mapa para facilitar el consumo del cliente front-end o consumidor REST.
     * </p>
     *
     * @param ex Excepción lanzada por Spring MVC cuando falla la validación de los argumentos de un método.
     * @return {@link ResponseEntity} con código HTTP 400 (Bad Request) y el desglose de campos con error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        // Trazabilidad a nivel ERROR del fallo de validación global
        log.error("❌ ERROR DE VALIDACIÓN: Se enviaron datos no válidos en la petición payload.");

        // Extracción de cada campo infractor y su respectivo mensaje declarativo
        Map<String, String> erroresCampos = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            erroresCampos.put(fieldError.getField(), fieldError.getDefaultMessage());
            // Emisión de advertencia WARN para cada campo que no superó la restricción
            log.warn("⚠️ Validación fallida en campo '{}': {}", fieldError.getField(), fieldError.getDefaultMessage());
        }

        // Ensamblado del cuerpo de la respuesta con metadatos temporales y código de estado
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", "Error de Validación en los Datos Enviados");
        respuesta.put("detalles", erroresCampos);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    /**
     * Intercepta excepciones de tipo {@link RuntimeException} originadas por reglas de negocio no satisfechas
     * o recursos inexistentes (por ejemplo, categorías no encontradas por identificador).
     *
     * @param ex Excepción en tiempo de ejecución capturada.
     * @return {@link ResponseEntity} con código HTTP 404 (Not Found) y la descripción explicativa de la anomalía.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> manejarRuntimeException(RuntimeException ex) {
        // Advertencia a nivel WARN ante solicitud de recursos inexistentes o reglas no cumplidas
        log.warn("⚠️ ADVERTENCIA DE REGLA DE NEGOCIO: {}", ex.getMessage());

        // Estructura JSON homogénea para respuesta de recurso no encontrado
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", HttpStatus.NOT_FOUND.value());
        respuesta.put("error", "Recurso no encontrado o no procesable");
        respuesta.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respuesta);
    }

    /**
     * Captura de contingencia para cualquier otra excepción general no controlada explícitamente en el sistema.
     * <p>
     * Garantiza que el servidor devuelva un código HTTP 500 (Internal Server Error) seguro,
     * ocultando detalles sensibles del sistema y registrando la pila de llamadas completa en los logs del servidor.
     * </p>
     *
     * @param ex Excepción general imprevista capturada.
     * @return {@link ResponseEntity} con estado HTTP 500 (Internal Server Error) y mensaje general.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionGeneral(Exception ex) {
        // Registro a nivel ERROR crítico incluyendo la traza completa de la excepción
        log.error("💥 ERROR CRÍTICO NO CONTROLADO EN EL SISTEMA: ", ex);

        // Respuesta genérica defensiva para el cliente
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("timestamp", LocalDateTime.now().toString());
        respuesta.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        respuesta.put("error", "Error Interno del Servidor");
        respuesta.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
}
