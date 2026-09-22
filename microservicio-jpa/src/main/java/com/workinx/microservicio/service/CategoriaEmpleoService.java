package com.workinx.microservicio.service;

import com.workinx.microservicio.entity.CategoriaEmpleo;
import com.workinx.microservicio.mongo.AuditLogMongo;
import com.workinx.microservicio.mongo.AuditLogMongoRepository;
import com.workinx.microservicio.repository.CategoriaEmpleoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de lógica de negocio para la administración integral de categorías de empleo.
 * <p>
 * Esta clase orquesta las operaciones CRUD sobre la base de datos relacional mediante
 * {@link CategoriaEmpleoRepository}, gestiona transacciones atómicas con {@link Transactional},
 * implementa búsquedas combinadas multicriterio (operadores AND / OR), paginación con Spring Data,
 * emite trazas de auditoría y diagnóstico en tres niveles (INFO, WARN, ERROR), e interactúa de
 * manera resiliente con MongoDB (NoSQL) para el registro de bitácoras de auditoría histórica.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see com.workinx.microservicio.repository.CategoriaEmpleoRepository
 * @see com.workinx.microservicio.mongo.AuditLogMongoRepository
 * @see com.workinx.microservicio.entity.CategoriaEmpleo
 */
@Service
@Transactional
public class CategoriaEmpleoService {

    /**
     * Instancia del logger SLF4J para registrar eventos operativos, advertencias y anomalías del servicio.
     */
    private static final Logger log = LoggerFactory.getLogger(CategoriaEmpleoService.class);

    /**
     * Repositorio JPA para acceso y persistencia en base de datos relacional (MySQL).
     */
    private final CategoriaEmpleoRepository jpaRepository;

    /**
     * Repositorio NoSQL opcional para registro de bitácoras en MongoDB.
     * Se inyecta como no obligatorio (required = false) para permitir funcionamiento aún sin clúster Mongo activo.
     */
    @Autowired(required = false)
    private AuditLogMongoRepository mongoAuditRepository;

