package com.workinx.backend.repository;

import com.workinx.backend.entity.EntrevistaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio Spring Data JPA para Entrevistas.
 *
 * RETO 1A: Búsqueda por 2 campos con AND (Título Y Estado)
 * RETO 1B: Búsqueda por 3 campos con OR  (Título O Descripción O Modalidad)
 * RETO 5:  Paginación nativa con Pageable
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Repository
public interface EntrevistaJpaRepository extends JpaRepository<EntrevistaEntity, Long> {

    // RETO 1A: 2 Campos AND -> Título Y Estado
    Page<EntrevistaEntity> findByTituloContainingIgnoreCaseAndEstado(String titulo, String estado, Pageable pageable);

    // RETO 1B: 3 Campos OR -> Título O Descripción O Modalidad
    Page<EntrevistaEntity> findByTituloContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrModalidadContainingIgnoreCase(
            String titulo, String descripcion, String modalidad, Pageable pageable);
}
