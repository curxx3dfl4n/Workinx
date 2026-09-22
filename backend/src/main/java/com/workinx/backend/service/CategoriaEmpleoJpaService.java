package com.workinx.backend.service;

import com.workinx.backend.entity.CategoriaEmpleoEntity;
import com.workinx.backend.mongo.AuditLogMongo;
import com.workinx.backend.mongo.AuditLogMongoRepository;
import com.workinx.backend.repository.CategoriaEmpleoJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de lógica de negocio para la gestión de categorías de empleo en WorkInX.
 * <p>
 * Implementa las operaciones transaccionales para la administración de {@link CategoriaEmpleoEntity},
 * integrando persistencia relacional con Spring Data JPA y registro secundario de auditoría
 * desacoplado en MongoDB (Arquitectura Políglota).
 * </p>
 *
 * <p><b>Requerimientos del Reto SENA JPA implementados:</b></p>
 * <ul>
 *   <li><b>Reto 1:</b> Consultas derivadas compuestas con operadores lógicos {@code AND} (2 campos) y {@code OR} (3 campos).</li>
 *   <li><b>Reto 3:</b> Emisión estructurada de logs en niveles {@code INFO}, {@code WARN} y {@code ERROR} usando SLF4J.</li>
 *   <li><b>Reto 5:</b> Paginación robusta mediante {@link Pageable} para optimizar el rendimiento y consumo de memoria.</li>
 *   <li><b>Funcionalidad Plus:</b> Trazabilidad y auditoría de eventos de mutación (creación, edición, eliminación) en MongoDB sin bloquear la transacción principal.</li>
 * </ul>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see com.workinx.backend.repository.CategoriaEmpleoJpaRepository
 * @see com.workinx.backend.mongo.AuditLogMongoRepository
 * @see com.workinx.backend.entity.CategoriaEmpleoEntity
 */
@Service
@Transactional
public class CategoriaEmpleoJpaService {

    /**
     * Logger para el seguimiento y diagnóstico de eventos en la capa de servicio (SLF4J).
     */
    private static final Logger log = LoggerFactory.getLogger(CategoriaEmpleoJpaService.class);

    /**
     * Repositorio JPA para operaciones relacionales sobre {@link CategoriaEmpleoEntity}.
     */
    private final CategoriaEmpleoJpaRepository jpaRepository;

    /**
     * Repositorio NoSQL opcional para persistir eventos de auditoría en MongoDB.
     * <p>
     * Se inyecta de forma opcional ({@code required = false}) para permitir que la aplicación
     * opere normalmente incluso si MongoDB no está configurado o disponible en el entorno.
     * </p>
     */
    @Autowired(required = false)
    private AuditLogMongoRepository mongoAuditRepository;