    /**
     * Constructor principal con inyección de dependencias para el repositorio JPA.
     *
     * @param jpaRepository Instancia administrada de {@link CategoriaEmpleoRepository}.
     */
    @Autowired
    public CategoriaEmpleoService(CategoriaEmpleoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    /**
     * Obtiene una página segmentada de todas las categorías de empleo existentes.
     *
     * @param pageable Configuración de paginación y ordenamiento (número de página, tamaño y dirección).
     * @return Página con los registros recuperados para el segmento solicitado.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaEmpleo> listarPaginado(Pageable pageable) {
        // Registro a nivel INFO informando los parámetros de segmentación
        log.info("ℹ️ INFO: Solicitando página {} de categorías con tamaño {}", pageable.getPageNumber(), pageable.getPageSize());
        return jpaRepository.findAll(pageable);
    }

    /**
     * Obtiene la totalidad de categorías de empleo registradas sin aplicar paginación.
     *
     * @return Lista completa de entidades {@link CategoriaEmpleo}.
     */
    @Transactional(readOnly = true)
    public List<CategoriaEmpleo> listarTodas() {
        // Trazabilidad a nivel INFO de consulta masiva
        log.info("ℹ️ INFO: Consultando listado completo de categorías");
        return jpaRepository.findAll();
    }

    // =========================================================
    // RETO 1A: BÚSQUEDA POR 2 CAMPOS CON OPERADOR AND (Y)
    // =========================================================

    /**
     * Ejecuta una búsqueda filtrada por dos campos simultáneos utilizando el operador lógico AND:
     * coincidencia parcial en el nombre y estado exacto de activación, retornando un resultado paginado.
     *
     * @param nombre Subcadena que debe estar presente en el nombre de la categoría.
     * @param activa Estado booleano que debe tener la categoría (true o false).
     * @param pageable Configuración de paginación y ordenamiento.
     * @return Página con las categorías que satisfacen ambos criterios conjuntamente.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaEmpleo> buscarPorDosCamposAnd(String nombre, Boolean activa, Pageable pageable) {
        log.info("ℹ️ INFO: Ejecutando Búsqueda por 2 campos (AND) -> Nombre: '{}', Activa: {}", nombre, activa);
        return jpaRepository.findByNombreContainingIgnoreCaseAndActiva(nombre, activa, pageable);
    }

    // =========================================================
    // RETO 1B: BÚSQUEDA POR 3 CAMPOS CON OPERADOR OR (O)
    // =========================================================

    /**
     * Ejecuta una búsqueda flexible por tres campos utilizando el operador lógico OR:
     * coincidencia parcial en nombre, coincidencia parcial en descripción o coincidencia de estado activo.
     *
     * @param nombre Subcadena a buscar dentro del nombre de la categoría.
     * @param descripcion Subcadena a buscar dentro de la descripción de la categoría.
     * @param activa Estado booleano de la categoría a contrastar.
     * @param pageable Configuración de paginación y ordenamiento.
     * @return Página con las categorías que satisfacen al menos uno de los criterios.
     */
    @Transactional(readOnly = true)
    public Page<CategoriaEmpleo> buscarPorTresCamposOr(String nombre, String descripcion, Boolean activa, Pageable pageable) {
        log.info("ℹ️ INFO: Ejecutando Búsqueda por 3 campos (OR) -> Nombre: '{}', Desc: '{}', Activa: {}", nombre, descripcion, activa);
        return jpaRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrActiva(nombre, descripcion, activa, pageable);
    }

    /**
     * Busca y recupera una categoría de empleo a partir de su identificador único.
     *
     * @param id Identificador numérico de la categoría a consultar.
     * @return Instancia de {@link CategoriaEmpleo} encontrada.
     * @throws RuntimeException Si no existe ninguna categoría registrada con el ID provisto.
     */
    @Transactional(readOnly = true)
    public CategoriaEmpleo obtenerPorId(Long id) {
        log.info("ℹ️ INFO: Buscando categoría por ID: {}", id);
        return jpaRepository.findById(id).orElseThrow(() -> {
            // Emisión de advertencia WARN cuando se consulta una clave inexistente
            log.warn("⚠️ WARN: No se encontró la categoría con el ID: {}", id);
            return new RuntimeException("Categoría no encontrada con el ID: " + id);
        });
    }

    /**
     * Crea y persiste una nueva categoría de empleo en la base de datos relacional y registra
     * la acción en la bitácora de auditoría NoSQL de MongoDB.
     *
     * @param categoria Objeto con los datos de la categoría a registrar.
     * @param ipCliente Dirección IP de origen de la solicitud HTTP para fines de trazabilidad y seguridad.
     * @return Entidad persistida con su ID autoincremental generado.
     */
    public CategoriaEmpleo crear(CategoriaEmpleo categoria, String ipCliente) {
        log.info("ℹ️ INFO: Guardando nueva categoría con JPA -> Nombre: {}", categoria.getNombre());
        // Se fuerza el identificador a null para garantizar una operación de inserción (INSERT) limpia
        categoria.setId(null);
        CategoriaEmpleo creada = jpaRepository.save(categoria);

        // Registro de auditoría asíncrono/resiliente en la colección NoSQL de MongoDB
        registrarAuditoriaMongo("CREAR_CATEGORIA", String.valueOf(creada.getId()), 
                "Categoría creada: " + creada.getNombre(), ipCliente);

        return creada;
    }

    /**
     * Actualiza parcialmente o totalmente los campos de una categoría de empleo existente.
     *
     * @param id Identificador numérico de la categoría a modificar.
     * @param datos Objeto con las nuevas propiedades a aplicar.
     * @param ipCliente Dirección IP del cliente que ejecuta la actualización.
     * @return Entidad {@link CategoriaEmpleo} con los cambios persistidos.
     * @throws RuntimeException Si el ID especificado no se encuentra en el repositorio.
     */
    public CategoriaEmpleo actualizar(Long id, CategoriaEmpleo datos, String ipCliente) {
        log.info("ℹ️ INFO: Solicitud de actualización para la categoría ID: {}", id);

        // Verifica existencia previa cargando la entidad
        CategoriaEmpleo existente = obtenerPorId(id);

        // Modifica únicamente los valores suministrados que no sean nulos o vacíos
        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            existente.setNombre(datos.getNombre());
        }
        if (datos.getDescripcion() != null) {
            existente.setDescripcion(datos.getDescripcion());
        }
        if (datos.getActiva() != null) {
            existente.setActiva(datos.getActiva());
        }

        // Guarda la versión modificada en MySQL
        CategoriaEmpleo actualizada = jpaRepository.save(existente);
        log.info("ℹ️ INFO: Categoría ID {} actualizada exitosamente en JPA.", id);

        // Registra el evento de modificación en la auditoría NoSQL
        registrarAuditoriaMongo("ACTUALIZAR_CATEGORIA", String.valueOf(id), 
                "Categoría actualizada: " + actualizada.getNombre(), ipCliente);

        return actualizada;
    }

