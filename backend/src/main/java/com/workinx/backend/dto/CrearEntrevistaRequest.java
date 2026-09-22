package com.workinx.backend.dto;

import lombok.Data;

import java.util.List;

/**
 * Objeto de Transferencia de Datos (DTO) para la creación y edición de ofertas de entrevistas.
 * <p>
 * Representa la solicitud recibida en {@code POST /api/entrevistas} o {@code PUT /api/entrevistas/{id}}.
 * Incluye campos descriptivos, rango salarial, requisitos y coordenadas para geolocalización.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Data
public class CrearEntrevistaRequest {

    /** Título descriptivo de la vacante u oferta de entrevista. */
    private String titulo;

    /** Categoría laboral o sector funcional de la oferta. */
    private String categoria;

    /** Descripción detallada de las funciones y beneficios. */
    private String descripcion;

    /** Tipo de entrevista/contratación (ej: Individual, Grupal, Técnica). */
    private String tipo;

    /** Modalidad de trabajo (Presencial, Remota, Híbrida). */
    private String modalidad;

    /** Ubicación o ciudad de la vacante. */
    private String ubicacion;

    /** Lugar o dirección específica para la citación a la entrevista. */
    private String lugarEntrevista;

    /** Fecha programada de la entrevista en formato YYYY-MM-DD. */
    private String fechaEntrevista;

    /** Hora programada de la entrevista en formato HH:mm. */
    private String horaEntrevista;

    /** Indicador si el salario es a convenir libremente. */
    private Boolean salarioAConvenir;

    /** Mínimo monto salarial ofrecido. */
    private Double salarioMin;

    /** Máximo monto salarial ofrecido. */
    private Double salarioMax;

    /** Lista de requisitos o competencias requeridas. */
    private List<String> requisitos;

    /** Palabras clave para optimización de búsqueda. */
    private List<String> palabrasClave;

    /** Fecha límite para la recepción de postulaciones. */
    private String fechaLimite;

    /** Coordenada de Latitud geográfica para mapa interactivo. */
    private Double latitud;

    /** Coordenada de Longitud geográfica para mapa interactivo. */
    private Double longitud;
}
