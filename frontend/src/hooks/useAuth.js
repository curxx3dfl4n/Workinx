/**
 * @file useAuth.js
 * @description Hook personalizado para acceder de forma simplificada al estado de autenticación.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useContext } from "react";
import { AuthContext } from "../context/AuthContext";

/**
 * Custom Hook para consumir el contexto global de autenticación de WorkInX.
 *
 * @returns {Object} Objeto de contexto con usuario, token, funciones login, logout y roles
 * @throws {Error} Si se invoca fuera de un componente envuelto por <AuthProvider>
 */
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth debe ser utilizado dentro de un AuthProvider");
  }

  return context;
}
