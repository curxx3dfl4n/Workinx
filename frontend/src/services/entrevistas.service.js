/**
 * @file entrevistas.service.js
 * @description Módulo de servicios API para la gestión de Ofertas de Entrevistas.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { apiRequest } from "./api";

/** Servicio de cliente para la gestión de entrevistas. */
export const entrevistasService = {
  /**
   * Obtiene la lista completa de entrevistas publicadas.
   * @async
   * @returns {Promise<Array<Object>>} Lista de entrevistas
   */
  async listarEntrevistas() {
    return apiRequest("/api/entrevistas", {
      method: "GET",
    });
  },

  /**
   * Obtiene una entrevista por su ID.
   * @async
   * @param {number|string} id - Identificador de la entrevista
   * @returns {Promise<Object>} Detalle de la entrevista
   */
  async obtenerEntrevistaPorId(id) {
    return apiRequest(`/api/entrevistas/${id}`, {
      method: "GET",
    });
  },

  /**
   * Obtiene las entrevistas publicadas por la empresa autenticada.
   * @async
   * @returns {Promise<Array<Object>>} Lista de entrevistas propias
   */
  async listarMisEntrevistas() {
    return apiRequest("/api/entrevistas/empresa/mis-entrevistas", {
      method: "GET",
    });
  },

  /**
   * Crea una nueva oferta de entrevista.
   * @async
   * @param {Object} datos - Datos de la entrevista
   * @returns {Promise<Object>} Entrevista creada
   */
  async crearEntrevista(datos) {
    return apiRequest("/api/entrevistas", {
      method: "POST",
      body: JSON.stringify(datos),
    });
  },

  /**
   * Actualiza una entrevista existente.
   * @async
   * @param {number|string} id - ID de la entrevista
   * @param {Object} datos - Datos modificados
   * @returns {Promise<Object>} Entrevista actualizada
   */
  async actualizarEntrevista(id, datos) {
    return apiRequest(`/api/entrevistas/${id}`, {
      method: "PUT",
      body: JSON.stringify(datos),
    });
  },

  /**
   * Elimina una entrevista de la plataforma.
   * @async
   * @param {number|string} id - ID de la entrevista a eliminar
   * @returns {Promise<Object>} Resultado de la eliminación
   */
  async eliminarEntrevista(id) {
    return apiRequest(`/api/entrevistas/${id}`, {
      method: "DELETE",
    });
  },
};
