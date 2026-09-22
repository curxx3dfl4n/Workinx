package com.workinx.backend.repository;

import com.workinx.backend.entity.CategoriaEmpleoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link CategoriaEmpleoEntity} utilizando Spring Data JPA.
 * <p>
 * Proporciona operaciones CRUD estándar heredadas de {@link JpaRepository} e implementa
 * métodos de consulta derivados (Query Methods) para cumplir con los requerimientos del Reto SENA JPA:
 * </p>
 * <ul>
 *   <li><b>Reto 1 (Consultas Derivadas Multi-campo):</b>
 *     <ul>
 *       <li>Búsqueda combinada por 2 campos utilizando el operador lógico {@code AND} (nombre y estado activo).</li>
 *       <li>Búsqueda combinada por 3 campos utilizando el operador lógico {@code OR} (nombre, descripción y estado activo).</li>
 *     </ul>
 *   </li>
 *   <li><b>Reto 5 (Paginación y Rendimiento):</b> Soporte integrado de paginación y ordenamiento mediante {@link Pageable} y {@link Page}.</li>
 * </ul>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see com.workinx.backend.entity.CategoriaEmpleoEntity
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see org.springframework.data.domain.Pageable
 */
@Repository
public interface CategoriaEmpleoJpaRepository extends JpaRepository<CategoriaEmpleoEntity, Long> {

    /**
     * Busca categorías cuyo nombre contenga la cadena proporcionada (ignorando mayúsculas y minúsculas)
     * Y cuyo estado de activación coincida exactamente con el parámetro especificado.
     * <p>
     * Cumple con el <b>Reto 1A</b>: Consulta derivada por 2 campos con operador lógico {@code AND}.
     * Equivalente en SQL a: {@code WHERE LOWER(nombre) LIKE LOWER('%...%') AND activa = ?}.
     * </p>
     *
     * @param nombre Subcadena a buscar dentro del nombre de la categoría.
     * @param activa Estado booleano de activación requerido ({@code true} para activas, {@code false} para inactivas).
     * @return Lista de entidades {@link CategoriaEmpleoEntity} que satisfacen ambas condiciones.
     */
    List<CategoriaEmpleoEntity> findByNombreContainingIgnoreCaseAndActiva(String nombre, Boolean activa);

    /**
     * Busca categorías con coincidencia parcial de nombre (case-insensitive) Y estado de activación,
     * retornando los resultados de forma paginada.
     * <p>
     * Cumple con el <b>Reto 1A</b> y <b>Reto 5</b>: Consulta multi-campo con operador {@code AND} y paginación.
     * </p>
     *
     * @param nombre   Subcadena a buscar dentro del nombre de la categoría.
     * @param activa   Estado booleano de activación requerido.
     * @param pageable Configuración de paginación y ordenamiento (página, tamaño, criterios de orden).
     * @return Página ({@link Page}) con las entidades encontradas y metadatos de paginación.
     */
    Page<CategoriaEmpleoEntity> findByNombreContainingIgnoreCaseAndActiva(String nombre, Boolean activa, Pageable pageable);

    /**
     * Busca categorías que coincidan por nombre O por descripción O por estado de activación.
     * <p>
     * Cumple con el <b>Reto 1B</b>: Consulta derivada por 3 campos con operador lógico {@code OR}.
     * Las coincidencias en nombre y descripción se evalúan de forma parcial e insensible a mayúsculas/minúsculas.
     * Equivalente en SQL a: {@code WHERE LOWER(nombre) LIKE LOWER('%...%') OR LOWER(descripcion) LIKE LOWER('%...%') OR activa = ?}.
     * </p>
     *
     * @param nombre      Subcadena a buscar dentro del nombre de la categoría.
     * @param descripcion Subcadena a buscar dentro de la descripción de la categoría.
     * @param activa      Estado booleano de activación a evaluar.
     * @return Lista de entidades {@link CategoriaEmpleoEntity} que cumplan al menos una de las tres condiciones.
     */
    List<CategoriaEmpleoEntity> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrActiva(
            String nombre, String descripcion, Boolean activa
    );

    /**
     * Busca categorías por coincidencia en nombre O descripción O estado de activación con soporte de paginación.
     * <p>
     * Cumple con el <b>Reto 1B</b> y <b>Reto 5</b>: Consulta por 3 campos con operador {@code OR} y paginación.
     * </p>
     *
     * @param nombre      Subcadena a buscar dentro del nombre de la categoría.
     * @param descripcion Subcadena a buscar dentro de la descripción de la categoría.
     * @param activa      Estado booleano de activación a evaluar.
     * @param pageable    Configuración de paginación y ordenamiento.
     * @return Página ({@link Page}) con las entidades que cumplen al menos un criterio y metadatos de navegación.
     */
    Page<CategoriaEmpleoEntity> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrActiva(
            String nombre, String descripcion, Boolean activa, Pageable pageable
    );
}
