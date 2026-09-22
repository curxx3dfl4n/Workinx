package com.workinx.backend.controller;

import com.workinx.backend.dto.LoginRequest;
import com.workinx.backend.dto.RegistroCandidatoRequest;
import com.workinx.backend.dto.RegistroEmpresaRequest;
import com.workinx.backend.exception.AppException;
import com.workinx.backend.security.UsuarioAutenticado;
import com.workinx.backend.service.AuthService;
import com.workinx.backend.util.Validators;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador REST para el módulo de Autenticación y Cuentas de Usuario.
 * <p>
 * Expone los endpoints HTTP para registro de candidatos ({@code POST /api/auth/registro-candidato}),
 * registro de empresas ({@code POST /api/auth/registro-empresa}), inicio de sesión ({@code POST /api/auth/login})
 * y consulta de perfil ({@code GET /api/auth/perfil}).
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /** Servicio de autenticación y lógica de negocio de usuarios. */
    private final AuthService authService;

    /** Utilidad de validaciones de reglas de entrada. */
    private final Validators validators;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param authService Servicio de autenticación
     * @param validators Componente de validaciones
     */
    public AuthController(AuthService authService, Validators validators) {
        this.authService = authService;
        this.validators = validators;
    }

    // =====================================================================
    // POST /api/auth/registro-candidato
    // =====================================================================
    @PostMapping("/registro-candidato")
    public ResponseEntity<Map<String, Object>> registrarCandidato(
            @RequestBody RegistroCandidatoRequest req) {

        // Validaciones de campo obligatorio (mismo orden que el controller Node.js)
        if (isBlank(req.getNombreCompleto()) || isBlank(req.getTipoDocumento()) ||
                isBlank(req.getDocumento()) || isBlank(req.getCorreo()) ||
                isBlank(req.getPassword()) || isBlank(req.getTelefono()) ||
                isBlank(req.getCiudad()) || isBlank(req.getEdadRango())) {
            throw new AppException("Todos los campos obligatorios deben ser enviados.", HttpStatus.BAD_REQUEST);
        }

        if (req.getAceptaTerminos() == null || !req.getAceptaTerminos()) {
            throw new AppException("Debes aceptar los términos y condiciones.", HttpStatus.BAD_REQUEST);
        }

        if (!validators.validarPassword(req.getPassword())) {
            throw new AppException(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial.",
                    HttpStatus.BAD_REQUEST);
        }

        if (!validators.validarDocumento(req.getTipoDocumento(), req.getDocumento())) {
            throw new AppException(
                    "El documento no cumple la cantidad de dígitos requerida para el tipo seleccionado.",
                    HttpStatus.BAD_REQUEST);
        }

        String categoriaEdad = validators.obtenerCategoriaEdad(req.getEdadRango());
        if (categoriaEdad == null) {
            throw new AppException("La categoría de edad no es válida.", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> usuario = authService.registrarCandidato(
                req.getNombreCompleto(), req.getTipoDocumento(), req.getDocumento(),
                req.getCorreo(), req.getPassword(), req.getTelefono(),
                req.getCiudad(), categoriaEdad, req.getAceptaTerminos());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("mensaje", "Candidato registrado correctamente.", "usuario", usuario));
    }

    // =====================================================================
    // POST /api/auth/registro-empresa
    // =====================================================================
    @PostMapping("/registro-empresa")
    public ResponseEntity<Map<String, Object>> registrarEmpresa(
            @RequestBody RegistroEmpresaRequest req) {

        String tipoEntidadFinal      = validators.obtenerTipoEntidad(req.getTipoEntidad());
        String clasificacionEmpresa  = validators.obtenerClasificacionEmpresa(req.getRangoEmpleados());

        if (isBlank(req.getNombre()) || isBlank(req.getCorreo()) || isBlank(req.getPassword()) ||
                isBlank(req.getTelefono()) || isBlank(req.getDireccion()) || isBlank(req.getIndustria()) ||
                isBlank(req.getRangoEmpleados()) || tipoEntidadFinal == null) {
            throw new AppException(
                    "Todos los campos obligatorios de empresa deben ser enviados.", HttpStatus.BAD_REQUEST);
        }

        if (req.getAceptaTerminos() == null || !req.getAceptaTerminos()) {
            throw new AppException("Debes aceptar los términos y condiciones.", HttpStatus.BAD_REQUEST);
        }

        if (clasificacionEmpresa == null) {
            throw new AppException("El rango de empleados no es válido.", HttpStatus.BAD_REQUEST);
        }

        if (!validators.validarPassword(req.getPassword())) {
            throw new AppException(
                    "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial.",
                    HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> resultado = authService.registrarEmpresa(
                req.getNombre(), req.getCorreo(), req.getPassword(),
                req.getTelefono(), req.getDireccion(), req.getIndustria(),
                req.getDescripcion(), req.getSitioWeb(), req.getRangoEmpleados(),
                clasificacionEmpresa, tipoEntidadFinal, req.getAceptaTerminos());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Empresa registrada correctamente.",
                "usuario", resultado.get("usuario"),
                "empresa", resultado.get("empresa")));
    }

    // =====================================================================
    // POST /api/auth/login
    // =====================================================================
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest req) {
        if (isBlank(req.getCorreo()) || isBlank(req.getPassword())) {
            throw new AppException("Correo y contraseña son obligatorios.", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> resultado = authService.loginUsuario(req.getCorreo(), req.getPassword());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Login exitoso.",
                "token", resultado.get("token"),
                "usuario", resultado.get("usuario")));
    }

    // =====================================================================
    // GET /api/auth/perfil  (protegido — requiere JWT)
    // =====================================================================
    @GetMapping("/perfil")
    public ResponseEntity<Map<String, Object>> obtenerPerfil(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {

        Map<String, Object> resultado = authService.obtenerPerfil(usuario.id(), usuario.rol());

        return ResponseEntity.ok(Map.of(
                "mensaje", "Perfil obtenido correctamente.",
                "tipo_perfil", resultado.get("tipo_perfil"),
                "perfil", resultado.get("perfil")));
    }

    // ---- Helper ----
    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
