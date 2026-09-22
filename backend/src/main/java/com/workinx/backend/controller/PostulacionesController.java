package com.workinx.backend.controller;

import com.workinx.backend.dto.ActualizarEstadoRequest;
import com.workinx.backend.exception.AppException;
import com.workinx.backend.security.UsuarioAutenticado;
import com.workinx.backend.service.PostulacionesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el módulo de Postulaciones laborales y carga de Hojas de Vida.
 * <p>
 * Proporciona endpoints protegidos para que los candidatos envíen postulaciones adjuntando su CV
 * en formato Multipart/Form-Data ({@code POST /api/postulaciones}), consulten sus inscripciones
 * y las empresas revisen y cambien el estado de los postulantes.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@RestController
@RequestMapping("/api/postulaciones")
public class PostulacionesController {

    /** Servicio de lógica de negocio de postulaciones. */
    private final PostulacionesService postulacionesService;

    /** Lista de estados válidos permitidos para la postulación. */
    private static final List<String> ESTADOS_VALIDOS =
            List.of("pendiente", "revisado", "aceptado", "rechazado", "retirado");

    /**
     * Constructor para inyección de dependencias.
     *
     * @param postulacionesService Servicio de postulaciones
     */
    public PostulacionesController(PostulacionesService postulacionesService) {
        this.postulacionesService = postulacionesService;
    }

    // =====================================================================
    // POST /api/postulaciones  (JWT candidato + archivo cv)
    // Equivalente a: router.post("/", verificarToken, uploadCV.single("cv"), crearPostulacion)
    // =====================================================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> crearPostulacion(
            @RequestParam(value = "entrevistaId", required = false) Long entrevistaId,
            @RequestParam(value = "entrevista_id", required = false) Long entrevistaIdSnake,
            @RequestParam(value = "mensaje", required = false) String mensaje,
            @RequestPart(value = "cv") MultipartFile file,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (!"candidato".equals(usuario.rol())) {
            throw new AppException(
                    "Solo los candidatos pueden postularse a entrevistas.", HttpStatus.FORBIDDEN);
        }

        Long entId = entrevistaId != null ? entrevistaId : entrevistaIdSnake;
        if (entId == null) {
            throw new AppException(
                    "Debes enviar la entrevista a la que deseas postularte.", HttpStatus.BAD_REQUEST);
        }

        if (file == null || file.isEmpty()) {
            throw new AppException("Debes adjuntar tu hoja de vida o CV.", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> postulacion =
                postulacionesService.crearPostulacion(usuario.id(), entId, mensaje, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Postulación enviada correctamente.",
                "postulacion", postulacion));
    }

    // =====================================================================
    // GET /api/postulaciones/mis-postulaciones  (JWT candidato)
    // =====================================================================
    @GetMapping("/mis-postulaciones")
    public ResponseEntity<Map<String, Object>> listarMisPostulaciones(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (!"candidato".equals(usuario.rol())) {
            throw new AppException(
                    "Solo los candidatos pueden consultar sus postulaciones.", HttpStatus.FORBIDDEN);
        }

        List<Map<String, Object>> postulaciones =
                postulacionesService.listarMisPostulaciones(usuario.id());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Postulaciones obtenidas correctamente.",
                "total", postulaciones.size(),
                "postulaciones", postulaciones));
    }

    // =====================================================================
    // GET /api/postulaciones/entrevista/{id}  (JWT empresa)
    // =====================================================================
    @GetMapping("/entrevista/{id}")
    public ResponseEntity<Map<String, Object>> listarPostulacionesPorEntrevista(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (!"empresa".equals(usuario.rol())) {
            throw new AppException("Solo las empresas pueden consultar postulantes.", HttpStatus.FORBIDDEN);
        }

        Map<String, Object> resultado =
                postulacionesService.listarPostulacionesPorEntrevista(usuario.id(), id);

        List<?> postulantes = (List<?>) resultado.get("postulantes");
        return ResponseEntity.ok(Map.of(
                "mensaje", "Postulantes obtenidos correctamente.",
                "entrevista", resultado.get("entrevista"),
                "total", postulantes.size(),
                "postulantes", postulantes));
    }

    // =====================================================================
    // PUT /api/postulaciones/{id}/estado  (JWT empresa)
    // =====================================================================
    @PutMapping("/{id}/estado")
    public ResponseEntity<Map<String, Object>> actualizarEstadoPostulacion(
            @PathVariable Long id,
            @RequestBody ActualizarEstadoRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (!"empresa".equals(usuario.rol())) {
            throw new AppException(
                    "Solo las empresas pueden cambiar el estado de una postulación.", HttpStatus.FORBIDDEN);
        }

        String estado = req.getEstado();
        if (estado == null || !ESTADOS_VALIDOS.contains(estado) || "retirado".equals(estado)) {
            throw new AppException("El estado enviado no es válido.", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> postulacion =
                postulacionesService.actualizarEstadoPostulacion(usuario.id(), id, estado);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Estado de postulación actualizado correctamente.",
                "postulacion", postulacion));
    }
}
