/**
 * @file categoriasJpa.service.js
 * @description Servicio cliente para la interacción con la API REST de Categorías JPA en el backend de WorkInX.
 * Proporciona métodos para operaciones CRUD completas, paginación interactiva y búsquedas avanzadas (AND / OR).
 * @author Equipo WorkInX - SENA ADSO 2026
 */

/**
 * URL base del endpoint de Categorías JPA en el Backend Principal (Servidor 1 - Puerto 3000).
 * Este endpoint actúa como pasarela/proxy hacia el Microservicio JPA (Servidor 2 - Puerto 8081).
 * @constant {string}
 */
const JPA_MAIN_BACKEND_URL = 'http://localhost:3000/api/v1/categorias-jpa';

/**
 * Objeto de servicio Frontend integrado con el Backend Principal de WorkInX.
 * Implementa soporte para paginación (Pageable), filtros multicriterio y validaciones de entidad.
 */
export const categoriasJpaService = {
  /**
   * Obtiene un listado paginado de categorías aplicando filtros opcionales (RETO 1 y RETO 5).
   * Soporta búsquedas combinadas mediante operadores lógicos AND (2 campos) y OR (3 campos).
   * 
   * @async
   * @function listarPaginado
   * @param {Object} opciones - Parámetros de consulta y configuración de paginación.
   * @param {number} [opciones.page=0] - Índice de la página solicitada (base 0).
   * @param {number} [opciones.size=5] - Cantidad de registros por página.
   * @param {string} [opciones.modo=''] - Modo de búsqueda ('and', 'or' o vacío para listar todas).
   * @param {string} [opciones.nombre=''] - Filtro por nombre de la categoría.
   * @param {string} [opciones.descripcion=''] - Filtro por descripción de la categoría.
   * @param {boolean|null} [opciones.activa=null] - Filtro por estado activo/inactivo (null para omitir).
   * @returns {Promise<Object>} Promesa que resuelve al objeto paginado (Spring Data Page) con content, totalPages, totalElements, etc.
   * @throws {Error} Lanza un error descriptivo si la petición HTTP falla o el servidor responde con error.
   */
  listarPaginado: async ({ page = 0, size = 5, modo = '', nombre = '', descripcion = '', activa = null }) => {
    // Construcción dinámica de la URL con los parámetros básicos de paginación
    let url = `${JPA_MAIN_BACKEND_URL}?page=${page}&size=${size}`;
    
    // Inclusión condicional de parámetros de búsqueda avanzada codificados para evitar fallos o inyecciones
    if (modo) {
      url += `&modo=${modo}`;
    }
    if (nombre) {
      url += `&nombre=${encodeURIComponent(nombre)}`;
    }
    if (descripcion) {
      url += `&descripcion=${encodeURIComponent(descripcion)}`;
    }
    if (activa !== null) {
      url += `&activa=${activa}`;
    }

    const res = await fetch(url);
    if (!res.ok) {
      // Extracción del mensaje de error retornado por el manejador de excepciones global
      const errorData = await res.json().catch(() => ({}));
      throw new Error(errorData.mensaje || errorData.error || `Error HTTP ${res.status}`);
    }
    return await res.json();
  },

  /**
   * Obtiene una categoría específica a partir de su identificador único.
   * 
   * @async
   * @function obtenerPorId
   * @param {number|string} id - Identificador numérico de la categoría.
   * @returns {Promise<Object>} Promesa que resuelve a los datos de la categoría encontrada.
   * @throws {Error} Lanza un error si la categoría no existe o ocurre un problema de red.
   */
  obtenerPorId: async (id) => {
    const res = await fetch(`${JPA_MAIN_BACKEND_URL}/${id}`);
    if (!res.ok) throw new Error(`Error HTTP ${res.status}`);
    return await res.json();
  },

  /**
   * Envía la solicitud para registrar una nueva categoría en el sistema (RETO 4: 5 Validaciones @Valid).
   * 
   * @async
   * @function crear
   * @param {Object} datosCategoria - Datos de la categoría a registrar.
   * @param {string} datosCategoria.nombre - Nombre de la categoría (@NotBlank, @Size, @Pattern).
   * @param {string} [datosCategoria.descripcion] - Descripción de la categoría (@Size max 500).
   * @param {boolean} [datosCategoria.activa] - Indicador de estado de la categoría (@NotNull).
   * @returns {Promise<Object>} Promesa que resuelve a la categoría recién creada confirmada por el backend.
   * @throws {Error} Lanza un error detallado si falla la validación de Bean Validation o la respuesta HTTP.
   */
  crear: async (datosCategoria) => {
    const res = await fetch(JPA_MAIN_BACKEND_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(datosCategoria)
    });
    const data = await res.json();
    if (!res.ok) {
      // Si el backend envía detalles de validación (@Valid), concatenar los mensajes para el usuario
      if (data.detalles) {
        const errores = Object.values(data.detalles).join(' | ');
        throw new Error(`Error de validación: ${errores}`);
      }
      throw new Error(data.mensaje || data.error || `Error HTTP ${res.status}`);
    }
    return data;
  },

  /**
   * Actualiza la información de una categoría existente mediante su ID.
   * 
   * @async
   * @function actualizar
   * @param {number|string} id - Identificador de la categoría a modificar.
   * @param {Object} datosActualizados - Nuevos datos para actualizar la categoría.
   * @param {string} datosActualizados.nombre - Nombre modificado de la categoría.
   * @param {string} [datosActualizados.descripcion] - Descripción modificada.
   * @param {boolean} [datosActualizados.activa] - Estado activo/inactivo modificado.
   * @returns {Promise<Object>} Promesa que resuelve a la entidad actualizada devuelta por la API.
   * @throws {Error} Lanza un error si falla la validación o si el registro no se encuentra.
   */
  actualizar: async (id, datosActualizados) => {
    const res = await fetch(`${JPA_MAIN_BACKEND_URL}/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(datosActualizados)
    });
    const data = await res.json();
    if (!res.ok) {
      // Manejo estructurado de fallos de validación en la actualización
      if (data.detalles) {
        const errores = Object.values(data.detalles).join(' | ');
        throw new Error(`Error de validación: ${errores}`);
      }
      throw new Error(data.mensaje || data.error || `Error HTTP ${res.status}`);
    }
    return data;
  },

  /**
   * Elimina una categoría del sistema por su identificador único.
   * 
   * @async
   * @function eliminar
   * @param {number|string} id - Identificador de la categoría a eliminar.
   * @returns {Promise<Object>} Promesa que resuelve al mensaje de confirmación de eliminación.
   * @throws {Error} Lanza un error si la categoría no existe o no pudo ser eliminada.
   */
  eliminar: async (id) => {
    const res = await fetch(`${JPA_MAIN_BACKEND_URL}/${id}`, {
      method: 'DELETE'
    });
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      throw new Error(data.mensaje || `Error HTTP ${res.status}`);
    }
    return await res.json();
  }
};
