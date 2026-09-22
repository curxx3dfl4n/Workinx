/**
 * @file auth.service.js
 * @description Módulo de servicios de cliente API para operaciones de Autenticación y Cuentas.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { apiRequest } from "./api";

/**
 * Servicio de cliente para la gestión de solicitudes de autenticación y registros de usuario.
 */
export const authService = {
  /**
   * Realiza la solicitud de inicio de sesión de usuario.
   *
   * @async
   * @param {string} correo - Correo electrónico del usuario
   * @param {string} password - Contraseña ingresada
   * @returns {Promise<{token: string, usuario: Object}>} Objeto con token JWT y datos de usuario
   */
  async login(correo, password) {
    return apiRequest("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ correo, password }),
    });
  },

  /**
   * Registra una nueva cuenta de tipo candidato en el sistema.
   *
   * @async
   * @param {Object} datos - Formulario de inscripción del candidato
   * @returns {Promise<Object>} Respuesta del servidor con usuario creado
   */
  async registrarCandidato(datos) {
    return apiRequest("/api/auth/registro-candidato", {
      method: "POST",
      body: JSON.stringify(datos),
    });
  },

  /**
   * Registra una nueva entidad empresarial en la plataforma.
   *
   * @async
   * @param {Object} datos - Formulario de registro corporativo de empresa
   * @returns {Promise<Object>} Respuesta del servidor con perfil de empresa
   */
  async registrarEmpresa(datos) {
    return apiRequest("/api/auth/registro-empresa", {
      method: "POST",
      body: JSON.stringify(datos),
    });
  },

  /**
   * Obtiene la información del perfil del usuario actualmente autenticado.
   *
   * @async
   * @returns {Promise<Object>} Datos detallados del perfil de usuario o empresa
   */
  async obtenerPerfil() {
    return apiRequest("/api/auth/perfil", {
      method: "GET",
    });
  },
};
