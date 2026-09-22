package com.workinx.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * Objeto de Transferencia de Datos (DTO) para la solicitud de registro de candidatos.
 * <p>
 * Contiene alias Jackson para soportar tanto notaciones {@code camelCase} como {@code snake_case}
 * enviadas desde el cliente de React. Mapea la petición {@code POST /api/auth/registro-candidato}.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Data
public class RegistroCandidatoRequest {

    /** Nombre completo del postulante. */
    @JsonAlias("nombre_completo")
    private String nombreCompleto;

    /** Tipo de documento de identidad (ej: CC, TI, CE, Pasaporte). */
    @JsonAlias("tipo_documento")
    private String tipoDocumento;

    /** Número de documento de identidad. */
    private String documento;

    /** Correo electrónico institucional o personal del candidato. */
    private String correo;

    /** Contraseña elegida por el usuario (debe cumplir políticas de complejidad). */
    private String password;

    /** Número de teléfono de contacto. */
    private String telefono;

    /** Ciudad de residencia habitual. */
    @JsonAlias("ciudad_residencia")
    private String ciudad;

    /** Rango de edad seleccionado para la categorización del candidato. */
    @JsonAlias("edad_rango")
    private String edadRango;

    /** Flag de aceptación explícita de términos y condiciones de la plataforma. */
    @JsonAlias("acepta_terminos")
    private Boolean aceptaTerminos;
}
