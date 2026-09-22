package com.workinx.backend.service;

import com.workinx.backend.exception.AppException;
import com.workinx.backend.util.Formatters;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Servicio de Lógica de Negocio para la administración de Ofertas de Entrevistas.
 * <p>
 * Gestiona el ciclo de vida de las entrevistas (creación, edición, consulta filtrada por tipo/modalidad/búsqueda,
 * geolocalización de mapas, suspensión por reportes y eliminación por la empresa propietaria).
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Service
public class EntrevistasService {

    /** Componente {@link JdbcTemplate} para la ejecución de consultas SQL preparadas. */
    private final JdbcTemplate jdbc;

    /** Utilidad de formateo y estructuración de respuestas JSON. */
    private final Formatters formatters;

    /**
     * Constructor para la inyección de dependencias de Spring.
     *
     * @param jdbc Componente JdbcTemplate
     * @param formatters Componente de formateo de datos
     */
    public EntrevistasService(JdbcTemplate jdbc, Formatters formatters) {
        this.jdbc = jdbc;
        this.formatters = formatters;
    }

    // --- Helper: obtener empresa por usuario ---
    private Map<String, Object> obtenerEmpresaPorUsuario(Long usuarioId) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT id FROM empresas WHERE usuario_id = ? LIMIT 1", usuarioId);
        return rows.isEmpty() ? null : rows.get(0);
    }

    // --- Helper: obtener o crear categoría de empleo ---
    private Long obtenerOCrearCategoria(String nombreCategoria) {
        if (nombreCategoria == null || nombreCategoria.isBlank()) return null;
        String nombre = nombreCategoria.trim();

        List<Map<String, Object>> existing = jdbc.queryForList(
                "SELECT id FROM categorias_empleo WHERE nombre = ? LIMIT 1", nombre);
        if (!existing.isEmpty()) {
            return ((Number) existing.get(0).get("id")).longValue();
        }

        KeyHolder kh = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO categorias_empleo (nombre, descripcion) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, nombre);
            ps.setString(2, "Categoría creada desde publicación de entrevista: " + nombre);
            return ps;
        }, kh);
        return kh.getKey().longValue();
    }

    // --- Helper: insertar requisitos y palabras clave ---
    private void insertarRequisitosYPalabrasClave(Long entrevistaId,
            List<String> requisitos, List<String> palabrasClave) {
        if (requisitos != null) {
            for (String r : requisitos) {
                if (r != null && !r.trim().isEmpty()) {
                    jdbc.update(
                            "INSERT INTO requisitos_entrevistas (entrevista_id, requisito) VALUES (?, ?)",
                            entrevistaId, r.trim());
                }
            }
        }
        if (palabrasClave != null) {
            for (String p : palabrasClave) {
                if (p != null && !p.trim().isEmpty()) {
                    jdbc.update(
                            "INSERT INTO palabras_clave_entrevistas (entrevista_id, palabra) VALUES (?, ?)",
                            entrevistaId, p.trim().toLowerCase());
                }
            }
        }
    }

    // --- Query base para listar entrevistas (reutilizable) ---
    private static final String SELECT_ENTREVISTA =
            "SELECT e.*, emp.nombre_empresa, cat.nombre AS categoria, " +
            "(SELECT GROUP_CONCAT(p.palabra SEPARATOR '||') " +
            " FROM palabras_clave_entrevistas p WHERE p.entrevista_id = e.id) AS palabras_clave, " +
            "(SELECT GROUP_CONCAT(r.requisito SEPARATOR '||') " +
            " FROM requisitos_entrevistas r WHERE r.entrevista_id = e.id) AS requisitos " +
            "FROM entrevistas e " +
            "INNER JOIN empresas emp ON emp.id = e.empresa_id " +
            "LEFT JOIN categorias_empleo cat ON cat.id = e.categoria_id ";

    // =====================================================================
    // CREAR ENTREVISTA
    // =====================================================================
    @Transactional
    public Map<String, Object> crearEntrevista(Long usuarioId,
            String titulo, String categoria, String descripcion,
            String tipo, String modalidad, String ubicacion,
            String lugarEntrevista, String fechaEntrevista, String horaEntrevista,
            boolean salarioConvenido, Double salarioMin, Double salarioMax,
            List<String> requisitos, List<String> palabrasClave, String fechaLimite,
            Double latitud, Double longitud) {

        Map<String, Object> empresa = obtenerEmpresaPorUsuario(usuarioId);
        if (empresa == null) {
            throw new AppException(
                    "No se encontró el perfil de empresa asociado a este usuario.", HttpStatus.NOT_FOUND);
        }

        Long empresaId   = ((Number) empresa.get("id")).longValue();
        Long categoriaId = obtenerOCrearCategoria(categoria);

        KeyHolder kh = new GeneratedKeyHolder();
        final Long catId = categoriaId;
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO entrevistas " +
                    "(empresa_id, categoria_id, titulo, descripcion, tipo, modalidad, " +
                    "ubicacion, lugar_entrevista, fecha_entrevista, hora_entrevista, " +
                    "salario_a_convenir, salario_min, salario_max, fecha_limite, latitud, longitud) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, empresaId);
            if (catId != null) ps.setLong(2, catId); else ps.setNull(2, java.sql.Types.BIGINT);
            ps.setString(3, titulo.trim());
            ps.setString(4, descripcion.trim());
            ps.setString(5, tipo);
            ps.setString(6, modalidad);
            ps.setString(7, ubicacion.trim());
            ps.setString(8, lugarEntrevista.trim());
            ps.setString(9, fechaEntrevista);
            ps.setString(10, horaEntrevista);
            ps.setBoolean(11, salarioConvenido);
            if (salarioConvenido) { ps.setNull(12, java.sql.Types.DECIMAL); ps.setNull(13, java.sql.Types.DECIMAL); }
            else { ps.setDouble(12, salarioMin); ps.setDouble(13, salarioMax); }
            ps.setString(14, fechaLimite);
            if (latitud != null) ps.setDouble(15, latitud); else ps.setNull(15, java.sql.Types.DECIMAL);
            if (longitud != null) ps.setDouble(16, longitud); else ps.setNull(16, java.sql.Types.DECIMAL);
            return ps;
        }, kh);

        long entrevistaId = kh.getKey().longValue();
        insertarRequisitosYPalabrasClave(entrevistaId, requisitos, palabrasClave);

        return Map.of("id", entrevistaId, "titulo", titulo, "empresa_id", empresaId);
    }

    // =====================================================================
    // LISTAR ENTREVISTAS (público)
    // =====================================================================
    public List<Map<String, Object>> listarEntrevistas() {
        List<Map<String, Object>> rows = jdbc.queryForList(
                SELECT_ENTREVISTA +
                "WHERE e.activa = TRUE AND e.estado = 'publicada' " +
                "ORDER BY e.fecha_publicacion DESC");
        return rows.stream().map(formatters::convertirEntrevista).toList();
    }

    // =====================================================================
    // OBTENER ENTREVISTA POR ID
    // =====================================================================
    public Map<String, Object> obtenerEntrevistaPorId(Long id) {
        List<Map<String, Object>> rows = jdbc.queryForList(
                SELECT_ENTREVISTA +
                "WHERE e.id = ? AND e.activa = TRUE AND e.estado = 'publicada' LIMIT 1", id);

        if (rows.isEmpty()) {
            throw new AppException("Entrevista no encontrada.", HttpStatus.NOT_FOUND);
        }
        return formatters.convertirEntrevista(rows.get(0));
    }

    // =====================================================================
    // MIS ENTREVISTAS (empresa)
    // =====================================================================
    public List<Map<String, Object>> listarMisEntrevistas(Long usuarioId) {
        Map<String, Object> empresa = obtenerEmpresaPorUsuario(usuarioId);
        if (empresa == null) {
            throw new AppException("No se encontró el perfil de empresa.", HttpStatus.NOT_FOUND);
        }

        Long empresaId = ((Number) empresa.get("id")).longValue();
        List<Map<String, Object>> rows = jdbc.queryForList(
                SELECT_ENTREVISTA +
                "WHERE e.empresa_id = ? AND e.estado <> 'eliminada' " +
                "ORDER BY e.fecha_publicacion DESC", empresaId);

        return rows.stream().map(formatters::convertirEntrevista).toList();
    }

    // =====================================================================
    // ACTUALIZAR ENTREVISTA
    // =====================================================================
    @Transactional
    public Map<String, Object> actualizarEntrevista(Long usuarioId, Long id,
            String titulo, String categoria, String descripcion,
            String tipo, String modalidad, String ubicacion,
            String lugarEntrevista, String fechaEntrevista, String horaEntrevista,
            boolean salarioConvenido, Double salarioMin, Double salarioMax,
            List<String> requisitos, List<String> palabrasClave, String fechaLimite,
            Double latitud, Double longitud) {

        Map<String, Object> empresa = obtenerEmpresaPorUsuario(usuarioId);
        if (empresa == null) {
            throw new AppException(
                    "No se encontró el perfil de empresa asociado a este usuario.", HttpStatus.NOT_FOUND);
        }

        Long empresaId = ((Number) empresa.get("id")).longValue();

        List<Map<String, Object>> existe = jdbc.queryForList(
                "SELECT id FROM entrevistas WHERE id = ? AND empresa_id = ? AND estado <> 'eliminada' LIMIT 1",
                id, empresaId);
        if (existe.isEmpty()) {
            throw new AppException(
                    "La entrevista no existe o no pertenece a esta empresa.", HttpStatus.NOT_FOUND);
        }

        Long categoriaId = obtenerOCrearCategoria(categoria);
        final Long catId = categoriaId;
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE entrevistas SET " +
                    "categoria_id=?, titulo=?, descripcion=?, tipo=?, modalidad=?, " +
                    "ubicacion=?, lugar_entrevista=?, fecha_entrevista=?, hora_entrevista=?, " +
                    "salario_a_convenir=?, salario_min=?, salario_max=?, fecha_limite=?, " +
                    "latitud=?, longitud=? " +
                    "WHERE id=? AND empresa_id=?");
            if (catId != null) ps.setLong(1, catId); else ps.setNull(1, java.sql.Types.BIGINT);
            ps.setString(2, titulo.trim());
            ps.setString(3, descripcion.trim());
            ps.setString(4, tipo);
            ps.setString(5, modalidad);
            ps.setString(6, ubicacion.trim());
            ps.setString(7, lugarEntrevista.trim());
            ps.setString(8, fechaEntrevista);
            ps.setString(9, horaEntrevista);
            ps.setBoolean(10, salarioConvenido);
            if (salarioConvenido) { ps.setNull(11, java.sql.Types.DECIMAL); ps.setNull(12, java.sql.Types.DECIMAL); }
            else { ps.setDouble(11, salarioMin); ps.setDouble(12, salarioMax); }
            ps.setString(13, fechaLimite);
            if (latitud != null) ps.setDouble(14, latitud); else ps.setNull(14, java.sql.Types.DECIMAL);
            if (longitud != null) ps.setDouble(15, longitud); else ps.setNull(15, java.sql.Types.DECIMAL);
            ps.setLong(16, id);
            ps.setLong(17, empresaId);
            return ps;
        });

        // Borrar y reinsertar requisitos/palabras (mismo patrón DELETE + INSERT)
        jdbc.update("DELETE FROM requisitos_entrevistas WHERE entrevista_id = ?", id);
        jdbc.update("DELETE FROM palabras_clave_entrevistas WHERE entrevista_id = ?", id);
        insertarRequisitosYPalabrasClave(id, requisitos, palabrasClave);

        return Map.of("id", id, "titulo", titulo);
    }

    // =====================================================================
    // ELIMINAR ENTREVISTA (soft delete)
    // =====================================================================
    public void eliminarEntrevista(Long usuarioId, Long id) {
        Map<String, Object> empresa = obtenerEmpresaPorUsuario(usuarioId);
        if (empresa == null) {
            throw new AppException(
                    "No se encontró el perfil de empresa asociado a este usuario.", HttpStatus.NOT_FOUND);
        }

        Long empresaId = ((Number) empresa.get("id")).longValue();
        int affected = jdbc.update(
                "UPDATE entrevistas SET activa = FALSE, estado = 'eliminada' " +
                "WHERE id = ? AND empresa_id = ? AND estado <> 'eliminada'",
                id, empresaId);

        if (affected == 0) {
            throw new AppException(
                    "La entrevista no existe o no pertenece a esta empresa.", HttpStatus.NOT_FOUND);
        }
    }
}
