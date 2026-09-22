package com.workinx.microservicio.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la colección de auditoría en MongoDB administrado por Spring Data NoSQL.
 * <p>
 * Extiende de {@link MongoRepository} para heredar operaciones estándar de persistencia, consulta y eliminación
 * de documentos {@link AuditLogMongo} con identificador de tipo {@link String} (representación hexadecimal de ObjectId).
 * Facilita la consulta de la bitácora histórica para análisis de seguridad, cumplimiento y trazabilidad operativa.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see org.springframework.data.mongodb.repository.MongoRepository
 * @see com.workinx.microservicio.mongo.AuditLogMongo
 */
@Repository
public interface AuditLogMongoRepository extends MongoRepository<AuditLogMongo, String> {

    /**
     * Recupera todos los registros de auditoría que coincidan exactamente con la acción o verbo indicado.
     *
     * @param accion Tipo de operación a consultar (ej. "CREAR_CATEGORIA", "ELIMINAR_CATEGORIA").
     * @return Lista de documentos {@link AuditLogMongo} correspondientes a dicha acción.
     */
    List<AuditLogMongo> findByAccion(String accion);
}
