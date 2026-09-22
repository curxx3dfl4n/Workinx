package com.workinx.backend.service;

import com.workinx.backend.exception.AppException;
import com.workinx.backend.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de Lógica de Negocio para la Autenticación y Gestión de Perfiles.
 * <p>
 * Maneja el registro transaccional de candidatos y empresas, el hashing de contraseñas con BCrypt,
 * el inicio de sesión con generación de tokens JWT y la consulta de perfiles de usuario.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Service
public class AuthService {

    /** Instancia de {@link JdbcTemplate} para consultas parametrizadas directas a MySQL. */
    private final JdbcTemplate jdbc;

    /** Codificador de contraseñas mediante algoritmo BCrypt. */
    private final PasswordEncoder passwordEncoder;

    /** Utilidad para la creación y verificación de Tokens JWT. */
    private final JwtUtil jwtUtil;

    /**
     * Constructor para inyección de dependencias de Spring.
     *
     * @param jdbc Objeto JdbcTemplate para persistencia SQL
     * @param passwordEncoder Componente de hashing BCrypt
     * @param jwtUtil Componente de utilidades JWT
     */
    public AuthService(JdbcTemplate jdbc, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.jdbc = jdbc;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    // =====================================================================
    // REGISTRO CANDIDATO
    // =====================================================================
    @Transactional
    public Map<String, Object> registrarCandidato(
            String nombre, String tipoDocumento, String documento,
            String correo, String password, String telefono,
            String ciudad, String categoriaEdad, boolean aceptaTerminos) {

        // Verificar correo duplicado — equivalente a: SELECT id FROM usuarios WHERE correo = ?
        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM usuarios WHERE correo = ? LIMIT 1", correo);
        if (!existing.isEmpty()) {
            throw new AppException("El correo ya está registrado.", HttpStatus.CONFLICT);
        }

        String passwordHash = passwordEncoder.encode(password);

        // INSERT usuarios
        KeyHolder ukh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuarios (nombre_completo, correo, password_hash, rol, telefono) " +
                    "VALUES (?, ?, ?, 'candidato', ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            ps.setString(2, correo);
            ps.setString(3, passwordHash);
            ps.setString(4, telefono);
            return ps;
        }, ukh);

        long usuarioId = ukh.getKey().longValue();

        // INSERT perfiles_candidatos
        jdbc.update(
                "INSERT INTO perfiles_candidatos " +
                "(usuario_id, tipo_documento, documento, ciudad_residencia, categoria_edad, acepta_terminos) " +
                "VALUES (?, ?, ?, ?, ?, ?)",
                usuarioId, tipoDocumento, documento, ciudad, categoriaEdad, aceptaTerminos);

