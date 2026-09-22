package com.workinx.backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * Objeto de Transferencia de Datos (DTO) para el registro de entidades empresariales.
 * <p>
 * Mapea la petición {@code POST /api/auth/registro-empresa} aceptando múltiples alias
 * para compatibilidad con variaciones de campos recibidos desde el cliente Web.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Data
public class RegistroEmpresaRequest {

    /** Nombre comercial o razón social de la empresa. */
    @JsonAlias("nombre_empresa")
    private String nombre;

    /** Correo electrónico corporativo principal. */
    private String correo;

    /** Contraseña de acceso a la cuenta de empresa. */
    private String password;

    /** Número telefónico de contacto de la entidad. */
    @JsonAlias("telefono_contacto")
    private String telefono;

    /** Dirección física de la sede principal o sucursal. */
    private String direccion;

    /** Sector industrial o económico al que pertenece. */
    private String industria;

    /** Descripción corporativa detallada. */
    private String descripcion;

    /** Dirección URL del sitio web oficial de la empresa. */
    @JsonAlias("sitio_web")
    private String sitioWeb;

    /** Rango de cantidad de empleados para clasificación según Ley de Mipymes. */
    @JsonAlias({"rango_empleados", "numero_empleados_rango"})
    private String rangoEmpleados;

    /** Tipo de entidad jurídica (Privada, Pública, Mixta, ONG). */
    @JsonAlias("tipo_entidad")
    private String tipoEntidad;

    /** Flag de aceptación explícita de los términos de servicio. */
    @JsonAlias("acepta_terminos")
    private Boolean aceptaTerminos;
}
