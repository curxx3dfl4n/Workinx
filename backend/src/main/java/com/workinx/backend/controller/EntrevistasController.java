package com.workinx.backend.controller;

import com.workinx.backend.dto.CrearEntrevistaRequest;
import com.workinx.backend.exception.AppException;
import com.workinx.backend.security.UsuarioAutenticado;
import com.workinx.backend.service.EntrevistasService;
import com.workinx.backend.util.Validators;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de Ofertas de Entrevistas Laborales.
 * <p>
 * Proporciona endpoints públicos para listar y consultar ofertas ({@code GET /api/entrevistas}),
 * y endpoints privados para que las empresas creen, editen o eliminen sus publicaciones.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@RestController
@RequestMapping("/api/entrevistas")
public class EntrevistasController {

    /** Servicio de lógica de negocio de entrevistas. */
    private final EntrevistasService entrevistasService;

    /** Utilidad para la normalización y validación de campos. */
    private final Validators validators;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param entrevistasService Servicio de entrevistas
     * @param validators Componente de validaciones
     */
    public EntrevistasController(EntrevistasService entrevistasService, Validators validators) {
        this.entrevistasService = entrevistasService;
        this.validators = validators;
    }

    // =====================================================================
    // GET /api/entrevistas  (público)
    // =====================================================================
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarEntrevistas() {
        List<Map<String, Object>> entrevistas = entrevistasService.listarEntrevistas();
        return ResponseEntity.ok(Map.of(
                "mensaje", "Entrevistas obtenidas correctamente.",
                "total", entrevistas.size(),
                "entrevistas", entrevistas));
    }

