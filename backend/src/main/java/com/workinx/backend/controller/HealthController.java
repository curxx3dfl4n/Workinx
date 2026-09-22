package com.workinx.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Controlador REST para el monitoreo de la salud y disponibilidad del servidor.
 * <p>
 * Ofrece los endpoints {@code GET /}, {@code GET /api/health} y {@code GET /api/test}
 * para diagnóstico de estado de la API y verificación del pool de base de datos MySQL.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@RestController
public class HealthController {

    /** Componente JdbcTemplate para prueba de conexión activa. */
    private final JdbcTemplate jdbc;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param jdbc Componente JdbcTemplate
     */
    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Ruta raíz — equivalente al app.get("/") del original */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "mensaje", "Backend de WorkInX (Spring Boot) funcionando correctamente",
                "version", "1.0.0",
                "status", "ok"
        ));
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "ok");
        response.put("mensaje", "Backend de WorkInX (Spring Boot) funcionando correctamente");
        response.put("version", "1.0.0");

        try {
            jdbc.queryForObject("SELECT 1", Integer.class);
            response.put("database", "connected");
        } catch (Exception e) {
            response.put("database", "error: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/test")
    public ResponseEntity<Map<String, Object>> test() {
        return ResponseEntity.ok(Map.of(
                "mensaje", "Ruta de prueba funcionando",
                "status", "ok"
        ));
    }
}
