package com.workinx.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una categoría de empleo en el sistema WorkInX.
 * <p>
 * Esta clase mapea directamente a la tabla relacional {@code categorias_empleo}
 * de la base de datos principal (MySQL) y define las restricciones de integridad,
 * tanto a nivel de persistencia como mediante anotaciones de validación (Bean Validation).
 * </p>
 *
 * <p><b>Cumplimiento de Reglas de Negocio y Validación (Reto SENA JPA):</b></p>
 * <ul>
 *   <li><b>Validación 1 (@NotBlank):</b> El nombre de la categoría es obligatorio y no debe contener solo espacios.</li>
 *   <li><b>Validación 2 (@Size):</b> El nombre debe tener una longitud comprendida entre 3 y 80 caracteres.</li>
 *   <li><b>Validación 3 (@Pattern):</b> Expresión regular que restringe caracteres especiales para mitigar inyecciones SQL y XSS.</li>
 *   <li><b>Validación 4 (@Size):</b> La descripción tiene un límite máximo de 500 caracteres.</li>
 *   <li><b>Validación 5 (@NotNull):</b> El indicador de estado activo/inactivo no permite valores nulos.</li>
 * </ul>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see com.workinx.backend.repository.CategoriaEmpleoJpaRepository
 * @see com.workinx.backend.service.CategoriaEmpleoJpaService
 */
@Entity
@Table(name = "categorias_empleo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaEmpleoEntity {

    /**
     * Identificador único autoincremental de la categoría de empleo (Clave Primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo de la categoría de empleo.
     * <p>
     * Se aplican múltiples validaciones:
     * 1. {@code @NotBlank}: Asegura que el valor no sea nulo ni consista en espacios en blanco.
     * 2. {@code @Size}: Delimita la extensión del nombre entre 3 y 80 caracteres.
     * 3. {@code @Pattern}: Limita los caracteres aceptados a alfanuméricos, acentos, espacios,
     *    comas, puntos y guiones, protegiendo contra ataques de inyección y scripts maliciosos.
     * </p>
     */
    @NotBlank(message = "El nombre de la categoría es obligatorio y no puede estar en blanco.")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres.")
    @Pattern(
        regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s,\\.\\-]+$", 
        message = "El nombre contiene caracteres especiales no permitidos (protección contra exploits y SQLi)."
    )
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Descripción detallada del alcance o tipo de ofertas laborales agrupadas en la categoría.
     * <p>
     * Validación {@code @Size}: Longitud máxima permitida de 500 caracteres.
     * Mapeado como columna de tipo {@code TEXT} en la base de datos para flexibilidad.
     * </p>
     */
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Indicador de activación de la categoría en la plataforma.
     * <p>
     * Permite borrado lógico o deshabilitación sin destruir la integridad referencial.
     * Validación {@code @NotNull}: Requiere un valor booleano explícito.
     * Por defecto se inicializa en {@code true}.
     * </p>
     */
    @NotNull(message = "El estado de activación de la categoría no puede ser nulo.")
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    /**
     * Marca de tiempo que registra el momento exacto en el que se creó la categoría.
     * <p>
     * Se configura con {@code updatable = false} para prevenir modificaciones posteriores
     * en operaciones de actualización de la entidad.
     * </p>
     */
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Método callback de ciclo de vida JPA ejecutado antes de persistir una nueva entidad.
     * <p>
     * Asigna automáticamente la fecha y hora actual en {@link #fechaCreacion} si no fue
     * proporcionada previamente, y garantiza que {@link #activa} tenga el valor por defecto {@code true}.
     * </p>
     */
    @PrePersist
    protected void onCreate() {
        // Inicializa la fecha de creación en el instante actual si viene nula
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        // Asegura que el estado por defecto sea activo si no se especificó
        if (this.activa == null) {
            this.activa = true;
        }
    }
}