    /**
     * Elimina físicamente una categoría de empleo a partir de su ID.
     *
     * @param id Identificador de la categoría a remover.
     * @param ipCliente Dirección IP del cliente que solicita la supresión.
     * @throws RuntimeException Si la categoría no existe en el sistema.
     */
    public void eliminar(Long id, String ipCliente) {
        log.info("ℹ️ INFO: Solicitando eliminación de categoría ID: {}", id);
        // Validación de existencia antes de proceder al borrado
        if (!jpaRepository.existsById(id)) {
            // Emisión de log a nivel ERROR cuando ocurre una falla de precondición grave
            log.error("❌ ERROR: Intento de eliminar una categoría inexistente ID: {}", id);
            throw new RuntimeException("No se puede eliminar. Categoría no encontrada con ID: " + id);
        }

        // Eliminación física del registro en la base de datos MySQL
        jpaRepository.deleteById(id);
        log.info("ℹ️ INFO: Categoría ID {} eliminada de la base de datos MySQL.", id);

        // Notificación y guardado de auditoría en MongoDB
        registrarAuditoriaMongo("ELIMINAR_CATEGORIA", String.valueOf(id), 
                "Categoría eliminada ID: " + id, ipCliente);
    }

    /**
     * Registra un evento transaccional en la base de datos NoSQL MongoDB con manejo tolerante a fallos.
     * <p>
     * En caso de que el servicio de MongoDB se encuentre fuera de línea o inaccesible, la excepción es
     * capturada y registrada mediante un log WARN, garantizando que la operación principal sobre MySQL
     * no se interrumpa ni sufra un rollback innecesario (patrón de degradación elegante).
     * </p>
     *
     * @param accion Tipo de acción ejecutada (ej. CREAR_CATEGORIA, ACTUALIZAR_CATEGORIA, ELIMINAR_CATEGORIA).
     * @param entidadId Identificador de la entidad afectada.
     * @param detalles Descripción informativa complementaria del evento.
     * @param ipCliente Dirección IP de origen de la petición.
     */
    private void registrarAuditoriaMongo(String accion, String entidadId, String detalles, String ipCliente) {
        // Verifica si el componente de auditoría NoSQL fue inicializado por Spring
        if (mongoAuditRepository != null) {
            try {
                // Instancia el documento de auditoría con los metadatos de la operación
                AuditLogMongo audit = new AuditLogMongo(accion, "CategoriaEmpleo", entidadId, detalles, ipCliente);
                mongoAuditRepository.save(audit);
                log.info("🍃 PLUS MONGO: Auditoría NoSQL guardada exitosamente en MongoDB [ID Mongo: {}]", audit.getId());
            } catch (Exception e) {
                // Tolerancia a fallos: no interrumpe el flujo si el servidor NoSQL no responde
                log.warn("⚠️ WARN MONGO: No se pudo conectar a MongoDB para la auditoría (Servidor Mongo offline). Operación JPA continúa sin problemas: {}", e.getMessage());
            }
        }
    }
}
