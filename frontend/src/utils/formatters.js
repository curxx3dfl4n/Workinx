/**
 * @file formatters.js
 * @description Utilidades de formateo visual y etiquetas para componentes de React.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */

/**
 * Convierte la clave de categoría de edad a texto amigable.
 * @param {string} categoriaEdad - Clave técnica ("mayor_edad", "menor_edad")
 * @returns {string} Texto formateado
 */
export const convertirEdad = (categoriaEdad) => {
  if (categoriaEdad === "mayor_edad") return "Mayor de edad";
  if (categoriaEdad === "menor_edad") return "Menor de edad";
  return "Sin clasificar";
};

/**
 * Convierte el estado técnico de la postulación a etiqueta con capitalización adecuada.
 * @param {string} estado - Clave del estado ("pendiente", "aceptado", etc.)
 * @returns {string} Estado traducido para la interfaz gráfica
 */
export const convertirEstado = (estado) => {
  const estados = {
    pendiente: "Pendiente",
    revisado: "Revisado",
    aceptado: "Aceptado",
    rechazado: "Rechazado",
    retirado: "Retirado",
  };

  return estados[estado] || estado;
};

/**
 * Formatea cadenas o marcas de tiempo a fechas formateadas en español de Colombia.
 * @param {string|Date} fecha - Valor de fecha
 * @returns {string} Fecha formateada en texto largo
 */
export const formatearFecha = (fecha) => {
  if (!fecha) return "Sin fecha";

  return new Date(fecha).toLocaleDateString("es-CO", {
    year: "numeric",
    month: "long",
    day: "numeric",
  });
};

/**
 * Convierte la clasificación técnica de la empresa a texto legal Mipyme.
 * @param {string} clasificacion - Clave Mipyme
 * @returns {string} Nombre legal formateado
 */
export const convertirClasificacion = (clasificacion) => {
  const valores = {
    microempresa: "Microempresa",
    pequena_empresa: "Pequeña empresa",
    mediana_empresa: "Mediana empresa",
    gran_empresa: "Gran empresa",
    macroempresa: "Macroempresa",
  };

  return valores[clasificacion] || "Sin clasificación";
};

/**
 * Convierte la naturaleza jurídica de la empresa a etiqueta traducida.
 * @param {string} tipoEntidad - "publica" o "privada"
 * @returns {string} Etiqueta con tilde ("Pública", "Privada")
 */
export const convertirTipoEntidad = (tipoEntidad) => {
  if (tipoEntidad === "publica") return "Pública";
  if (tipoEntidad === "privada") return "Privada";
  return "Sin definir";
};
