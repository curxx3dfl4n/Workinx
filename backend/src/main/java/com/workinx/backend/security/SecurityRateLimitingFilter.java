package com.workinx.backend.security;

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
 * Filtro de seguridad perimetral y limitación de tasa (Rate Limiting) para endpoints JPA.
 * <p>
 * Este componente intercepta las peticiones dirigidas a la ruta base {@code /api/v1/categorias-jpa/**}
 * y ejecuta dos capas de protección defensiva en profundidad:
 * </p>
 * <ol>
 *   <li><b>Protección Anti-Exploits (SQL Injection &amp; XSS):</b> Analiza los parámetros de consulta
 *       ({@code queryString}) contra patrones conocidos de ataque (como {@code UNION SELECT}, {@code DROP TABLE},
 *       inyecciones lógicas {@code ' OR '1'='1}, etiquetas {@code <script>} o esquemas {@code javascript:}).
 *       En caso de detección positiva, bloquea inmediatamente la solicitud retornando un estado HTTP 400 (Bad Request).
 *   </li>
 *   <li><b>Limitación de Tasa (Rate Limiting) en Memoria:</b> Mantiene una ventana temporal de 60 segundos
 *       asociada a la dirección IP del cliente, permitiendo un máximo configurable de solicitudes (200 req/min).
 *       Si se alcanza el 80% del límite, emite un log de advertencia (WARN); si se supera el máximo,
 *       bloquea la solicitud con estado HTTP 429 (Too Many Requests).
 *   </li>
 * </ol>
 *
 * <p><b>Aislamiento Operativo:</b></p>
 * <p>
 * El filtro omite deliberadamente cualquier otra ruta del sistema (por ejemplo, flujos de autenticación,
 * videollamadas o entrevistas) para no interferir con las operaciones productivas preexistentes.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see jakarta.servlet.Filter
 * @see jakarta.servlet.FilterChain
 */
@Component
public class SecurityRateLimitingFilter implements Filter {

    /**
     * Logger para registro de eventos de seguridad y anomalías de tráfico (SLF4J).
     */
    private static final Logger log = LoggerFactory.getLogger(SecurityRateLimitingFilter.class);

    /**
     * Umbral máximo de peticiones permitidas por cada dirección IP en una ventana de 60 segundos.
     */
    private static final int MAX_PETICIONES_POR_MINUTO = 200;

    /**
     * Mapa concurrente que almacena el contador de solicitudes atómicas por cada dirección IP cliente.
     */
    private final Map<String, AtomicInteger> contadoresIp = new ConcurrentHashMap<>();

    /**
     * Mapa concurrente que almacena la marca de tiempo (timestamp) de inicio de la ventana actual por IP.
     */
    private final Map<String, Long> tiemposInicioIp = new ConcurrentHashMap<>();

    /**
     * Patrón de expresión regular compilado para identificar firmas de inyección SQL y Cross-Site Scripting (XSS).
     * Se evalúa de manera insensible a mayúsculas y minúsculas (case-insensitive).
     */
    private static final Pattern SQLI_EXPLOIT_PATTERN = Pattern.compile(
            "(?i)(union\\s+select|drop\\s+table|drop\\s+database|<script>|javascript:|'\\s+or\\s+'1'\\s*=\\s*'1)",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Intercepta cada solicitud HTTP entrante para aplicar las reglas de seguridad y control de frecuencia.
     *
     * @param request  Petición de servlet entrante.
     * @param response Respuesta de servlet saliente.
     * @param chain    Cadena de filtros de Spring / Servlet.
     * @throws IOException      Si ocurre un error de entrada/salida durante el procesamiento.
     * @throws ServletException Si ocurre un error interno en la cadena del servlet.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();

        // 1. Filtrado selectivo: Solo aplica a los endpoints JPA del reto para garantizar aislamiento
        if (!uri.startsWith("/api/v1/entrevistas-jpa")) {
            chain.doFilter(request, response);
            return;
        }

        String ipCliente = obtenerIpCliente(httpRequest);
        String queryString = httpRequest.getQueryString();

        // 2. Detección proactiva de inyecciones SQL y vectores XSS en los parámetros de URL
        if (queryString != null && SQLI_EXPLOIT_PATTERN.matcher(queryString).find()) {
            log.error("🚨 ALERTA: Intento de inyección desde IP {}: {}?{}", ipCliente, uri, queryString);
            httpResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"mensaje\": \"Petición bloqueada por el filtro de seguridad.\"}");
            return;
        }

        // 3. Algoritmo de limitación de tasa (Rate Limiting con ventana fija de 60 segundos)
        long ahora = System.currentTimeMillis();
        tiemposInicioIp.putIfAbsent(ipCliente, ahora);
        contadoresIp.putIfAbsent(ipCliente, new AtomicInteger(0));

        // Si transcurrió más de 1 minuto desde el inicio de la ventana, reiniciar contador y timestamp
        if (ahora - tiemposInicioIp.get(ipCliente) > 60000) {
            tiemposInicioIp.put(ipCliente, ahora);
            contadoresIp.get(ipCliente).set(0);
        }

        int peticiones = contadoresIp.get(ipCliente).incrementAndGet();

        // Bloqueo cuando se supera la cuota máxima permitida (HTTP 429 Too Many Requests)
        if (peticiones > MAX_PETICIONES_POR_MINUTO) {
            log.error("🚨 Rate Limit excedido: IP {} bloqueada ({}/min).", ipCliente, MAX_PETICIONES_POR_MINUTO);
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"mensaje\": \"Límite de peticiones excedido. Intenta en 1 minuto.\"}");
            return;
        }

        // Alerta preventiva al alcanzar el 80% de la capacidad de tráfico asignada
        if (peticiones > MAX_PETICIONES_POR_MINUTO * 0.8) {
            log.warn("⚠️ Rate Limit: IP {} lleva {}/{} peticiones.", ipCliente, peticiones, MAX_PETICIONES_POR_MINUTO);
        }

        // Petición válida: continuar con el flujo estándar de la cadena de filtros
        chain.doFilter(request, response);
    }

    /**
     * Resuelve la dirección IP real del cliente solicitante.
     * <p>
     * Evalúa prioritariamente la cabecera {@code X-Forwarded-For} en caso de que la petición
     * atraviese proxies inversos o balanceadores de carga (como Nginx o Cloudflare),
     * utilizando {@link HttpServletRequest#getRemoteAddr()} como alternativa directa.
     * </p>
     *
     * @param request Solicitud HTTP de la cual se obtendrán las cabeceras de red.
     * @return Dirección IP del cliente en formato String.
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : request.getRemoteAddr();
    }
}