    /**
     * Constructor para inyección de dependencias del repositorio JPA principal.
     *
     * @param jpaRepository Repositorio JPA de categorías de empleo.
     */
    @Autowired
    public CategoriaEmpleoJpaService(CategoriaEmpleoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Recupera una página de categorías de empleo registradas en el sistema.
     * <p>
     * Método de solo lectura optimizado para paginación y listados tabulares en la interfaz.
     * </p>
     *
     * @param pageable Configuración de número de página, tamaño y ordenamiento.
     * @return {@link Page} conteniendo las categorías correspondientes al segmento solicitado.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaEmpleoEntity> listarPaginado(Pageable pageable) {
        log.info("ℹ️ INFO BACKEND: Consultando página {} de categorías (tamaño: {})", pageable.getPageNumber(), pageable.getPageSize());
        return jpaRepository.findAll(pageable);
    }

    /**
     * Obtiene el listado completo de todas las categorías de empleo existentes.
     *
     * @return Lista completa de entidades {@link CategoriaEmpleoEntity}.
     */
    @Transactional(readOnly = true)
    public List<CategoriaEmpleoEntity> listarTodas() {
        log.info("ℹ️ INFO BACKEND: Consultando todas las categorías en JPA");
        return jpaRepository.findAll();
    }

    /**
     * Ejecuta una búsqueda filtrando por dos campos con conjunción lógica (AND):
     * coincidencia parcial en nombre y estado de activación exacto.
     * <p>
     * Cumple con el <b>Reto 1A</b> del Reto SENA JPA.
     * </p>
     *
     * @param nombre   Texto parcial a buscar dentro del nombre (ignora mayúsculas/minúsculas).
     * @param activa   Estado booleano de activación requerido.
     * @param pageable Información de paginación y ordenamiento.
     * @return Página de categorías que cumplen simultáneamente ambos criterios.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaEmpleoEntity> buscarPorDosCamposAnd(String nombre, Boolean activa, Pageable pageable) {
        log.info("ℹ️ INFO BACKEND: Búsqueda 2 campos (AND) -> Nombre: '{}', Activa: {}", nombre, activa);
        return jpaRepository.findByNombreContainingIgnoreCaseAndActiva(nombre, activa, pageable);
    }

    /**
     * Ejecuta una búsqueda flexible evaluando tres campos con disyunción lógica (OR):
     * nombre O descripción O estado de activación.
     * <p>
     * Cumple con el <b>Reto 1B</b> del Reto SENA JPA.
     * </p>
     *
     * @param nombre      Texto parcial a buscar en el nombre.
     * @param descripcion Texto parcial a buscar en la descripción.
     * @param activa      Estado booleano a evaluar.
     * @param pageable    Información de paginación y ordenamiento.
     * @return Página de categorías que coinciden con al menos uno de los tres criterios.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaEmpleoEntity> buscarPorTresCamposOr(String nombre, String descripcion, Boolean activa, Pageable pageable) {
        log.info("ℹ️ INFO BACKEND: Búsqueda 3 campos (OR) -> Nombre: '{}', Desc: '{}', Activa: {}", nombre, descripcion, activa);
        return jpaRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrActiva(nombre, descripcion, activa, pageable);
    }

    /**
     * Busca y obtiene una categoría de empleo por su identificador primario.
     *
     * @param id Identificador numérico de la categoría.
     * @return La entidad {@link CategoriaEmpleoEntity} correspondiente.
     * @throws RuntimeException Si no existe ninguna categoría registrada con el ID provisto.
     */
    @Transactional(readOnly = true)
    public CategoriaEmpleoEntity obtenerPorId(Long id) {
        log.info("ℹ️ INFO BACKEND: Buscando categoría por ID {}", id);
        return jpaRepository.findById(id).orElseThrow(() -> {
            // Emisión de log de advertencia (WARN) para monitorear intentos de consulta de recursos inexistentes
            log.warn("⚠️ WARN BACKEND: No se encontró la categoría ID {}", id);
            return new RuntimeException("Categoría no encontrada con ID: " + id);
        });
    }

    /**
     * Registra y persiste una nueva categoría de empleo en la base de datos relacional
     * y genera una entrada de auditoría en MongoDB.
     *
     * @param categoria Objeto {@link CategoriaEmpleoEntity} con los datos a persistir.
     * @param ipCliente Dirección IP del cliente que solicitó la operación para trazabilidad.
     * @return La categoría creada con su ID generado por la base de datos.
     */
    public CategoriaEmpleoEntity crear(CategoriaEmpleoEntity categoria, String ipCliente) {
        log.info("ℹ️ INFO BACKEND: Creando categoría en JPA -> Nombre: {}", categoria.getNombre());
        
        // Forzar id en null para asegurar que el motor JPA ejecute un INSERT y no un UPDATE accidental
        categoria.setId(null);
        CategoriaEmpleoEntity creada = jpaRepository.save(categoria);

        // Registro de auditoría desacoplado en MongoDB para trazabilidad NoSQL
        registrarAuditoriaMongo("CREAR_CATEGORIA", String.valueOf(creada.getId()), 
                "Categoría creada: " + creada.getNombre(), ipCliente);

        return creada;
    }

    /**
     * Actualiza parcialmente los datos de una categoría de empleo existente.
     * <p>
     * Aplica validación previa de existencia, actualiza solo los atributos no nulos provistos
     * y registra el evento de modificación en MongoDB.
     * </p>
     *
     * @param id        Identificador primario de la categoría a actualizar.
     * @param datos     Entidad con los nuevos valores para los campos modificables.
     * @param ipCliente Dirección IP del solicitante para registro de auditoría.
     * @return La entidad {@link CategoriaEmpleoEntity} actualizada y persistida.
     * @throws RuntimeException Si la categoría a modificar no existe en la base de datos.
     */
    public CategoriaEmpleoEntity actualizar(Long id, CategoriaEmpleoEntity datos, String ipCliente) {
        log.info("ℹ️ INFO BACKEND: Actualizando categoría ID {}", id);

        // Verifica que la entidad exista antes de intentar actualizarla
        CategoriaEmpleoEntity existente = obtenerPorId(id);
        
        // Actualización granular de campos (solo los valores enviados)
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            existente.setNombre(datos.getNombre());
        }
        if (datos.getDescripcion() != null) {
            existente.setDescripcion(datos.getDescripcion());
        }
        if (datos.getActiva() != null) {
            existente.setActiva(datos.getActiva());
        }

        CategoriaEmpleoEntity actualizada = jpaRepository.save(existente);
        log.info("ℹ️ INFO BACKEND: Categoría ID {} actualizada correctamente.", id);

        // Registro del evento de actualización en MongoDB
        registrarAuditoriaMongo("ACTUALIZAR_CATEGORIA", String.valueOf(id), 
                "Categoría actualizada: " + actualizada.getNombre(), ipCliente);

        return actualizada;
    }