    // =====================================================================
    // GET /api/entrevistas/empresa/mis-entrevistas  (JWT empresa)
    // IMPORTANTE: debe ir ANTES de /:id para que Spring no confunda la ruta
    // =====================================================================
    @GetMapping("/empresa/mis-entrevistas")
    public ResponseEntity<Map<String, Object>> listarMisEntrevistas(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (!"empresa".equals(usuario.rol())) {
            throw new AppException("Solo las empresas pueden consultar sus entrevistas.", HttpStatus.FORBIDDEN);
        }

        List<Map<String, Object>> entrevistas = entrevistasService.listarMisEntrevistas(usuario.id());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Entrevistas de empresa obtenidas correctamente.",
                "total", entrevistas.size(),
                "entrevistas", entrevistas));
    }

    // =====================================================================
    // GET /api/entrevistas/{id}  (público)
    // =====================================================================
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerEntrevistaPorId(@PathVariable Long id) {
        Map<String, Object> entrevista = entrevistasService.obtenerEntrevistaPorId(id);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Entrevista obtenida correctamente.",
                "entrevista", entrevista));
    }

    // =====================================================================
    // POST /api/entrevistas  (JWT empresa)
    // =====================================================================
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearEntrevista(
            @RequestBody CrearEntrevistaRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (!"empresa".equals(usuario.rol())) {
            throw new AppException("Solo las empresas pueden publicar entrevistas.", HttpStatus.FORBIDDEN);
        }

        validarCamposEntrevista(req);

        String tipoNorm      = validators.normalizarTipoEntrevista(req.getTipo());
        String modalidadNorm = validators.normalizarModalidad(req.getModalidad());

        if (tipoNorm == null) {
            throw new AppException("El tipo de entrevista no es válido.", HttpStatus.BAD_REQUEST);
        }
        if (modalidadNorm == null) {
            throw new AppException("La modalidad no es válida.", HttpStatus.BAD_REQUEST);
        }

        boolean salarioConvenido = Boolean.TRUE.equals(req.getSalarioAConvenir());
        validarSalario(salarioConvenido, req.getSalarioMin(), req.getSalarioMax());
        validarFechas(req.getFechaLimite(), req.getFechaEntrevista());

        Map<String, Object> entrevista = entrevistasService.crearEntrevista(
                usuario.id(),
                req.getTitulo(), req.getCategoria(), req.getDescripcion(),
                tipoNorm, modalidadNorm, req.getUbicacion(),
                req.getLugarEntrevista(), req.getFechaEntrevista(), req.getHoraEntrevista(),
                salarioConvenido, req.getSalarioMin(), req.getSalarioMax(),
                req.getRequisitos(), req.getPalabrasClave(), req.getFechaLimite(),
                req.getLatitud(), req.getLongitud());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Entrevista publicada correctamente.",
                "entrevista", entrevista));
    }

    // =====================================================================
    // PUT /api/entrevistas/{id}  (JWT empresa)
    // =====================================================================
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarEntrevista(
            @PathVariable Long id,
            @RequestBody CrearEntrevistaRequest req,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (!"empresa".equals(usuario.rol())) {
            throw new AppException("Solo las empresas pueden editar entrevistas.", HttpStatus.FORBIDDEN);
        }

        validarCamposEntrevista(req);

        String tipoNorm      = validators.normalizarTipoEntrevista(req.getTipo());
        String modalidadNorm = validators.normalizarModalidad(req.getModalidad());

        if (tipoNorm == null) {
            throw new AppException("El tipo de entrevista no es válido.", HttpStatus.BAD_REQUEST);
        }
        if (modalidadNorm == null) {
            throw new AppException("La modalidad no es válida.", HttpStatus.BAD_REQUEST);
        }

        boolean salarioConvenido = Boolean.TRUE.equals(req.getSalarioAConvenir());
        validarSalario(salarioConvenido, req.getSalarioMin(), req.getSalarioMax());
        validarFechas(req.getFechaLimite(), req.getFechaEntrevista());

        Map<String, Object> entrevista = entrevistasService.actualizarEntrevista(
                usuario.id(), id,
                req.getTitulo(), req.getCategoria(), req.getDescripcion(),
                tipoNorm, modalidadNorm, req.getUbicacion(),
                req.getLugarEntrevista(), req.getFechaEntrevista(), req.getHoraEntrevista(),
                salarioConvenido, req.getSalarioMin(), req.getSalarioMax(),
                req.getRequisitos(), req.getPalabrasClave(), req.getFechaLimite(),
                req.getLatitud(), req.getLongitud());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Entrevista actualizada correctamente.",
                "entrevista", entrevista));
    }

    // =====================================================================
    // DELETE /api/entrevistas/{id}  (JWT empresa)
    // =====================================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarEntrevista(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        if (!"empresa".equals(usuario.rol())) {
            throw new AppException("Solo las empresas pueden eliminar entrevistas.", HttpStatus.FORBIDDEN);
        }

        entrevistasService.eliminarEntrevista(usuario.id(), id);
        return ResponseEntity.ok(Map.of("mensaje", "Entrevista eliminada correctamente."));
    }

    // ---- Helpers de validación (mismo código que el controller Node.js) ----

    private void validarCamposEntrevista(CrearEntrevistaRequest req) {
        if (isBlank(req.getTitulo()) || isBlank(req.getCategoria()) || isBlank(req.getDescripcion()) ||
                isBlank(req.getTipo()) || isBlank(req.getModalidad()) || isBlank(req.getUbicacion()) ||
                isBlank(req.getLugarEntrevista()) || isBlank(req.getFechaEntrevista()) ||
                isBlank(req.getHoraEntrevista()) || isBlank(req.getFechaLimite())) {
            throw new AppException(
                    "Todos los campos obligatorios de la entrevista deben ser enviados.", HttpStatus.BAD_REQUEST);
        }
        if (req.getDescripcion().trim().length() < 20) {
            throw new AppException("La descripción debe tener mínimo 20 caracteres.", HttpStatus.BAD_REQUEST);
        }
    }

    private void validarSalario(boolean salarioConvenido, Double min, Double max) {
        if (!salarioConvenido) {
            if (min == null || max == null) {
                throw new AppException(
                        "Debes enviar salario mínimo y máximo, o marcar salario a convenir.",
                        HttpStatus.BAD_REQUEST);
            }
            if (min > max) {
                throw new AppException(
                        "El salario mínimo no puede ser mayor al salario máximo.", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private void validarFechas(String fechaLimite, String fechaEntrevista) {
        if (fechaLimite != null && fechaEntrevista != null && fechaLimite.compareTo(fechaEntrevista) > 0) {
            throw new AppException(
                    "La fecha límite no puede ser posterior a la fecha de entrevista.", HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
