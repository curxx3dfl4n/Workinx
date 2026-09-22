package com.workinx.backend.dto;

import lombok.Data;

/**
 * Objeto de Transferencia de Datos (DTO) para la actualización del estado de una postulación.
 * <p>
 * Mapea la petición {@code PUT /api/postulaciones/{id}/estado} enviada por reclutadores.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Data
public class ActualizarEstadoRequest {
    /** Nuevo estado de la postulación (ej: pendiente, seleccionada, rechazada, finalizada). */
    private String estado;
}
