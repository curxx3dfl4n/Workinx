package com.workinx.backend.controller;

import com.workinx.backend.dto.CrearReporteRequest;
import com.workinx.backend.exception.AppException;
import com.workinx.backend.security.UsuarioAutenticado;
import com.workinx.backend.service.ReportesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la recepción de Reportes de Moderación sobre ofertas de entrevistas.
 * <p>
 * Expone el endpoint {@code POST /api/reportes} para que usuarios autenticados reporten vacantes
 * sospechosas o maliciosas, activando el mecanismo de moderación comunitaria.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@RestController
@RequestMapping("/api/reportes")
public class ReportesController {

    /** Servicio de lógica de negocio de reportes. */
    private final ReportesService reportesService;

    /** Tipos de reporte permitidos por la plataforma. */
    private static final List<String> TIPOS_REPORTE =
            List.of("fraude", "informacion_falsa", "contenido_inapropiado", "spam", "otro");

    /**
     * Constructor para inyección de dependencias.
     *
     * @param reportesService Servicio de reportes
     */
    public ReportesController(ReportesService reportesService) {
        this.reportesService = reportesService;
    }

    // =====================================================================
    // POST /api/reportes  (JWT cualquier usuario autenticado)
    // =====================================================================
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearReporte(
            @RequestBody CrearReporteRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (req.getEntrevistaId() == null || isBlank(req.getTipoReporte()) || isBlank(req.getDescripcion())) {
            throw new AppException(
                    "Debes enviar entrevista, motivo y descripción del reporte.", HttpStatus.BAD_REQUEST);
        }

        if (!TIPOS_REPORTE.contains(req.getTipoReporte())) {
            throw new AppException("El tipo de reporte no es válido.", HttpStatus.BAD_REQUEST);
        }

        if (req.getDescripcion().trim().length() < 10) {
            throw new AppException(
                    "La descripción debe tener mínimo 10 caracteres.", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> reporte = reportesService.crearReporte(
                usuario.id(),
                req.getEntrevistaId(),
                req.getTipoReporte(),
                req.getDescripcion(),
                req.getEvidencia());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Reporte enviado correctamente.",
                "reporte", reporte));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
