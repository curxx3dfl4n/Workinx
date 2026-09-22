package com.workinx.backend.service;

import com.workinx.backend.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Servicio de Lógica de Negocio para la creación y gestión de Reportes de Moderación.
 * <p>
 * Registra denuncias de usuarios sobre ofertas de entrevista inapropiadas o fraudulentas,
 * interactuando con los triggers de la base de datos para la suspensión comunitaria de ofertas.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Service
public class ReportesService {

    /** Componente JdbcTemplate para persistencia de reportes en MySQL. */
    private final JdbcTemplate jdbc;

    /**
     * Constructor para inyección de dependencias.
     *
     * @param jdbc Componente JdbcTemplate
     */
    public ReportesService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Map<String, Object> crearReporte(Long usuarioId, Long entrevistaId,
            String tipoReporte, String descripcion, String evidencia) {

        // Verificar que la entrevista existe y está activa
        List<Map<String, Object>> entrevistas = jdbc.queryForList(
                "SELECT id FROM entrevistas WHERE id = ? AND activa = TRUE AND estado = 'publicada' LIMIT 1",
                entrevistaId);
        if (entrevistas.isEmpty()) {
            throw new AppException(
                    "La entrevista no existe o no está disponible para reportar.", HttpStatus.NOT_FOUND);
        }

        // Verificar que no tenga ya un reporte activo para esa entrevista
        List<Map<String, Object>> reporteExistente = jdbc.queryForList(
                "SELECT id FROM reportes_entrevistas " +
                "WHERE entrevista_id = ? AND usuario_id = ? " +
                "AND estado IN ('pendiente', 'en_revision') LIMIT 1",
                entrevistaId, usuarioId);
        if (!reporteExistente.isEmpty()) {
            throw new AppException(
                    "Ya tienes un reporte activo para esta entrevista.", HttpStatus.CONFLICT);
        }

        KeyHolder kh = new GeneratedKeyHolder();
        final String evidenciaFinal = evidencia != null ? evidencia.trim() : null;
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO reportes_entrevistas " +
                    "(entrevista_id, usuario_id, tipo_reporte, descripcion, evidencia, estado) " +
                    "VALUES (?, ?, ?, ?, ?, 'pendiente')",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, entrevistaId);
            ps.setLong(2, usuarioId);
            ps.setString(3, tipoReporte);
            ps.setString(4, descripcion.trim());
            if (evidenciaFinal != null) ps.setString(5, evidenciaFinal);
            else ps.setNull(5, java.sql.Types.VARCHAR);
            return ps;
        }, kh);

        return Map.of(
                "id", kh.getKey().longValue(),
                "entrevista_id", entrevistaId,
                "tipo_reporte", tipoReporte,
                "estado", "pendiente"
        );
    }
}
