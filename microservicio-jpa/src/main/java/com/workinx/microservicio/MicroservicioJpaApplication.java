package com.workinx.microservicio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal y punto de entrada para el Microservicio JPA independiente (Servidor 2).
 * <p>
 * Este servicio fue diseñado e implementado en el marco del proyecto formativo WorkInX
 * para responder a los requerimientos técnicos del SENA, permitiendo que el monolito/aplicación
 * principal consuma de forma desacoplada un API RESTful respaldado por Spring Data JPA.
 * Gestiona de manera autónoma el ciclo de vida y persistencia de categorías de empleo.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.boot.SpringApplication
 */
@SpringBootApplication
public class MicroservicioJpaApplication {

    /**
     * Método de arranque principal de la aplicación Spring Boot.
     * <p>
     * Inicializa el contenedor de inversión de control (IoC), escanea los componentes,
     * configura la conexión a la base de datos relacional y levanta el servidor
     * web embebido (Tomcat) en el puerto configurado (8081 por defecto).
     * </p>
     *
     * @param args Argumentos de la línea de comandos transmitidos durante la ejecución.
     */
    public static void main(String[] args) {
        // Inicializa el contexto de Spring Boot y el servidor embebido
        SpringApplication.run(MicroservicioJpaApplication.class, args);

        // Imprime en consola la confirmación de despliegue y rutas de acceso rápido
        System.out.println("=================================================");
        System.out.println("🚀 Microservicio JPA (Servidor 2) iniciado en el puerto 8081");
        System.out.println("📌 Endpoint CRUD: http://localhost:8081/api/v1/microservicio/categorias");
        System.out.println("=================================================");
    }
}
