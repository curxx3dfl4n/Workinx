/**
 * @file api.js
 * @description Cliente HTTP centralizado para peticiones a la API REST del backend de WorkInX.
 * Inyecta de forma transparente el token JWT de autorización desde el almacenamiento local.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */

const API_BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:3000";

/**
 * Función unificada para ejecutar solicitudes HTTP fetch al servidor backend.
 *
 * @async
 * @param {string} endpoint - Ruta relativa del recurso REST (ej: "/api/auth/login")
 * @param {RequestInit} [options={}] - Opciones de configuración de fetch (method, body, headers)
 * @returns {Promise<any>} Objeto de respuesta JSON parseado
 * @throws {Error} Error HTTP o error de conexión de red
 */
export async function apiRequest(endpoint, options = {}) {
  const token = localStorage.getItem("workinx_token");
  const headers = new Headers(options.headers || {});

  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  // Si no es FormData, se asigna Content-Type por defecto si se envía body
  const isFormData = options.body instanceof FormData;
  if (!isFormData && options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);

    let data = null;
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
      data = await response.json();
    }

    if (!response.ok) {
      const errorMessage =
        data?.mensaje || `Error en la petición: ${response.statusText} (${response.status})`;
      const error = new Error(errorMessage);
      error.status = response.status;
      error.data = data;
      throw error;
    }

    return data;
  } catch (error) {
    if (error.status) {
      throw error;
    }
    const networkError = new Error(
      "No se pudo conectar con el servidor. Verifica que el backend esté encendido."
    );
    networkError.original = error;
    throw networkError;
  }
}

export { API_BASE_URL };
