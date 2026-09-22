package com.workinx.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Clase de configuración para la integración condicional con la base de datos NoSQL MongoDB.
 * <p>
 * Implementa una arquitectura políglota tolerante a fallos: la persistencia en MongoDB solo se
 * habilita si la propiedad {@code spring.data.mongodb.uri} está expresamente declarada en la configuración
 * del entorno (por ejemplo en {@code application.properties} o variables de entorno).
 * </p>
 * <p>
 * Si dicha propiedad no está presente o se encuentra deshabilitada, Spring Boot omite la creación
 * de los beans de MongoDB, permitiendo que la aplicación continúe su ciclo de vida y arranque de manera
 * transparente con la persistencia relacional MySQL (JPA).
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @see org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
 * @see org.springframework.data.mongodb.repository.config.EnableMongoRepositories
 * @see com.workinx.backend.mongo.AuditLogMongoRepository
 */
@Configuration
@ConditionalOnProperty(name = "spring.data.mongodb.uri")
@EnableMongoRepositories(basePackages = "com.workinx.backend.mongo")
public class MongoConfig {

    /**
     * Logger para registrar la inicialización del contexto de datos NoSQL de MongoDB.
     */
    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    /**
     * Constructor predeterminado de la configuración de MongoDB.
     * <p>
     * Se invoca únicamente cuando la condición {@link ConditionalOnProperty} se satisface,
     * emitiendo un mensaje informativo en los logs del servidor.
     * </p>
     */
    public MongoConfig() {
        // Notificación en logs confirmando que los repositorios NoSQL fueron registrados en Spring
        log.info("🍃 MONGO CONFIG: Configuración de MongoDB habilitada.");
    }
}
