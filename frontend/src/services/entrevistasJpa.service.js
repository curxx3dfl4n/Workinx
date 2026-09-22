// URL de la API JPA de Entrevistas en el Backend Principal (Puerto 3000)
const JPA_ENTREVISTAS_URL = 'http://localhost:3000/api/v1/entrevistas-jpa';

/**
 * Servicio Frontend para consumir la API de Entrevistas JPA
 * Maneja errores de red de forma transparente para evitar mensajes crudos como "Failed to fetch".
 */
export const entrevistasJpaService = {
  // RETO 5 & 1: Listar con Paginación y Filtros (AND / OR)
  listarPaginado: async ({ page = 0, size = 5, modo = '', titulo = '', descripcion = '', modalidad = '', estado = '' }) => {
    let url = `${JPA_ENTREVISTAS_URL}?page=${page}&size=${size}`;
    
    if (modo) url += `&modo=${modo}`;
    if (titulo) url += `&titulo=${encodeURIComponent(titulo)}`;
    if (descripcion) url += `&descripcion=${encodeURIComponent(descripcion)}`;
    if (modalidad) url += `&modalidad=${encodeURIComponent(modalidad)}`;
    if (estado) url += `&estado=${encodeURIComponent(estado)}`;

    try {
      const res = await fetch(url);
      if (!res.ok) {
        const errorData = await res.json().catch(() => ({}));
        throw new Error(errorData.mensaje || errorData.error || `Error HTTP ${res.status}`);
      }
      return await res.json();
    } catch (err) {
      if (err.name === 'TypeError' || err.message === 'Failed to fetch') {
        // En lugar de lanzar "Failed to fetch", retornamos estado offline seguro
        return { content: [], totalPages: 0, totalElements: 0, serverOffline: true };
      }
      throw err;
    }
  },

  // GET: Obtener por ID
  obtenerPorId: async (id) => {
    try {
      const res = await fetch(`${JPA_ENTREVISTAS_URL}/${id}`);
      if (!res.ok) throw new Error(`Error HTTP ${res.status}`);
      return await res.json();
    } catch (err) {
      if (err.message === 'Failed to fetch') {
        throw new Error('No se pudo conectar con el servidor backend. Verifica que esté encendido.');
      }
      throw err;
    }
  },

  // POST: Crear nueva entrevista
  crear: async (datos) => {
    try {
      const res = await fetch(JPA_ENTREVISTAS_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
      });
      const data = await res.json();
      if (!res.ok) {
        if (data.detalles) {
          const errores = Object.values(data.detalles).join(' | ');
          throw new Error(`Error de validación: ${errores}`);
        }
        throw new Error(data.mensaje || data.error || `Error HTTP ${res.status}`);
      }
      return data;
    } catch (err) {
      if (err.message === 'Failed to fetch') {
        throw new Error('No se pudo conectar con el servidor backend (Puerto 3000). Asegúrate de encenderlo.');
      }
      throw err;
    }
  },

  // PUT: Actualizar entrevista
  actualizar: async (id, datos) => {
    try {
      const res = await fetch(`${JPA_ENTREVISTAS_URL}/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(datos)
      });
      const data = await res.json();
      if (!res.ok) {
        if (data.detalles) {
          const errores = Object.values(data.detalles).join(' | ');
          throw new Error(`Error de validación: ${errores}`);
        }
        throw new Error(data.mensaje || data.error || `Error HTTP ${res.status}`);
      }
      return data;
    } catch (err) {
      if (err.message === 'Failed to fetch') {
        throw new Error('No se pudo conectar con el servidor backend (Puerto 3000). Asegúrate de encenderlo.');
      }
      throw err;
    }
  },

  // DELETE: Eliminar entrevista
  eliminar: async (id) => {
    try {
      const res = await fetch(`${JPA_ENTREVISTAS_URL}/${id}`, {
        method: 'DELETE'
      });
      if (!res.ok) {
        const data = await res.json().catch(() => ({}));
        throw new Error(data.mensaje || `Error HTTP ${res.status}`);
      }
      return await res.json();
    } catch (err) {
      if (err.message === 'Failed to fetch') {
        throw new Error('No se pudo conectar con el servidor backend (Puerto 3000). Asegúrate de encenderlo.');
      }
      throw err;
    }
  }
};
