package com.workinx.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entidad JPA mapeada a la tabla 'entrevistas'.
 *
 * RETO 4: 5 Validaciones con Anotaciones:
 * 1. @NotBlank  -> título obligatorio
 * 2. @Size(min=5, max=200) -> longitud del título
 * 3. @Pattern   -> regex anti-SQLi/XSS en título
 * 4. @Size(max=1000) -> límite de descripción
 * 5. @NotNull   -> estado activa obligatorio
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Entity
@Table(name = "entrevistas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntrevistaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId = 1L;

    @Column(name = "categoria_id")
    private Long categoriaId = 1L;

    // VALIDACIÓN 1, 2 y 3
    @NotBlank(message = "El título de la entrevista es obligatorio.")
    @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres.")
    @Pattern(
        regexp = "^[a-zA-Z0-9áéíóúÁÉÍÓÚñÑüÜ\\s,\\.\\-\\/\\(\\)]+$",
        message = "El título contiene caracteres no permitidos (protección anti-exploits)."
    )
    @Column(name = "titulo", nullable = false)
    private String titulo;

    // VALIDACIÓN 4
    @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres.")
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "tipo")
    private String tipo = "primer_empleo";

    @Column(name = "modalidad")
    private String modalidad = "presencial";

    @Column(name = "ubicacion")
    private String ubicacion = "Medellín, Colombia";

    @Column(name = "lugar_entrevista")
    private String lugarEntrevista = "Oficina Principal / Remoto";

    @Column(name = "fecha_entrevista")
    private LocalDate fechaEntrevista;

    @Column(name = "hora_entrevista")
    private LocalTime horaEntrevista;

    @Column(name = "salario_a_convenir")
    private Boolean salarioAConvenir = true;

    @Column(name = "salario_min")
    private Double salarioMin = 0.0;

    @Column(name = "salario_max")
    private Double salarioMax = 0.0;

    @Column(name = "fecha_publicacion", updatable = false)
    private LocalDateTime fechaPublicacion;

    @Column(name = "fecha_edicion")
    private LocalDateTime fechaEdicion;

    @Column(name = "fecha_limite")
    private LocalDate fechaLimite;

    // VALIDACIÓN 5
    @NotNull(message = "El campo 'activa' no puede ser nulo.")
    @Column(name = "activa", nullable = false)
    private Boolean activa = true;

    @Column(name = "vistas")
    private Integer vistas = 0;

    @Column(name = "estado")
    private String estado = "publicada";

    @PrePersist
    protected void onCreate() {
        if (this.empresaId == null) this.empresaId = 1L;
        if (this.categoriaId == null) this.categoriaId = 1L;
        if (this.fechaPublicacion == null) this.fechaPublicacion = LocalDateTime.now();
        if (this.fechaEntrevista == null) this.fechaEntrevista = LocalDate.now().plusDays(7);
        if (this.horaEntrevista == null) this.horaEntrevista = LocalTime.of(9, 0);
        if (this.lugarEntrevista == null) this.lugarEntrevista = "Oficina Principal / Remoto";
        if (this.ubicacion == null) this.ubicacion = "Medellín, Colombia";
        if (this.fechaLimite == null) this.fechaLimite = LocalDate.now().plusDays(30);
        if (this.salarioAConvenir == null) this.salarioAConvenir = true;
        if (this.salarioMin == null) this.salarioMin = 0.0;
        if (this.salarioMax == null) this.salarioMax = 0.0;
        if (this.activa == null) this.activa = true;
        if (this.vistas == null) this.vistas = 0;
        if (this.estado == null) this.estado = "publicada";
        if (this.tipo == null) this.tipo = "primer_empleo";
        if (this.modalidad == null) this.modalidad = "presencial";
    }
}
