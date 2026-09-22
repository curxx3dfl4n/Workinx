package com.workinx.backend.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Documento NoSQL mapeado a la colección {@code auditoria_backend_principal} en MongoDB.
 * <p>
 * Representa un registro inmutable de auditoría para la trazabilidad y seguimiento de eventos
 * de negocio en el Backend Principal de WorkInX. Cada entrada almacena el tipo de acción ejecutada,
 * la entidad involucrada, el identificador del registro afectado, una descripción contextual,
 * la marca temporal del suceso y la dirección IP del cliente solicitante.
 * </p>
 * <p>
 * Esta implementación desacopla la persistencia de auditoría de la base de datos relacional MySQL,
 * aportando alto rendimiento de escritura y flexibilidad en esquemas semiestructurados.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see com.workinx.backend.mongo.AuditLogMongoRepository
 * @see com.workinx.backend.service.CategoriaEmpleoJpaService
 */
@Document(collection = "auditoria_backend_principal")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogMongo {

    /**
     * Identificador único del documento generado automáticamente por MongoDB (ObjectId representado como String).
     */
    @Id
    private String id;

    /**
     * Operación o evento registrado en el sistema (por ejemplo: CREAR_CATEGORIA, ACTUALIZAR_CATEGORIA, ELIMINAR_CATEGORIA).
     */
    private String accion;

    /**
     * Nombre simple o calificado de la entidad de dominio sujeta a la acción (ej. {@code CategoriaEmpleoEntity}).
     */
    private String entidad;

    /**
     * Identificador de la entidad relacional afectada por la operación.
     */
    private String entidadId;

    /**
     * Mensaje explicativo o resumen detallado de los cambios aplicados en la operación.
     */
    private String detalles;

    /**
     * Fecha y hora exacta de registro del evento de auditoría.
     */
    private LocalDateTime fecha;

    /**
     * Dirección IP remota desde donde se originó la petición HTTP causante del evento.
     */
    private String ipCliente;

    /**
     * Constructor de conveniencia para registrar una nueva entrada de auditoría NoSQL.
     * <p>
     * Inicializa automáticamente el atributo {@link #fecha} con el instante actual del sistema
     * ({@link LocalDateTime#now()}).
     * </p>
     *
     * @param accion    Tipo de operación ejecutada (ej. CREAR_CATEGORIA).
     * @param entidad   Nombre de la clase o entidad de negocio afectada.
     * @param entidadId Clave primaria de la entidad afectada en formato String.
     * @param detalles  Descripción o metadatos relevantes del suceso.
     * @param ipCliente Dirección IP del cliente emisor de la solicitud.
     */
    public AuditLogMongo(String accion, String entidad, String entidadId, String detalles, String ipCliente) {
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.detalles = detalles;
        this.fecha = LocalDateTime.now();
        this.ipCliente = ipCliente;
    }
}
