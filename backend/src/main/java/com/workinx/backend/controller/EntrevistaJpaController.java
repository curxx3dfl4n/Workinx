package com.workinx.backend.controller;

import com.workinx.backend.entity.EntrevistaEntity;
import com.workinx.backend.service.EntrevistaJpaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador RESTful JPA para Entrevistas.
 * Endpoint base: /api/v1/entrevistas-jpa
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@RestController
@RequestMapping("/api/v1/entrevistas-jpa")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class EntrevistaJpaController {

    private static final Logger log = LoggerFactory.getLogger(EntrevistaJpaController.class);
    private final EntrevistaJpaService service;

    @Autowired
    public EntrevistaJpaController(EntrevistaJpaService service) {
        this.service = service;
    }

    /** RETO 5 & 1: Paginación + Búsquedas AND/OR */
    @GetMapping
    public ResponseEntity<Page<EntrevistaEntity>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String modo,
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) String modalidad,
            @RequestParam(required = false) String estado
    ) {
        log.info("ℹ️ GET entrevistas-jpa. Modo: '{}', Pág: {}", modo, page);
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // RETO 1A: 2 campos AND (Título Y Estado)
        if ("and".equalsIgnoreCase(modo) && titulo != null) {
            String est = (estado != null) ? estado : "publicada";
            return ResponseEntity.ok(service.buscarAndTituloEstado(titulo, est, pageable));
        }

        // RETO 1B: 3 campos OR (Título O Descripción O Modalidad)
        if ("or".equalsIgnoreCase(modo)) {
            return ResponseEntity.ok(service.buscarOrTituloDescModalidad(
                    titulo != null ? titulo : "",
                    descripcion != null ? descripcion : "",
                    modalidad != null ? modalidad : "",
                    pageable));
        }

        return ResponseEntity.ok(service.listarPaginado(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntrevistaEntity> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    /** RETO 4: @Valid activa las 5 validaciones */
    @PostMapping
    public ResponseEntity<EntrevistaEntity> crear(@Valid @RequestBody EntrevistaEntity entrevista, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(entrevista, req.getRemoteAddr()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntrevistaEntity> actualizar(@PathVariable Long id, @Valid @RequestBody EntrevistaEntity datos, HttpServletRequest req) {
        return ResponseEntity.ok(service.actualizar(id, datos, req.getRemoteAddr()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id, HttpServletRequest req) {
        service.eliminar(id, req.getRemoteAddr());
        Map<String, Object> resp = new HashMap<>();
        resp.put("mensaje", "Entrevista ID " + id + " eliminada correctamente.");
        return ResponseEntity.ok(resp);
    }
}
