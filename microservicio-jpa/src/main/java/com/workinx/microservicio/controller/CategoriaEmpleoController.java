package com.workinx.microservicio.controller;

import com.workinx.microservicio.entity.CategoriaEmpleo;
import com.workinx.microservicio.service.CategoriaEmpleoService;
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
 * Controlador RESTful que expone los endpoints de gestión para las categorías de empleo (Servidor 2).
 * <p>
 * Provee la interfaz HTTP pública para el consumo desacoplado desde el monolito WorkInX u otros clientes:
 * <ul>
 *   <li><b>Paginación y Ordenamiento:</b> Soporta parámetros dinámicos de paginación {@link Pageable}.</li>
 *   <li><b>Búsquedas Multicriterio:</b> Modos condicionales con operadores lógicos {@code AND} y {@code OR}.</li>
 *   <li><b>Validación Declarativa:</b> Emplea {@link Valid} para activar las 5 restricciones JSR 380 de la entidad.</li>
 *   <li><b>Trazabilidad y Auditoría:</b> Captura la IP remota del cliente para la bitácora NoSQL.</li>
 *   <li><b>CORS Abierto:</b> Configurado para permitir integración sin restricciones entre orígenes cruzados.</li>
 * </ul>
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see com.workinx.microservicio.service.CategoriaEmpleoService
 * @see com.workinx.microservicio.entity.CategoriaEmpleo
 */
@RestController
@RequestMapping("/api/v1/microservicio/categorias")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class CategoriaEmpleoController {

    /**
     * Logger para registrar eventos HTTP entrantes, parámetros y respuestas procesadas.
     */
    private static final Logger log = LoggerFactory.getLogger(CategoriaEmpleoController.class);

    /**
     * Servicio de lógica de negocio inyectado.
     */
    private final CategoriaEmpleoService service;

    /**
     * Constructor con inyección de dependencias para el servicio de categorías.
     *
     * @param service Capa de negocio {@link CategoriaEmpleoService}.
     */
    @Autowired
    public CategoriaEmpleoController(CategoriaEmpleoService service) {
        this.service = service;
    }

    /**
     * Consulta categorías de empleo con soporte de paginación, ordenamiento dinámico y filtros avanzados.
     * <p>
     * Dependiendo del valor del parámetro {@code modo}, ejecuta:
     * <ul>
     *   <li>{@code modo=and}: Búsqueda combinada por nombre y estado activa (ambas obligatorias).</li>
     *   <li>{@code modo=or}: Búsqueda disyuntiva por nombre, descripción o estado activa (al menos una coincidencia).</li>
     *   <li>Sin modo o modo no reconocido: Listado general paginado estándar.</li>
     * </ul>
     * </p>
     *
     * @param page Número de página solicitado (base 0, valor por defecto: 0).
     * @param size Cantidad de registros por página (valor por defecto: 5).
     * @param sortBy Campo por el cual ordenar los resultados (valor por defecto: "id").
     * @param sortDir Dirección del ordenamiento ("asc" o "desc", valor por defecto: "asc").
     * @param modo Modalidad de filtrado ("and", "or", o nulo para paginación estándar).
     * @param nombre Subcadena para filtrar por nombre.
     * @param descripcion Subcadena para filtrar por descripción (aplicable en modo OR).
     * @param activa Filtro por estado booleano de activación.
     * @return {@link ResponseEntity} con la página {@link Page} de categorías encontradas y código HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<Page<CategoriaEmpleo>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String modo,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Boolean activa
    ) {
        log.info("ℹ️ INFO CONTROLLER: Petición GET recibida. Modo: '{}', Página: {}", modo, page);

        // Construcción de la dirección y criterio de ordenamiento solicitado
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // RETO 1A: BÚSQUEDA POR 2 CAMPOS CON OPERADOR AND (Y)
        if ("and".equalsIgnoreCase(modo) && nombre != null) {
            Boolean estado = (activa != null) ? activa : true;
            return ResponseEntity.ok(service.buscarPorDosCamposAnd(nombre, estado, pageable));
        }

        // RETO 1B: BÚSQUEDA POR 3 CAMPOS CON OPERADOR OR (O)
        if ("or".equalsIgnoreCase(modo)) {
            String qNombre = (nombre != null) ? nombre : "";
            String qDesc = (descripcion != null) ? descripcion : "";
            Boolean qActiva = (activa != null) ? activa : true;
            return ResponseEntity.ok(service.buscarPorTresCamposOr(qNombre, qDesc, qActiva, pageable));
        }

        // RETO 5: PAGINACIÓN ESTÁNDAR
        return ResponseEntity.ok(service.listarPaginado(pageable));
    }

    /**
     * Recupera el detalle de una categoría específica mediante su identificador numérico.
     *
     * @param id Identificador de la categoría a consultar.
     * @return {@link ResponseEntity} con el objeto {@link CategoriaEmpleo} y código HTTP 200 (OK).
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaEmpleo> obtenerPorId(@PathVariable Long id) {
        log.info("ℹ️ INFO CONTROLLER: Buscando categoría por ID {}", id);
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    /**
     * Registra una nueva categoría de empleo aplicando validación automática de campos.
     * <p>
     * La anotación {@code @Valid} intercepta el cuerpo de la petición y comprueba las reglas
     * de validación definidas en {@link CategoriaEmpleo} antes de ingresar a la lógica de negocio.
     * </p>
     *
     * @param categoria Objeto deserializado del cuerpo JSON de la petición.
     * @param request Objeto HTTP para extraer metadatos de red como la dirección IP remota.
     * @return {@link ResponseEntity} con la entidad creada y código HTTP 201 (Created).
     */
    @PostMapping
    public ResponseEntity<CategoriaEmpleo> crear(@Valid @RequestBody CategoriaEmpleo categoria, HttpServletRequest request) {
        log.info("ℹ️ INFO CONTROLLER: Creando nueva categoría con validación @Valid.");
        // Extrae la IP del cliente para enriquecer la bitácora de auditoría
        CategoriaEmpleo creada = service.crear(categoria, request.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /**
     * Actualiza los datos de una categoría de empleo existente.
     * <p>
     * Requiere que el cuerpo de la solicitud cumpla con las anotaciones {@code @Valid}.
     * </p>
     *
     * @param id Identificador de la categoría que se desea modificar.
     * @param datos Objeto con los nuevos valores para los campos de la categoría.
     * @param request Objeto HTTP para capturar la IP de la solicitud.
     * @return {@link ResponseEntity} con la categoría actualizada y código HTTP 200 (OK).
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaEmpleo> actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaEmpleo datos, HttpServletRequest request) {
        log.info("ℹ️ INFO CONTROLLER: Actualizando categoría ID {}", id);
        CategoriaEmpleo actualizada = service.actualizar(id, datos, request.getRemoteAddr());
        return ResponseEntity.ok(actualizada);
    }

    /**
     * Elimina una categoría de empleo del sistema a partir de su ID.
     *
     * @param id Identificador de la categoría a suprimir.
     * @param request Objeto HTTP para extraer la dirección IP del cliente solicitante.
     * @return {@link ResponseEntity} con un mapa que contiene el mensaje de confirmación y estado HTTP 200 (OK).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id, HttpServletRequest request) {
        log.info("ℹ️ INFO CONTROLLER: Eliminando categoría ID {}", id);
        service.eliminar(id, request.getRemoteAddr());

        // Estructura de respuesta estandarizada en formato JSON
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mensaje", "Categoría ID " + id + " eliminada exitosamente.");
        respuesta.put("status", HttpStatus.OK.value());

        return ResponseEntity.ok(respuesta);
    }
}
