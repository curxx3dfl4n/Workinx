package com.workinx.microservicio.entity;

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
 * Se encuentra mapeada a la tabla relacional {@code categorias_empleo} y encapsula
 * las reglas de negocio, integridad de datos y validaciones de seguridad Bean Validation
 * (JSR 380 / Jakarta Validation) requeridas para la clasificación de vacantes laborales.
 * </p>
 * <p>
 * <b>Validaciones implementadas:</b>
 * <ul>
 *   <li><b>@NotBlank:</b> Garantiza que el nombre no sea nulo ni consista únicamente en espacios en blanco.</li>
 *   <li><b>@Size(min = 3, max = 80):</b> Restringe la longitud del nombre a un rango válido.</li>
 *   <li><b>@Pattern:</b> Aplica una lista blanca de caracteres permitidos para prevenir vulnerabilidades de Cross-Site Scripting (XSS) y SQL Injection.</li>
 *   <li><b>@Size(max = 500):</b> Delimita la descripción para evitar sobrecargas en la base de datos.</li>
 *   <li><b>@NotNull:</b> Asegura que el estado booleano de activación esté siempre definido.</li>
 * </ul>
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see jakarta.persistence.Entity
 * @see jakarta.persistence.Table
 */
@Entity
@Table(name = "categorias_empleo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaEmpleo {

    /**
     * Identificador único autoincremental de la categoría de empleo (Clave Primaria).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nombre descriptivo de la categoría de empleo.
     * <p>
     * Se valida que sea obligatorio, tenga una longitud adecuada y contenga únicamente
     * caracteres alfanuméricos y signos de puntuación seguros para evitar inyecciones.
     * </p>
     */
    // VALIDACIÓN 1, 2 y 3: @NotBlank, @Size, @Pattern
    @NotBlank(message = "El nombre de la categoría es obligatorio y no puede estar en blanco.")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres.")
    @Pattern(
        regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑ\\s,\\.\\-]+$", 
        message = "El nombre contiene caracteres no permitidos. Protección contra inyección de exploits/scripts."
    )
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    /**
     * Detalle o descripción extendida sobre el alcance de la categoría.
     * Longitud máxima permitida de 500 caracteres.
     */
    // VALIDACIÓN 4: @Size
    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres.")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    /**
     * Bandera lógica que indica si la categoría está activa para asignación de vacantes.
     * Por defecto es {@code true}.
     */
    // VALIDACIÓN 5: @NotNull
    @NotNull(message = "El estado de la categoría (activa) no puede ser nulo.")
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    /**
     * Marca de tiempo que registra el momento exacto de inserción en el sistema.
     * Es inmutable tras su creación inicial.
     */
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    /**
     * Método callback del ciclo de vida de JPA ejecutado antes de la persistencia inicial.
     * <p>
     * Establece la fecha y hora actual si no han sido fijadas previamente y asegura
     * que el estado de activación posea un valor predeterminado seguro en caso de ser nulo.
     * </p>
     */
    @PrePersist
    protected void onCreate() {
        // Asigna la fecha del sistema en caso de que no haya sido suministrada
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
        // Garantiza que el indicador de estado no quede en valor nulo antes de persistir
        if (this.activa == null) {
            this.activa = true;
        }
    }
}
