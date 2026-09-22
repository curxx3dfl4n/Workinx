package com.workinx.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * Objeto de Transferencia de Datos (DTO) para la creación de reportes sobre entrevistas.
 * <p>
 * Mapea la petición {@code POST /api/reportes} enviada por usuarios para moderación comunitaria.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Data
public class CrearReporteRequest {

    /** Identificador único de la entrevista a reportar. */
    @JsonAlias("entrevista_id")
    private Long entrevistaId;

    /** Categoría o motivo del reporte (ej: Spam, Contenido Inapropiado, Estafa, Información Falsa). */
    @JsonAlias("tipo_reporte")
    private String tipoReporte;

    /** Explicación o justificación detallada del reporte. */
    private String descripcion;

    /** Enlace o texto suplementario aportado como evidencia del reporte. */
    private String evidencia;
}
