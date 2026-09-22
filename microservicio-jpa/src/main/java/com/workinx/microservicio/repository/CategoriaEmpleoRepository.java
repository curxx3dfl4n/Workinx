package com.workinx.microservicio.repository;

import com.workinx.microservicio.entity.CategoriaEmpleo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de acceso a datos para la entidad {@link CategoriaEmpleo} gestionado por Spring Data JPA.
 * <p>
 * Proporciona métodos CRUD estándar heredados de {@link JpaRepository} y define consultas
 * derivadas por convención de nomenclatura de métodos (Derived Queries) para cumplir con los
 * requerimientos formativos de filtros combinados y paginación:
 * <ul>
 *   <li>Búsqueda por dos campos utilizando el operador lógico {@code AND} (Y).</li>
 *   <li>Búsqueda por tres campos utilizando el operador lógico {@code OR} (O).</li>
 *   <li>Manejo de paginación y ordenamiento dinámico mediante {@link Pageable} y {@link Page}.</li>
 * </ul>
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see org.springframework.data.jpa.repository.JpaRepository
 * @see com.workinx.microservicio.entity.CategoriaEmpleo
 */
@Repository
public interface CategoriaEmpleoRepository extends JpaRepository<CategoriaEmpleo, Long> {

    // =========================================================
    // RETO 1A: BÚSQUEDA POR 2 CAMPOS CON OPERADOR (AND / Y)
    // Busca por Nombre Y por Estado Activa
    // =========================================================

    /**
     * Recupera una lista de categorías cuyo nombre contenga la cadena proporcionada (sin distinguir mayúsculas/minúsculas)
     * <b>Y</b> cuyo estado de activación coincida con el valor solicitado.
     *
     * @param nombre Subcadena a buscar dentro del nombre de la categoría (insensible a mayúsculas).
     * @param activa Estado booleano de la categoría (true para activas, false para inactivas).
     * @return Lista de categorías que cumplen estrictamente con ambas condiciones lógicas.
     */
    List<CategoriaEmpleo> findByNombreContainingIgnoreCaseAndActiva(String nombre, Boolean activa);

    /**
     * Recupera una página paginada de categorías cuyo nombre contenga la cadena proporcionada
     * <b>Y</b> cuyo estado de activación coincida con el valor especificado.
     *
     * @param nombre Subcadena a buscar dentro del nombre de la categoría.
     * @param activa Estado booleano de la categoría.
     * @param pageable Objeto que encapsula el número de página, tamaño de lote y ordenamiento.
     * @return Objeto {@link Page} conteniendo las entidades correspondientes al segmento solicitado.
     */
    Page<CategoriaEmpleo> findByNombreContainingIgnoreCaseAndActiva(String nombre, Boolean activa, Pageable pageable);

    // =========================================================
    // RETO 1B: BÚSQUEDA POR 3 CAMPOS CON OPERADOR (OR / O)
    // Busca por Nombre O por Descripción O por Estado Activa
    // =========================================================

    /**
     * Recupera una lista de categorías donde se cumpla al menos una de las siguientes tres condiciones:
     * que el nombre contenga la subcadena indicada, <b>O</b> que la descripción contenga la subcadena indicada,
     * <b>O</b> que el estado de activación coincida con el parámetro recibido.
     *
     * @param nombre Subcadena a contrastar en el nombre de la categoría.
     * @param descripcion Subcadena a contrastar en la descripción de la categoría.
     * @param activa Estado booleano a contrastar en la bandera activa.
     * @return Lista de categorías que cumplen con al menos uno de los tres criterios evaluados.
     */
    List<CategoriaEmpleo> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrActiva(
            String nombre, String descripcion, Boolean activa
    );

    /**
     * Recupera un segmento paginado de categorías donde se cumpla al menos una de las tres condiciones:
     * coincidencia parcial en nombre, coincidencia parcial en descripción, o coincidencia exacta en estado activo.
     *
     * @param nombre Subcadena para búsqueda en el nombre.
     * @param descripcion Subcadena para búsqueda en la descripción.
     * @param activa Estado booleano de la categoría.
     * @param pageable Configuración de paginación y ordenamiento.
     * @return Objeto {@link Page} con las entidades resultantes de la disyunción lógica.
     */
    Page<CategoriaEmpleo> findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCaseOrActiva(
            String nombre, String descripcion, Boolean activa, Pageable pageable
    );
}
