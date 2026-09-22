package com.workinx.backend.service;

import com.workinx.backend.entity.CategoriaEmpleoEntity;
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
 *
 * <p><b>Requerimientos del Reto SENA JPA implementados:</b></p>
 * <ul>
 *   <li><b>Reto 1:</b> Consultas derivadas compuestas con operadores lógicos {@code AND} (2 campos) y {@code OR} (3 campos).</li>
 *   <li><b>Reto 3:</b> Emisión estructurada de logs en niveles {@code INFO}, {@code WARN} y {@code ERROR} usando SLF4J.</li>
 *   <li><b>Reto 5:</b> Paginación robusta mediante {@link Pageable} para optimizar el rendimiento y consumo de memoria.</li>
 * </ul>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see com.workinx.backend.repository.CategoriaEmpleoJpaRepository
 * @see com.workinx.backend.entity.CategoriaEmpleoEntity
 */
@Service
@Transactional
public class CategoriaEmpleoJpaService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaEmpleoJpaService.class);
    private final CategoriaEmpleoJpaRepository jpaRepository;

    @Autowired
    public CategoriaEmpleoJpaService(CategoriaEmpleoJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaEmpleoEntity> listarPaginado(Pageable pageable) {
        log.info("ℹ️ INFO BACKEND: Consultando página {} de categorías (tamaño: {})", pageable.getPageNumber(), pageable.getPageSize());
        return jpaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<CategoriaEmpleoEntity> listarTodas() {
        log.info("ℹ️ INFO BACKEND: Consultando todas las categorías en JPA");
        return jpaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<CategoriaEmpleoEntity> buscarPorDosCamposAnd(String nombre, Boolean activa, Pageable pageable) {
        log.info("ℹ️ INFO BACKEND: Búsqueda 2 campos (AND) -> Nombre: '{}', Activa: {}", nombre, activa);
        return jpaRepository.findByNombreContainingIgnoreCaseAndActiva(nombre, activa, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaEmpleoEntity> buscarPorTresCamposOr(String nombre, String descripcion, Boolean activa, Pageable pageable) {
        log.info("ℹ️ INFO BACKEND: Búsqueda 3 campos (OR) -> Nombre: '{}', Desc: '{}', Activa: {}", nombre, descripcion, activa);
        return jpaRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrActiva(nombre, descripcion, activa, pageable);
    }

    @Transactional(readOnly = true)
    public CategoriaEmpleoEntity obtenerPorId(Long id) {
        log.info("ℹ️ INFO BACKEND: Buscando categoría por ID {}", id);
        return jpaRepository.findById(id).orElseThrow(() -> {
            log.warn("⚠️ WARN BACKEND: No se encontró la categoría ID {}", id);
            return new RuntimeException("Categoría no encontrada con ID: " + id);
        });
    }

    public CategoriaEmpleoEntity crear(CategoriaEmpleoEntity categoria, String ipCliente) {
        log.info("ℹ️ INFO BACKEND: Creando categoría en JPA -> Nombre: {}", categoria.getNombre());
        categoria.setId(null);
        return jpaRepository.save(categoria);
    }

    public CategoriaEmpleoEntity actualizar(Long id, CategoriaEmpleoEntity datos, String ipCliente) {
        log.info("ℹ️ INFO BACKEND: Actualizando categoría ID {}", id);
        CategoriaEmpleoEntity existente = obtenerPorId(id);

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
        return actualizada;
    }

    public void eliminar(Long id, String ipCliente) {
        log.info("ℹ️ INFO BACKEND: Eliminando categoría ID {}", id);
        if (!jpaRepository.existsById(id)) {
            log.error("❌ ERROR BACKEND: Intento fallido de eliminar categoría inexistente ID {}", id);
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }

        jpaRepository.deleteById(id);
        log.info("ℹ️ INFO BACKEND: Categoría ID {} eliminada.", id);
    }
}
