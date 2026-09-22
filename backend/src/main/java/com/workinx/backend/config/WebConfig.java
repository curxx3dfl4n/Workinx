package com.workinx.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Paths;

/**
 * Configuración de recursos estáticos de la aplicación Web MVC.
 * <p>
 * Mapea las solicitudes al endpoint {@code /uploads/**} hacia el directorio físico
 * de almacenamiento de archivos subidos (Hojas de vida / CVs en PDF/DOCX).
 * Equivalente a {@code app.use("/uploads", express.static(...))} en Express.js.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Ruta relativa o absoluta del directorio de archivos subidos.
     * Inyectada desde {@code application.properties} (propiedad {@code app.uploads.dir}).
     */
    @Value("${app.uploads.dir:uploads}")
    private String uploadsDir;

    /**
     * Registra los controladores de recursos estáticos del servidor.
     *
     * @param registry Registro de manejadores de recursos de Spring Web MVC
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absolutePath = Paths.get(uploadsDir).toAbsolutePath().toString();
        // Asegurar separador correcto en sistemas operacionales Windows/Linux
        if (!absolutePath.endsWith(File.separator)) {
            absolutePath = absolutePath + File.separator;
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
