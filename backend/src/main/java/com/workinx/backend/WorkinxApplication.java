package com.workinx.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal y punto de entrada de la plataforma WorkInX (Backend Spring Boot).
 * <p>
 * Inicializa el contenedor de Inyección de Dependencias, escanea componentes,
 * controladores y servicios del paquete {@code com.workinx.backend}.
 * </p>
 *
 * @author Equipo WorkInX - SENA ADSO 2026
 * @version 1.0.0
 */
@SpringBootApplication
public class WorkinxApplication {

    /**
     * Método principal que arranca la aplicación Spring Boot.
     *
     * @param args Argumentos de la línea de comandos pasados al iniciar el servidor
     */
    public static void main(String[] args) {
        SpringApplication.run(WorkinxApplication.class, args);
        System.out.println("✅ Servidor WorkInX (Spring Boot) corriendo en http://localhost:3000");
    }
}

