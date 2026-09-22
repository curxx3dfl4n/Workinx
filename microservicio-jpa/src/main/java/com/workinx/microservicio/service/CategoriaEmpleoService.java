package com.workinx.microservicio.service;

import com.workinx.microservicio.entity.CategoriaEmpleo;
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
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see com.workinx.microservicio.repository.CategoriaEmpleoRepository
 * @see com.workinx.microservicio.entity.CategoriaEmpleo
 */
@Service
@Transactional
public class CategoriaEmpleoService {

    private static final Logger log = LoggerFactory.getLogger(CategoriaEmpleoService.class);
    private final CategoriaEmpleoRepository jpaRepository;

    @Autowired
    public CategoriaEmpleoService(CategoriaEmpleoRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Transactional(readOnly = true)
    public Page<CategoriaEmpleo> listarPaginado(Pageable pageable) {
        log.info("ℹ️ INFO: Solicitando página {} de categorías con tamaño {}", pageable.getPageNumber(), pageable.getPageSize());
        return jpaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<CategoriaEmpleo> listarTodas() {
        log.info("ℹ️ INFO: Consultando listado completo de categorías");
        return jpaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<CategoriaEmpleo> buscarPorDosCamposAnd(String nombre, Boolean activa, Pageable pageable) {
        log.info("ℹ️ INFO: Ejecutando Búsqueda por 2 campos (AND) -> Nombre: '{}', Activa: {}", nombre, activa);
        return jpaRepository.findByNombreContainingIgnoreCaseAndActiva(nombre, activa, pageable);
    }

    @Transactional(readOnly = true)
    public Page<CategoriaEmpleo> buscarPorTresCamposOr(String nombre, String descripcion, Boolean activa, Pageable pageable) {
        log.info("ℹ️ INFO: Ejecutando Búsqueda por 3 campos (OR) -> Nombre: '{}', Desc: '{}', Activa: {}", nombre, descripcion, activa);
        return jpaRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrActiva(nombre, descripcion, activa, pageable);
    }

    @Transactional(readOnly = true)
    public CategoriaEmpleo obtenerPorId(Long id) {
        log.info("ℹ️ INFO: Buscando categoría por ID: {}", id);
        return jpaRepository.findById(id).orElseThrow(() -> {
            log.warn("⚠️ WARN: No se encontró la categoría con el ID: {}", id);
            return new RuntimeException("Categoría no encontrada con el ID: " + id);
        });
    }

    public CategoriaEmpleo crear(CategoriaEmpleo categoria, String ipCliente) {
        log.info("ℹ️ INFO: Guardando nueva categoría con JPA -> Nombre: {}", categoria.getNombre());
        categoria.setId(null);
        return jpaRepository.save(categoria);
    }

    public CategoriaEmpleo actualizar(Long id, CategoriaEmpleo datos, String ipCliente) {
        log.info("ℹ️ INFO: Solicitud de actualización para la categoría ID: {}", id);
        CategoriaEmpleo existente = obtenerPorId(id);

        if (datos.getNombre() != null && !datos.getNombre().isBlank()) {
            existente.setNombre(datos.getNombre());
        }
        if (datos.getDescripcion() != null) {
            existente.setDescripcion(datos.getDescripcion());
        }
        if (datos.getActiva() != null) {
            existente.setActiva(datos.getActiva());
        }

        CategoriaEmpleo actualizada = jpaRepository.save(existente);
        log.info("ℹ️ INFO: Categoría ID {} actualizada exitosamente en JPA.", id);
        return actualizada;
    }

    public void eliminar(Long id, String ipCliente) {
        log.info("ℹ️ INFO: Solicitando eliminación de categoría ID: {}", id);
        if (!jpaRepository.existsById(id)) {
            log.error("❌ ERROR: Intento de eliminar una categoría inexistente ID: {}", id);
            throw new RuntimeException("No se puede eliminar. Categoría no encontrada con ID: " + id);
        }

        jpaRepository.deleteById(id);
        log.info("ℹ️ INFO: Categoría ID {} eliminada de la base de datos MySQL.", id);
    }
}
