/**
 * @file postulaciones.service.js
 * @description Módulo de servicios API para la gestión de Postulaciones y Hojas de Vida.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { apiRequest } from "./api";

/** Servicio de cliente para el envío y revisión de postulaciones laborales. */
export const postulacionesService = {
  /**
   * Envía una nueva postulación adjuntando el archivo CV del candidato.
   * @async
   * @param {FormData} formData - Datos multipart con el archivo de hoja de vida
   * @returns {Promise<Object>} Resultado de la postulación
   */
  async crearPostulacion(formData) {
    return apiRequest("/api/postulaciones", {
      method: "POST",
      body: formData,
    });
  },

  /**
   * Obtiene las postulaciones realizadas por el candidato autenticado.
   * @async
   * @returns {Promise<Array<Object>>} Lista de postulaciones del usuario
   */
  async listarMisPostulaciones() {
    return apiRequest("/api/postulaciones/mis-postulaciones", {
      method: "GET",
    });
  },

  /**
   * Obtiene la lista de postulantes inscritos en una entrevista específica de la empresa.
   * @async
   * @param {number|string} entrevistaId - ID de la entrevista
   * @returns {Promise<Array<Object>>} Lista de candidatos postulados
   */
  async listarPostulacionesPorEntrevista(entrevistaId) {
    return apiRequest(`/api/postulaciones/entrevista/${entrevistaId}`, {
      method: "GET",
    });
  },

  /**
   * Actualiza el estado de la postulación de un candidato (aceptado, rechazado, revisado).
   * @async
   * @param {number|string} postulacionId - ID de la postulación
   * @param {string} estado - Nuevo estado
   * @returns {Promise<Object>} Postulación actualizada
   */
  async actualizarEstadoPostulacion(postulacionId, estado) {
    return apiRequest(`/api/postulaciones/${postulacionId}/estado`, {
      method: "PUT",
      body: JSON.stringify({ estado }),
    });
  },
};
