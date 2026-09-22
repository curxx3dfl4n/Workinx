package com.workinx.backend.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Componente de utilidad para el formateo y transformación de datos del sistema.
 * <p>
 * Convierte registros crudos retornado por la base de datos SQL a estructuras JSON
 * formateadas, legibles y amigables para el consumo del cliente web React.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Component
public class Formatters {

    /** URL base de la aplicación para la generación de enlaces estáticos. */
    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    /**
     * Formatea la etiqueta técnica del tipo de entrevista a texto amigable.
     *
     * @param tipo Clave técnica del tipo
     * @return Texto legible formateado
     */
    public String formatearTipo(String tipo) {
        if (tipo == null) return tipo;
        return switch (tipo) {
            case "primer_empleo" -> "Primer empleo";
            case "profesional"   -> "Profesional";
            case "emprendedor"   -> "Emprendedor";
            default              -> tipo;
        };
    }

    /**
     * Formatea la etiqueta de modalidad laboral.
     *
     * @param modalidad Clave técnica de modalidad
     * @return Texto legible (Presencial, Remoto, Híbrido)
     */
    public String formatearModalidad(String modalidad) {
        if (modalidad == null) return modalidad;
        return switch (modalidad) {
            case "presencial" -> "Presencial";
            case "remoto"     -> "Remoto";
            case "hibrido"    -> "Híbrido";
            default           -> modalidad;
        };
    }

    /**
     * Convierte objetos de fecha SQL a cadenas con formato {@code YYYY-MM-DD}.
     *
     * @param fecha Objeto de fecha retornado por la base de datos
     * @return Cadena formateada de la fecha
     */
    public String formatearFecha(Object fecha) {
        if (fecha == null) return null;
        String s = fecha.toString();
        if (s.length() >= 10) return s.substring(0, 10);
        return s;
    }

    /**
     * Formatea campos de tiempo o hora a cadena {@code HH:mm:ss}.
     *
     * @param hora Objeto de hora SQL
     * @return Cadena formateada de hora
     */
    public String formatearHora(Object hora) {
        if (hora == null) return null;
        return hora.toString();
    }

    /**
     * Evalúa si un objeto representando un indicador booleano o TINYINT es verdadero.
     *
     * @param val Valor a evaluar
     * @return {@code true} si equivale a verdadero, {@code false} en caso contrario
     */
    public boolean esVerdadero(Object val) {
        if (val == null)            return false;
        if (val instanceof Boolean b) return b;
        if (val instanceof Number n)  return n.intValue() != 0;
        return false;
    }

    /**
     * Construye la representación textual legible de la compensación salarial de una entrevista.
     *
     * @param e Mapa de propiedades de la entrevista
     * @return Cadena formateada como "$X.XXX.XXX - $Y.YYY.YYY" o "Salario a convenir"
     */
    public String crearSalarioTexto(Map<String, Object> e) {
        if (esVerdadero(e.get("salario_a_convenir"))) {
            return "Salario a convenir";
        }
        Object min = e.get("salario_min");
        Object max = e.get("salario_max");
        if (min == null || max == null) return "Salario a convenir";
        long minLong = ((Number) min).longValue();
        long maxLong = ((Number) max).longValue();
        if (minLong == maxLong) {
            return String.format("$%,d", minLong).replace(",", ".");
        }
        return String.format("$%,d - $%,d", minLong, maxLong)
                .replace(",", ".");
    }

    /**
     * Transforma una fila de resultados de la base de datos a un mapa JSON estandarizado para ofertas de entrevista.
     *
     * @param e Mapa con los datos crudos de la fila SQL
     * @return Mapa estructurado con datos parseados y formateados
     */
    public Map<String, Object> convertirEntrevista(Map<String, Object> e) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id",              e.get("id"));
        result.put("titulo",          e.get("titulo"));
        result.put("descripcion",     e.get("descripcion"));
        result.put("tipo",            formatearTipo(safeStr(e.get("tipo"))));
        result.put("modalidad",       formatearModalidad(safeStr(e.get("modalidad"))));
        result.put("ubicacion",       e.get("ubicacion"));
        result.put("lugarEntrevista", e.get("lugar_entrevista"));
        result.put("fechaEntrevista", formatearFecha(e.get("fecha_entrevista")));
        result.put("horaEntrevista",  formatearHora(e.get("hora_entrevista")));
        result.put("salarioAConvenir",esVerdadero(e.get("salario_a_convenir")));
        result.put("salarioMin",      e.get("salario_min"));
        result.put("salarioMax",      e.get("salario_max"));
        result.put("salarioTexto",    crearSalarioTexto(e));
        result.put("fechaPublicacion",formatearFecha(e.get("fecha_publicacion")));
        result.put("fechaEdicion",    formatearFecha(e.get("fecha_edicion")));
        result.put("fechaLimite",     formatearFecha(e.get("fecha_limite")));
        result.put("activa",          esVerdadero(e.get("activa")));
        result.put("estado",          e.get("estado"));
        result.put("empresa",         e.get("nombre_empresa"));
        result.put("categoria",       e.getOrDefault("categoria", "Sin categoría"));

        result.put("palabrasClave", splitPipe(safeStr(e.get("palabras_clave"))));
        result.put("requisitos",    splitPipe(safeStr(e.get("requisitos"))));

        result.put("latitud",  e.get("latitud"));
        result.put("longitud", e.get("longitud"));

        return result;
    }

    /**
     * Construye la URL pública accesible para la visualización o descarga de un archivo de hoja de vida (CV).
     *
     * @param cvPath Ruta relativa o nombre del archivo guardado en el servidor
     * @return URL absoluta del recurso
     */
    public String generarCvUrl(String cvPath) {
        if (cvPath == null || cvPath.isBlank()) return null;
        String nombre = new File(cvPath).getName();
        return appUrl + "/uploads/cv/" + nombre;
    }

    // ---- Helpers internos ----

    private String safeStr(Object val) {
        return val == null ? null : val.toString();
    }

    private List<String> splitPipe(String val) {
        if (val == null || val.isBlank()) return Collections.emptyList();
        return Arrays.asList(val.split("\\|\\|"));
    }
}
