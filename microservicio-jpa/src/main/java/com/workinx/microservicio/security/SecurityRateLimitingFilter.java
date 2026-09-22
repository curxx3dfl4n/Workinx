package com.workinx.microservicio.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Filtro de seguridad perimetral a nivel de servlet (jakarta.servlet.Filter) para la protección de la API.
 * <p>
 * Este componente intercepta todas las peticiones entrantes antes de que alcancen la capa de controladores
 * Spring MVC y ejecuta dos mecanismos de defensa fundamentales:
 * <ol>
 *   <li><b>Detección y Bloqueo de Inyecciones (SQLi / XSS / Exploits):</b> Analiza los parámetros de consulta
 *       (query string) contra una expresión regular que identifica palabras clave sospechosas y patrones de ataque
 *       conocidos, respondiendo de inmediato con HTTP 400 (Bad Request).</li>
 *   <li><b>Limitación de Tasa (Rate Limiting en Memoria):</b> Restringe la frecuencia máxima de peticiones por cliente
 *       (60 peticiones por minuto por dirección IP) mediante estructuras de datos concurrentes seguras
 *       ({@link ConcurrentHashMap} y {@link AtomicInteger}), respondiendo con HTTP 429 (Too Many Requests)
 *       al detectar abusos o saturación.</li>
 * </ol>
 * </p>
 * <p>
 * Incorpora un sistema de alertas mediante SLF4J que genera registros de advertencia (WARN) cuando un cliente
 * alcanza el 80% de su cuota y registros de error (ERROR) ante intentos maliciosos o denegación de servicio.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see jakarta.servlet.Filter
 * @see org.springframework.stereotype.Component
 */
@Component
public class SecurityRateLimitingFilter implements Filter {

    /**
     * Instancia de logger para auditar accesos sospechosos y violaciones de tasa de consumo.
     */
    private static final Logger log = LoggerFactory.getLogger(SecurityRateLimitingFilter.class);

    /**
     * Umbral máximo permitido de peticiones por ventana de un minuto para una dirección IP individual.
     */
    private static final int MAX_PETICIONES_POR_MINUTO = 60;

    /**
     * Mapa concurrente que almacena el contador de solicitudes atómicas acumuladas por cada IP en la ventana activa.
     */
    private final Map<String, AtomicInteger> contadoresIp = new ConcurrentHashMap<>();

    /**
     * Mapa concurrente que conserva la marca de tiempo (milisegundos epoch) en que inició la ventana actual para cada IP.
     */
    private final Map<String, Long> tiemposInicioIp = new ConcurrentHashMap<>();

    /**
     * Patrón de expresión regular compilado que busca firmas clásicas de inyección SQL, scripting malicioso y secuencias de escape.
     */
    private static final Pattern SQLI_EXPLOIT_PATTERN = Pattern.compile(
            "(?i)(union\\s+select|select\\s+.*\\s+from|insert\\s+into|delete\\s+from|drop\\s+table|drop\\s+database|alter\\s+table|<script>|javascript:|or\\s+1=1|--|;)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Intercepta la petición HTTP y ejecuta las validaciones de seguridad perimetral y cuotas de consumo.
     *
     * @param request Petición servlet recibida.
     * @param response Respuesta servlet generada.
     * @param chain Cadena de filtros a través de la cual avanza el procesamiento si no es denegado.
     * @throws IOException Si ocurre una falla en la lectura/escritura del flujo HTTP.
     * @throws ServletException Si ocurre una anomalía en el contenedor de servlets.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Conversión a interfaces HTTP para acceder a cabeceras, URI y códigos de estado
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extracción de metadatos de la solicitud entrante
        String ipCliente = obtenerIpCliente(httpRequest);
        String uri = httpRequest.getRequestURI();
        String queryString = httpRequest.getQueryString();

        // =========================================================
        // 1. MILLA EXTRA: FILTRADO DE SQL INJECTION & EXPLOITS
        // =========================================================
        // Verifica si la cadena de parámetros contiene patrones maliciosos conocidos
        if (queryString != null && SQLI_EXPLOIT_PATTERN.matcher(queryString).find()) {
            log.error("🚨 ¡ALERTA DE ATAQUE DE INYECCIÓN / EXPLOIT! IP: {} intentó inyectar código sospechoso en la URI: {}?{}", ipCliente, uri, queryString);
            
            // Retorna respuesta inmediata de rechazo HTTP 400
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Petición bloqueada por el filtro de seguridad contra SQL Injection y Exploits.\"}");
            return;
        }

        // =========================================================
        // 2. MILLA EXTRA: RATE LIMITING (BLOQUEO DE PETICIONES POR MINUTO)
        // =========================================================
        long ahora = System.currentTimeMillis();
        // Inicializa estructuras concurrentes para clientes no registrados previamente
        tiemposInicioIp.putIfAbsent(ipCliente, ahora);
        contadoresIp.putIfAbsent(ipCliente, new AtomicInteger(0));

        long tiempoTranscurrido = ahora - tiemposInicioIp.get(ipCliente);

        // Si transcurrió más de 1 minuto (60,000 ms), se reinicia el ciclo de conteo
        if (tiempoTranscurrido > 60000) {
            tiemposInicioIp.put(ipCliente, ahora);
            contadoresIp.get(ipCliente).set(0);
        }

        // Incrementa atómicamente el contador de solicitudes de la IP
        int peticionesActuales = contadoresIp.get(ipCliente).incrementAndGet();

        // Advertencia preventiva a nivel WARN cuando se aproxima al 80% de la capacidad
        if (peticionesActuales > MAX_PETICIONES_POR_MINUTO * 0.8 && peticionesActuales <= MAX_PETICIONES_POR_MINUTO) {
            log.warn("⚠️ ADVERTENCIA DE SEGURIDAD (Rate Limit): La IP {} lleva {}/{} peticiones en el último minuto.", ipCliente, peticionesActuales, MAX_PETICIONES_POR_MINUTO);
        }

        // Bloqueo estricto a nivel ERROR al sobrepasar el límite configurado
        if (peticionesActuales > MAX_PETICIONES_POR_MINUTO) {
            log.error("🚨 ¡ALERTA DE DSI/BOTNET! La IP {} excedió el límite máximo ({} peticiones/minuto). Petición bloqueada.", ipCliente, MAX_PETICIONES_POR_MINUTO);

            // Genera respuesta de exceso de solicitudes HTTP 429
            httpResponse.setStatus(429); // 429 Too Many Requests
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\": \"Límite de peticiones por minuto excedido (Rate Limit Activo). IP bloqueada temporalmente.\"}");
            return;
        }

        // Si pasa todas las validaciones de seguridad, prosigue la cadena hacia los controladores
        chain.doFilter(request, response);
    }

    /**
     * Resuelve la dirección IP real del cliente considerando posibles servidores proxy inversos y balanceadores de carga.
     * <p>
     * Evalúa la cabecera estándar {@code X-Forwarded-For}. Si está presente, toma la primera dirección IP de la lista;
     * de lo contrario, utiliza la IP de conexión directa reportada por el socket mediante {@code request.getRemoteAddr()}.
     * </p>
     *
     * @param request Solicitud HTTP entrante.
     * @return Dirección IP del cliente en formato de cadena de texto.
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            // Extrae la primera IP de la cadena de reenvío por proxy
            return xForwardedFor.split(",")[0].trim();
        }
        // Retorna la IP remota directa de la conexión
        return request.getRemoteAddr();
    }
}
