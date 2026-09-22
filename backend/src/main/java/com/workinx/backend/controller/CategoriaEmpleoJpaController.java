package com.workinx.backend.controller;

import com.workinx.backend.entity.CategoriaEmpleoEntity;
import com.workinx.backend.service.CategoriaEmpleoJpaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador RESTful para la administración integral de categorías de empleo en WorkInX.
 * <p>
 * Expone endpoints HTTP bajo la ruta base {@code /api/v1/categorias-jpa}, integrando
 * mecanismos de paginación dinámica, filtrado multi-criterio, validación estricta de payloads
 * y captura de direcciones IP para auditoría.
 * </p>
 *
 * <p><b>Requerimientos del Reto SENA JPA cumplidos:</b></p>
 * <ul>
 *   <li><b>Reto 1 (Consultas AND / OR):</b> Parámetro {@code modo} para conmutar entre búsqueda por 2 campos (AND) o 3 campos (OR).</li>
 *   <li><b>Reto 3 (Emisión de Logs):</b> Trazabilidad de cada solicitud HTTP con niveles {@code INFO}, {@code WARN} y {@code ERROR}.</li>
 *   <li><b>Reto 4 (Validaciones con @Valid):</b> Validación automática de las 5 reglas definidas en {@link CategoriaEmpleoEntity}.</li>
 *   <li><b>Reto 5 (Paginación Dinámica):</b> Soporte para parámetros {@code page}, {@code size}, {@code sortBy} y {@code sortDir}.</li>
 * </ul>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see com.workinx.backend.service.CategoriaEmpleoJpaService
 * @see com.workinx.backend.entity.CategoriaEmpleoEntity
 */
