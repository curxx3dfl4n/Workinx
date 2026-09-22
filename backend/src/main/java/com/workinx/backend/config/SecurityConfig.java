package com.workinx.backend.config;

import com.workinx.backend.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración central de seguridad de la aplicación basada en Spring Security.
 * <p>
 * Define la cadena de filtros de seguridad (Security Filter Chain), la política de sesiones stateless
 * (orientada a arquitectura JWT sin estado), las rutas públicas y protegidas por autenticación/roles,
 * el codificador de contraseñas BCrypt y la política CORS global.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /** Filtro personalizado para la validación e interceptación de tokens JWT. */
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Constructor para inyección de dependencias de Spring.
     *
     * @param jwtAuthFilter Filtro interceptor JWT
     */
    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Define la cadena de filtros de seguridad HTTP y las reglas de autorización por ruta.
     *
     * @param http Objeto HttpSecurity para configurar la seguridad web
     * @return Instancia construida de {@link SecurityFilterChain}
     * @throws Exception Si ocurre un error durante la configuración de seguridad
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // --- Rutas públicas ---
                .requestMatchers("/").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/health/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/test/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registro-candidato").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/registro-empresa").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/entrevistas").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/entrevistas/{id}").permitAll()
                // --- RETO SENA: Endpoints JPA CRUD públicos ---
                .requestMatchers("/api/v1/entrevistas-jpa/**").permitAll()
                // Servir archivos estáticos de uploads (CVs)
                .requestMatchers("/uploads/**").permitAll()
                // --- Todo lo demás requiere JWT ---
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Define el Bean codificador de contraseñas utilizando BCrypt con factor de costo 10.
     * Compatible con el estándar de cifrado seguro de contraseñas.
     *
     * @return Instancia de {@link PasswordEncoder} configurada con BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * Configuración global de CORS (Cross-Origin Resource Sharing).
     * Permite la comunicación segura entre el cliente Frontend (React) y la API REST Backend.
     *
     * @return Fuente de configuración CORS basada en URLs
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