        Map<String, Object> result = new HashMap<>();
        result.put("id", usuarioId);
        result.put("nombre_completo", nombre);
        result.put("correo", correo);
        result.put("rol", "candidato");
        return result;
    }

    // =====================================================================
    // REGISTRO EMPRESA
    // =====================================================================
    @Transactional
    public Map<String, Object> registrarEmpresa(
            String nombreEmpresa, String correo, String password,
            String telefono, String direccion, String industria,
            String descripcion, String sitioWeb, String rangoEmpleados,
            String clasificacionEmpresa, String tipoEntidad, boolean aceptaTerminos) {

        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM usuarios WHERE correo = ? LIMIT 1", correo);
        if (!existing.isEmpty()) {
            throw new AppException("El correo ya está registrado.", HttpStatus.CONFLICT);
        }

        String passwordHash = passwordEncoder.encode(password);

        // INSERT usuarios (rol = empresa)
        KeyHolder ukh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuarios (nombre_completo, correo, password_hash, rol, telefono) " +
                    "VALUES (?, ?, ?, 'empresa', ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombreEmpresa);
            ps.setString(2, correo);
            ps.setString(3, passwordHash);
            ps.setString(4, telefono);
            return ps;
        }, ukh);

        long usuarioId = ukh.getKey().longValue();

        // INSERT empresas
        final String descFinal = descripcion;
        final String sitioWebFinal = sitioWeb;
        KeyHolder ekh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO empresas " +
                    "(usuario_id, nombre_empresa, descripcion, industria, sitio_web, " +
                    "telefono_contacto, direccion, rango_empleados, clasificacion_empresa, " +
                    "tipo_entidad, acepta_terminos) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, usuarioId);
            ps.setString(2, nombreEmpresa);
            ps.setString(3, descFinal);
            ps.setString(4, industria);
            ps.setString(5, sitioWebFinal);
            ps.setString(6, telefono);
            ps.setString(7, direccion);
            ps.setString(8, rangoEmpleados);
            ps.setString(9, clasificacionEmpresa);
            ps.setString(10, tipoEntidad);
            ps.setBoolean(11, aceptaTerminos);
            return ps;
        }, ekh);

        long empresaId = ekh.getKey().longValue();

        Map<String, Object> usuario = new HashMap<>();
        usuario.put("id", usuarioId);
        usuario.put("nombre_completo", nombreEmpresa);
        usuario.put("correo", correo);
        usuario.put("rol", "empresa");

        Map<String, Object> empresa = new HashMap<>();
        empresa.put("id", empresaId);
        empresa.put("nombre_empresa", nombreEmpresa);
        empresa.put("rango_empleados", rangoEmpleados);
        empresa.put("clasificacion_empresa", clasificacionEmpresa);
        empresa.put("tipo_entidad", tipoEntidad);

        Map<String, Object> result = new HashMap<>();
        result.put("usuario", usuario);
        result.put("empresa", empresa);
        return result;
    }

    // =====================================================================
    // LOGIN
    // =====================================================================
    public Map<String, Object> loginUsuario(String correo, String password) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id, nombre_completo, correo, password_hash, rol, estado " +
                "FROM usuarios WHERE correo = ? LIMIT 1", correo);

        if (rows.isEmpty()) {
            throw new AppException("Correo o contraseña incorrectos.", HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> usuario = rows.get(0);

        if (!"activo".equals(usuario.get("estado"))) {
            throw new AppException("La cuenta no está activa.", HttpStatus.FORBIDDEN);
        }

        String passwordHash = (String) usuario.get("password_hash");
        if (!passwordEncoder.matches(password, passwordHash)) {
            throw new AppException("Correo o contraseña incorrectos.", HttpStatus.UNAUTHORIZED);
        }

        Long id    = ((Number) usuario.get("id")).longValue();
        String rol = (String) usuario.get("rol");
        String token = jwtUtil.generarToken(id, correo, rol);

        // Ruta de perfil según rol (misma lógica que el original)
        String rutaPerfil = "/perfil/usuario";
        if ("empresa".equals(rol)) rutaPerfil = "/perfil/empresa";
        if ("admin".equals(rol))   rutaPerfil = "/admin";

        Map<String, Object> usuarioResponse = new HashMap<>();
        usuarioResponse.put("id", id);
        usuarioResponse.put("nombre_completo", usuario.get("nombre_completo"));
        usuarioResponse.put("correo", correo);
        usuarioResponse.put("rol", rol);
        usuarioResponse.put("ruta_perfil", rutaPerfil);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("usuario", usuarioResponse);
        return result;
    }

    // =====================================================================
    // PERFIL
    // =====================================================================
    public Map<String, Object> obtenerPerfil(Long usuarioId, String rol) {
        if ("candidato".equals(rol)) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT u.id, u.nombre_completo, u.correo, u.telefono, u.rol, u.estado, " +
                    "pc.tipo_documento, pc.documento, pc.ciudad_residencia, " +
                    "pc.categoria_edad, pc.acepta_terminos " +
                    "FROM usuarios u " +
                    "INNER JOIN perfiles_candidatos pc ON pc.usuario_id = u.id " +
                    "WHERE u.id = ? LIMIT 1", usuarioId);

            if (rows.isEmpty()) {
                throw new AppException("Perfil de candidato no encontrado.", HttpStatus.NOT_FOUND);
            }

            return Map.of("tipo_perfil", "candidato", "perfil", rows.get(0));
        }

        if ("empresa".equals(rol)) {
            List<Map<String, Object>> rows = jdbc.queryForList(
                    "SELECT u.id AS usuario_id, u.nombre_completo, u.correo, u.telefono, u.rol, u.estado, " +
                    "e.id AS empresa_id, e.nombre_empresa, e.descripcion, e.industria, " +
                    "e.sitio_web, e.telefono_contacto, e.direccion, e.rango_empleados, " +
                    "e.clasificacion_empresa, e.tipo_entidad, e.acepta_terminos " +
                    "FROM usuarios u " +
                    "INNER JOIN empresas e ON e.usuario_id = u.id " +
                    "WHERE u.id = ? LIMIT 1", usuarioId);

            if (rows.isEmpty()) {
                throw new AppException("Perfil de empresa no encontrado.", HttpStatus.NOT_FOUND);
            }

            return Map.of("tipo_perfil", "empresa", "perfil", rows.get(0));
        }

        throw new AppException("Rol no autorizado para consultar perfil.", HttpStatus.FORBIDDEN);
    }
}