    /**
     * Elimina físicamente una categoría de empleo de la base de datos por su ID.
     * <p>
     * Verifica la existencia de la entidad antes de ejecutar la eliminación física,
     * emitiendo logs de error en caso de que no se encuentre.
     * </p>
     *
     * @param id        Identificador primario de la categoría a eliminar.
     * @param ipCliente Dirección IP del cliente que realizó la petición.
     * @throws RuntimeException Si la categoría a eliminar no existe en la base de datos.
     */
    public void eliminar(Long id, String ipCliente) {
        log.info("ℹ️ INFO BACKEND: Eliminando categoría ID {}", id);
        
        // Validación previa de existencia para emitir log ERROR y evitar excepciones no controladas de JPA
        if (!jpaRepository.existsById(id)) {
            log.error("❌ ERROR BACKEND: Intento fallido de eliminar categoría inexistente ID {}", id);
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }

        jpaRepository.deleteById(id);
        log.info("ℹ️ INFO BACKEND: Categoría ID {} eliminada.", id);

        // Registro del evento de eliminación en MongoDB
        registrarAuditoriaMongo("ELIMINAR_CATEGORIA", String.valueOf(id), 
                "Categoría eliminada ID: " + id, ipCliente);
    }

    /**
     * Método auxiliar privado para registrar eventos de auditoría en la colección NoSQL de MongoDB.
     * <p>
     * Implementa resiliencia y tolerancia a fallos: si el servicio de MongoDB no se encuentra
     * disponible o la persistencia falla, captura la excepción y emite un log de advertencia
     * sin interrumpir ni hacer rollback de la transacción principal en MySQL.
     * </p>
     *
     * @param accion    Nombre representativo de la acción ejecutada (ej. CREAR_CATEGORIA).
     * @param entidadId ID del registro afectado.
     * @param detalles  Descripción informativa de la mutación.
     * @param ipCliente Dirección IP de origen de la solicitud HTTP.
     */
    private void registrarAuditoriaMongo(String accion, String entidadId, String detalles, String ipCliente) {
        if (mongoAuditRepository != null) {
            try {
                AuditLogMongo audit = new AuditLogMongo(accion, "CategoriaEmpleoEntity", entidadId, detalles, ipCliente);
                mongoAuditRepository.save(audit);
                log.info("🍃 MONGO BACKEND AUDIT: Guardado en MongoDB [ID: {}]", audit.getId());
            } catch (Exception e) {
                // Captura defensiva para evitar que fallas en NoSQL degraden la persistencia relacional
                log.warn("⚠️ MONGO BACKEND WARN: Servidor MongoDB desconectado, operación JPA completada exitosamente: {}", e.getMessage());
            }
        }
    }
}
