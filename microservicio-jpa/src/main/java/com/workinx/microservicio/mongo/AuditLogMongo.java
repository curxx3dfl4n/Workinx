package com.workinx.microservicio.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento de persistencia NoSQL para el registro histórico de auditoría en MongoDB.
 * <p>
 * Mapeado a la colección {@code auditoria_operaciones}, este modelo desacopla la bitácora
 * de eventos transaccionales de la base de datos relacional principal, permitiendo almacenar
 * trazas de modificación, creación y borrado con metadatos de red (IP de origen, fecha y hora).
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see org.springframework.data.mongodb.core.mapping.Document
 */
@Document(collection = "auditoria_operaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogMongo {

    /**
     * Identificador único del documento asignado automáticamente por MongoDB (ObjectId).
     */
    @Id
    private String id;

    /**
     * Tipo o verbo de la acción ejecutada (por ejemplo: CREAR_CATEGORIA, ACTUALIZAR_CATEGORIA, ELIMINAR_CATEGORIA).
     */
    private String accion;

    /**
     * Nombre de la entidad de negocio intervenida (ej. "CategoriaEmpleo").
     */
    private String entidad;

    /**
     * Clave primaria o identificador de la entidad relacional afectada.
     */
    private String entidadId;

    /**
     * Descripción textual detallada sobre la operación y los valores aplicados.
     */
    private String detalles;

    /**
     * Fecha y hora precisa en la que ocurrió el evento de auditoría.
     */
    private LocalDateTime fecha;

    /**
     * Dirección IP del cliente o sistema consumidor que originó la transacción.
     */
    private String ipCliente;

    /**
     * Constructor de conveniencia para instanciar registros de auditoría asignando la fecha actual automáticamente.
     *
     * @param accion Verbo de la operación realizada.
     * @param entidad Nombre del modelo o recurso impactado.
     * @param entidadId Identificador del registro modificado o creado.
     * @param detalles Explicación contextual de la acción.
     * @param ipCliente Dirección IP del solicitante.
     */
    public AuditLogMongo(String accion, String entidad, String entidadId, String detalles, String ipCliente) {
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalles = detalles;
        // Se establece automáticamente la estampa de tiempo actual del sistema
        this.fecha = LocalDateTime.now();
        this.ipCliente = ipCliente;
    }
}