@RestController
@RequestMapping("/api/v1/categorias-jpa")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class CategoriaEmpleoJpaController {

    /**
     * Instancia de logger SLF4J para registrar las peticiones entrantes y sus parámetros.
     */
    private static final Logger log = LoggerFactory.getLogger(CategoriaEmpleoJpaController.class);

    /**
     * Servicio de lógica de negocio para la gestión de categorías.
     */
    private final CategoriaEmpleoJpaService service;

    /**
     * Constructor para la inyección de dependencias del servicio de categorías.
     *
     * @param service Instancia de {@link CategoriaEmpleoJpaService}.
     */
    @Autowired
    public CategoriaEmpleoJpaController(CategoriaEmpleoJpaService service) {
        this.service = service;
    }

    /**
     * Consulta categorías de empleo de forma paginada con capacidades de filtrado condicional (AND / OR).
     * <p>
     * Según el valor del parámetro {@code modo}:
     * <ul>
     *   <li>{@code modo = "and"}: Aplica el <b>Reto 1A</b> (filtra por coincidencia de nombre Y estado activo).</li>
     *   <li>{@code modo = "or"}: Aplica el <b>Reto 1B</b> (filtra por coincidencia de nombre O descripción O estado activo).</li>
     *   <li>Por defecto: Aplica el <b>Reto 5</b> (retorna la lista paginada estándar según los criterios de ordenamiento).</li>
     * </ul>
     * </p>
     *
     * @param page        Índice de la página solicitada (0-indexado, por defecto 0).
     * @param size        Cantidad máxima de elementos por página (por defecto 5).
     * @param sortBy      Campo de la entidad por el cual se ordenarán los resultados (por defecto "id").
     * @param sortDir     Dirección del ordenamiento: "asc" (ascendente) o "desc" (descendente, por defecto "asc").
     * @param modo        Modo de búsqueda opcional: "and", "or" o vacío para listado general.
     * @param nombre      Filtro opcional por subcadena en el nombre de la categoría.
     * @param descripcion Filtro opcional por subcadena en la descripción de la categoría (usado en modo OR).
     * @param activa      Filtro opcional por estado de activación (booleano).
     * @return {@link ResponseEntity} con la página {@link Page} de categorías y estado HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<Page<CategoriaEmpleoEntity>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String modo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Boolean activa
    ) {
        log.info("ℹ️ INFO CONTROLLER MAIN: GET paginado. Modo: '{}', Pág: {}", modo, page);

        // Construcción del objeto Sort evaluando la dirección de ordenamiento solicitada
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // RETO 1A: Búsqueda combinada por 2 Campos con conjunción AND
        if ("and".equalsIgnoreCase(modo) && nombre != null) {
            Boolean estado = (activa != null) ? activa : true;
            return ResponseEntity.ok(service.buscarPorDosCamposAnd(nombre, estado, pageable));
        }

        // RETO 1B: Búsqueda flexible por 3 Campos con disyunción OR
        if ("or".equalsIgnoreCase(modo)) {
            String qNombre = (nombre != null) ? nombre : "";
            String qDesc = (descripcion != null) ? descripcion : "";
            Boolean qActiva = (activa != null) ? activa : true;
            return ResponseEntity.ok(service.buscarPorTresCamposOr(qNombre, qDesc, qActiva, pageable));
        }

        // RETO 5: Retorno de paginación estándar sin filtros adicionales
        return ResponseEntity.ok(service.listarPaginado(pageable));
    }

    /**
     * Obtiene los datos detallados de una categoría específica a partir de su ID.
     *
     * @param id Identificador numérico de la categoría a consultar.
     * @return {@link ResponseEntity} con la entidad encontrada y código de estado 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaEmpleoEntity> obtenerPorId(@PathVariable Long id) {
        log.info("ℹ️ INFO CONTROLLER MAIN: Buscando categoría por ID {}", id);
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    /**
     * Crea y registra una nueva categoría de empleo aplicando validación de datos.
     * <p>
     * Cumple con el <b>Reto 4</b>: El uso de {@link Valid} activa la evaluación de las
     * anotaciones de Bean Validation en {@link CategoriaEmpleoEntity} (@NotBlank, @Size, @Pattern, @NotNull).
     * Además, extrae la IP del cliente desde {@link HttpServletRequest} para la auditoría NoSQL.
     * </p>
     *
     * @param categoria Objeto con los datos de la categoría recibido en el cuerpo JSON de la petición.
     * @param request   Petición HTTP entrante para extraer metadatos de red (dirección IP remota).
     * @return {@link ResponseEntity} con la entidad creada y código de estado 201 (CREATED).
     */
    @PostMapping
    public ResponseEntity<CategoriaEmpleoEntity> crear(@Valid @RequestBody CategoriaEmpleoEntity categoria, HttpServletRequest request) {
        log.info("ℹ️ INFO CONTROLLER MAIN: Creando categoría con @Valid");
        CategoriaEmpleoEntity creada = service.crear(categoria, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Actualiza los datos de una categoría de empleo existente identificada por su ID.
     * <p>
     * Valida la estructura del payload mediante {@link Valid} y registra la dirección IP
     * remota para la trazabilidad de la modificación.
     * </p>
     *
     * @param id      Identificador de la categoría a modificar.
     * @param datos   Cuerpo de la petición con los nuevos valores a actualizar.
     * @param request Solicitud HTTP entrante utilizada para obtener la IP del cliente.
     * @return {@link ResponseEntity} con la entidad actualizada y código de estado 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaEmpleoEntity> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaEmpleoEntity datos, HttpServletRequest request) {
        log.info("ℹ️ INFO CONTROLLER MAIN: Actualizando categoría ID {}", id);
        CategoriaEmpleoEntity actualizada = service.actualizar(id, datos, request.getRemoteAddr());
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Elimina una categoría de empleo del sistema por su identificador primario.
     * <p>
     * Registra el evento de eliminación en los logs del servidor.
     * </p>
     *
     * @param id      Identificador primario de la categoría a eliminar.
     * @param request Solicitud HTTP para obtener la dirección IP del emisor.
     * @return {@link ResponseEntity} conteniendo un mapa informativo con el resultado y código 200 (OK).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id, HttpServletRequest request) {
        log.info("ℹ️ INFO CONTROLLER MAIN: Eliminando categoría ID {}", id);
        service.eliminar(id, request.getRemoteAddr());

        // Construcción de la respuesta JSON informativa
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Categoría ID " + id + " eliminada correctamente.");
        respuesta.put("status", HttpStatus.OK.value());

        return ResponseEntity.ok(respuesta);
    }
}
