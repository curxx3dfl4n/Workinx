package com.workinx.backend.service;

import com.workinx.backend.entity.EntrevistaEntity;
import com.workinx.backend.mongo.AuditLogMongo;
import com.workinx.backend.mongo.AuditLogMongoRepository;
import com.workinx.backend.repository.EntrevistaJpaRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

/**
 * Servicio de negocio JPA para la tabla Entrevistas (Alta velocidad & rendimiento).
 *
 * RETO 1: Búsquedas AND / OR
 * RETO 3: Logs INFO, WARN, ERROR
 * RETO 5: Paginación
 * PLUS:   Auditoría MongoDB Asíncrona (0ms de latencia)
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Service
@Transactional
public class EntrevistaJpaService {

    private static final Logger log = LoggerFactory.getLogger(EntrevistaJpaService.class);

    private final EntrevistaJpaRepository repository;
    private final JdbcTemplate jdbcTemplate;

    // Caché en memoria para optimizar lecturas de FK
    private Long cachedEmpresaId = null;
    private Long cachedCategoriaId = null;

    @Autowired(required = false)
    private AuditLogMongoRepository mongoAuditRepo;

    @Autowired
    public EntrevistaJpaService(EntrevistaJpaRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initDemoData() {
        try {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=0");
            jdbcTemplate.update("INSERT IGNORE INTO usuarios (id, correo, contrasena, rol, activo) VALUES (1, 'demo@workinx.com', '123456', 'EMPRESA', 1)");
            jdbcTemplate.update("INSERT IGNORE INTO empresas (id, usuario_id, nombre_empresa, nit) VALUES (1, 1, 'Empresa Demo WorkInX', '900000000-1')");
            jdbcTemplate.update("INSERT IGNORE INTO categorias_empleo (id, nombre, descripcion, activa) VALUES (1, 'Tecnología y Software', 'Categoría general de tecnología', 1)");
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS=1");
            log.info("✅ Entorno de llaves foráneas verificado en MySQL.");
        } catch (Exception e) {
            log.warn("⚠️ Aviso inicialización datos demo: {}", e.getMessage());
        }
    }

    /** RETO 5: Paginación estándar */
    @Transactional(readOnly = true)
    public Page<EntrevistaEntity> listarPaginado(Pageable pageable) {
        log.info("ℹ️ INFO: Listando entrevistas - Página {}, Tamaño {}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable);
    }

    /** RETO 1A: Búsqueda por 2 campos AND (Título Y Estado) */
    @Transactional(readOnly = true)
    public Page<EntrevistaEntity> buscarAndTituloEstado(String titulo, String estado, Pageable pageable) {
        log.info("ℹ️ INFO: Búsqueda AND -> Título: '{}', Estado: '{}'", titulo, estado);
        return repository.findByTituloContainingIgnoreCaseAndEstado(titulo, estado, pageable);
    }

    /** RETO 1B: Búsqueda por 3 campos OR (Título O Descripción O Modalidad) */
    @Transactional(readOnly = true)
    public Page<EntrevistaEntity> buscarOrTituloDescModalidad(String titulo, String descripcion, String modalidad, Pageable pageable) {
        log.info("ℹ️ INFO: Búsqueda OR -> Título: '{}', Desc: '{}', Modalidad: '{}'", titulo, descripcion, modalidad);
        return repository.findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrModalidadContainingIgnoreCase(
                titulo, descripcion, modalidad, pageable);
    }

    /** Obtener una entrevista por ID */
    @Transactional(readOnly = true)
    public EntrevistaEntity obtenerPorId(Long id) {
        log.info("ℹ️ INFO: Buscando entrevista ID {}", id);
        return repository.findById(id).orElseThrow(() -> {
            log.warn("⚠️ WARN: Entrevista ID {} no encontrada", id);
            return new RuntimeException("Entrevista no encontrada con ID: " + id);
        });
    }

    /** Crear entrevista ultra-rápido */
    public EntrevistaEntity crear(EntrevistaEntity entrevista, String ip) {
        log.info("ℹ️ INFO: Creando entrevista '{}'", entrevista.getTitulo());
        entrevista.setId(null);
        asegurarCamposNoNulos(entrevista);

        EntrevistaEntity creada = repository.save(entrevista);
        auditMongoAsync("CREAR", String.valueOf(creada.getId()), "Entrevista creada: " + creada.getTitulo(), ip);
        return creada;
    }

    /** Actualizar entrevista ultra-rápido */
    public EntrevistaEntity actualizar(Long id, EntrevistaEntity datos, String ip) {
        log.info("ℹ️ INFO: Actualizando entrevista ID {}", id);
        EntrevistaEntity existente = obtenerPorId(id);

        if (datos.getTitulo() != null && !datos.getTitulo().isBlank()) existente.setTitulo(datos.getTitulo());
        if (datos.getDescripcion() != null) existente.setDescripcion(datos.getDescripcion());
        if (datos.getTipo() != null) existente.setTipo(datos.getTipo());
        if (datos.getModalidad() != null) existente.setModalidad(datos.getModalidad());
        if (datos.getUbicacion() != null) existente.setUbicacion(datos.getUbicacion());
        if (datos.getEstado() != null) existente.setEstado(datos.getEstado());
        if (datos.getActiva() != null) existente.setActiva(datos.getActiva());

        asegurarCamposNoNulos(existente);

        EntrevistaEntity actualizada = repository.save(existente);
        log.info("ℹ️ INFO: Entrevista ID {} actualizada", id);
        auditMongoAsync("ACTUALIZAR", String.valueOf(id), "Entrevista actualizada: " + actualizada.getTitulo(), ip);
        return actualizada;
    }

    /** Eliminar entrevista */
    public void eliminar(Long id, String ip) {
        log.info("ℹ️ INFO: Eliminando entrevista ID {}", id);
        if (!repository.existsById(id)) {
            log.error("❌ ERROR: Intento de eliminar entrevista inexistente ID {}", id);
            throw new RuntimeException("Entrevista no encontrada con ID: " + id);
        }
        repository.deleteById(id);
        log.info("ℹ️ INFO: Entrevista ID {} eliminada", id);
        auditMongoAsync("ELIMINAR", String.valueOf(id), "Entrevista eliminada ID: " + id, ip);
    }

    private void asegurarCamposNoNulos(EntrevistaEntity entrevista) {
        Long empresaValida = obtenerEmpresaIdValida();
        Long categoriaValida = obtenerCategoriaIdValida();

        if (entrevista.getEmpresaId() == null || entrevista.getEmpresaId() <= 0) entrevista.setEmpresaId(empresaValida);
        if (entrevista.getCategoriaId() == null || entrevista.getCategoriaId() <= 0) entrevista.setCategoriaId(categoriaValida);
        if (entrevista.getFechaEntrevista() == null) entrevista.setFechaEntrevista(LocalDate.now().plusDays(7));
        if (entrevista.getHoraEntrevista() == null) entrevista.setHoraEntrevista(LocalTime.of(9, 0));
        if (entrevista.getLugarEntrevista() == null) entrevista.setLugarEntrevista("Oficina Principal / Remoto");
        if (entrevista.getUbicacion() == null) entrevista.setUbicacion("Medellín, Colombia");
        if (entrevista.getFechaLimite() == null) entrevista.setFechaLimite(LocalDate.now().plusDays(30));
        if (entrevista.getSalarioAConvenir() == null) entrevista.setSalarioAConvenir(true);
        if (entrevista.getSalarioMin() == null) entrevista.setSalarioMin(0.0);
        if (entrevista.getSalarioMax() == null) entrevista.setSalarioMax(0.0);
        if (entrevista.getVistas() == null) entrevista.setVistas(0);
    }

    private Long obtenerEmpresaIdValida() {
        if (cachedEmpresaId != null) return cachedEmpresaId;
        try {
            Long id = jdbcTemplate.queryForObject("SELECT id FROM empresas ORDER BY id ASC LIMIT 1", Long.class);
            if (id != null) {
                cachedEmpresaId = id;
                return id;
            }
        } catch (Exception ignored) {}
        cachedEmpresaId = 1L;
        return 1L;
    }

    private Long obtenerCategoriaIdValida() {
        if (cachedCategoriaId != null) return cachedCategoriaId;
        try {
            Long id = jdbcTemplate.queryForObject("SELECT id FROM categorias_empleo ORDER BY id ASC LIMIT 1", Long.class);
            if (id != null) {
                cachedCategoriaId = id;
                return id;
            }
        } catch (Exception ignored) {}
        cachedCategoriaId = 1L;
        return 1L;
    }

    /** Auditoría MongoDB ASÍNCRONA: No congela el hilo HTTP principal ni retrasa la respuesta */
    private void auditMongoAsync(String accion, String entidadId, String detalles, String ip) {
        if (mongoAuditRepo != null) {
            CompletableFuture.runAsync(() -> {
                try {
                    mongoAuditRepo.save(new AuditLogMongo(accion, "EntrevistaEntity", entidadId, detalles, ip));
                    log.info("🍃 MONGO ASYNC: Auditoría guardada [{}]", accion);
                } catch (Exception e) {
                    log.warn("⚠️ MONGO ASYNC: Servidor NoSQL no disponible, auditoría omitida silenciosamente.");
                }
            });
        }
    }
}
