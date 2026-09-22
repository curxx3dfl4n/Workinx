/**
 * @file reportes.service.js
 * @description Módulo de servicios API para el reporte y moderación de entrevistas.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { apiRequest } from "./api";

/** Servicio de cliente para la creación de reportes sobre entrevistas. */
export const reportesService = {
  /**
   * Envía una denuncia o reporte de moderación sobre una entrevista.
   * @async
   * @param {Object} datos - Datos del reporte (entrevista_id, tipo_reporte, descripcion, evidencia)
   * @returns {Promise<Object>} Respuesta del servidor
   */
  async crearReporte(datos) {
    return apiRequest("/api/reportes", {
      method: "POST",
      body: JSON.stringify(datos),
    });
  },
};
