package com.workinx.backend.util;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Componente de utilidad para las validaciones de negocio del sistema WorkInX.
 * <p>
 * Incluye validaciones de seguridad de contraseñas, longitud y tipo de documentos de identidad,
 * normalizaciones de modalidades, tipos de entrevistas y clasificación de Mipymes según empleo.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Component
public class Validators {

    /** Mapeo de rangos de empleados hacia clasificaciones legales de empresas. */
    private static final Map<String, String> CLASIFICACIONES_EMPRESA = Map.of(
            "1-10",    "microempresa",
            "11-50",   "pequena_empresa",
            "51-200",  "mediana_empresa",
            "201-500", "gran_empresa",
            "500+",    "macroempresa"
    );

    /**
     * Valida si una contraseña cumple las políticas de complejidad mínimas:
     * Al menos 8 caracteres, una mayúscula, un número y un carácter especial.
     *
     * @param password Cadena de texto de la contraseña a verificar
     * @return {@code true} si la contraseña es segura, {@code false} en caso contrario
     */
    public boolean validarPassword(String password) {
        if (password == null || password.isEmpty()) return false;
        boolean minimo    = password.length() >= 8;
        boolean mayuscula = password.matches(".*[A-ZÁÉÍÓÚÑ].*");
        boolean numero    = password.matches(".*[0-9].*");
        boolean especial  = password.matches(".*[!@#$%^&*(),.?\":{}|<>_\\-+=/\\\\\\[\\];'`~].*");
        return minimo && mayuscula && numero && especial;
    }

    /**
     * Valida que el documento de identidad cumpla con la longitud adecuada según su tipo.
     *
     * @param tipoDocumento Tipo de documento (TI, CC, CE, PPT)
     * @param documento Número numérico de documento
     * @return {@code true} si es un documento válido según su tipo
     */
    public boolean validarDocumento(String tipoDocumento, String documento) {
        if (documento == null || !documento.matches("^[0-9]+$")) return false;
        return switch (tipoDocumento) {
            case "TI"  -> documento.length() == 10;
            case "CC"  -> documento.length() >= 6 && documento.length() <= 10;
            case "CE"  -> documento.length() >= 6 && documento.length() <= 7;
            case "PPT" -> documento.length() == 7;
            default    -> false;
        };
    }

    /**
     * Normaliza el rango de edad seleccionado al valor guardado en la base de datos.
     *
     * @param edadRango Rango recibido desde el frontend
     * @return Cadena etiquetada {@code "menor_edad"} o {@code "mayor_edad"}
     */
    public String obtenerCategoriaEdad(String edadRango) {
        if ("-17".equals(edadRango)   || "menor_edad".equals(edadRango)) return "menor_edad";
        if ("+18".equals(edadRango)   || "mayor_edad".equals(edadRango)) return "mayor_edad";
        return null;
    }

    /**
     * Normaliza la naturaleza jurídica de la entidad (Pública o Privada).
     *
     * @param tipoEntidad Texto original de tipo de entidad
     * @return {@code "publica"}, {@code "privada"} o {@code null}
     */
    public String obtenerTipoEntidad(String tipoEntidad) {
        if (tipoEntidad == null) return null;
        String valor = tipoEntidad.trim().toLowerCase()
                .replace("á", "a").replace("é", "e")
                .replace("í", "i").replace("ó", "o").replace("ú", "u");
        if ("publica".equals(valor))  return "publica";
        if ("privada".equals(valor)) return "privada";
        return null;
    }

    /**
     * Retorna la clasificación legal de la empresa según el rango de empleados.
     *
     * @param rangoEmpleados Rango de personal (ej: "1-10", "11-50")
     * @return Clasificación Mipyme correspondiente
     */
    public String obtenerClasificacionEmpresa(String rangoEmpleados) {
        if (rangoEmpleados == null) return null;
        return CLASIFICACIONES_EMPRESA.getOrDefault(rangoEmpleados, null);
    }

    /**
     * Normaliza la modalidad/tipo de entrevista recibida.
     *
     * @param tipo Tipo de oferta
     * @return Cadena normalizada para persistencia SQL
     */
    public String normalizarTipoEntrevista(String tipo) {
        if (tipo == null) return null;
        String valor = tipo.trim().toLowerCase();
        if ("primer empleo".equals(valor) || "primer_empleo".equals(valor)) return "primer_empleo";
        if ("profesional".equals(valor))  return "profesional";
        if ("emprendedor".equals(valor))  return "emprendedor";
        return null;
    }

    /**
     * Normaliza la modalidad de trabajo elegida.
     *
     * @param modalidad Texto recibido para la modalidad
     * @return {@code "presencial"}, {@code "remoto"}, {@code "hibrido"} o {@code null}
     */
    public String normalizarModalidad(String modalidad) {
        if (modalidad == null) return null;
        String valor = modalidad.trim().toLowerCase()
                .replace("í", "i");
        return switch (valor) {
            case "presencial"           -> "presencial";
            case "remoto"               -> "remoto";
            case "hibrido", "híbrido"   -> "hibrido";
            default                     -> null;
        };
    }
}
