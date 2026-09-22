package com.workinx.backend.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la colección de auditoría NoSQL utilizando Spring Data MongoDB.
 * <p>
 * Gestiona el almacenamiento y recuperación de documentos {@link AuditLogMongo} en la base de datos
 * MongoDB, proporcionando métodos CRUD integrados y consultas personalizadas por tipo de acción.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see com.workinx.backend.mongo.AuditLogMongo
 * @see org.springframework.data.mongodb.repository.MongoRepository
 */
@Repository
public interface AuditLogMongoRepository extends MongoRepository<AuditLogMongo, String> {

    /**
     * Consulta y recupera todos los registros de auditoría filtrados por un tipo específico de acción.
     *
     * @param accion Cadena que identifica la operación ejecutada (por ejemplo, "CREAR_CATEGORIA", "ELIMINAR_CATEGORIA").
     * @return Lista de entradas {@link AuditLogMongo} registradas bajo la acción indicada.
     */
    List<AuditLogMongo> findByAccion(String accion);
}
