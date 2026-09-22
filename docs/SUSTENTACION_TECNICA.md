# 🎓 MANUAL DEFITINIVO Y GUIÓN DE SUSTENTACIÓN TÉCNICA
## PLATAFORMA WORKINX — PROYECTO FORMATIVO SENA (ADSO 2026)

---

## 📋 INDICE DE CONTENIDOS

1. **GUIÓN ORAL DE SUSTENTACIÓN PASO A PASO (Speech para los Evaluadores)**
2. **ARQUITECTURA DE SOFTWARE SPA + API REST EN PROFUNDIDAD**
3. **DESGLOSE DETALLADO DE CAPAS DE DISEÑO (Backend, Frontend y BD)**
4. **CATÁLOGO Y EXPLICACIÓN CÓDIGO A CÓDIGO DE PATRONES DE DISEÑO**
5. **ARQUITECTURA DE SEGURIDAD Y CRIPTOGRAFÍA (JWT + BCrypt + Filter Chain)**
6. **CONEXIÓN A BASE DE DATOS: HIKARICP, JDBCTEMPLATE, TRANSACCIONALIDAD Y TRIGGERS**
7. **FLUJO COMPLETO DE DATOS PASO A PASO (Petición HTTP a Registro SQL)**
8. **GLOSARIO COMPLETO DE ANOTACIONES DE SPRING BOOT**
9. **BANCO DE 10 PREGUNTAS TRAMPA DEL JURADO Y SUS RESPUESTAS TÉCNICAS**

---

## 🎙️ 1. GUIÓN ORAL DE SUSTENTACIÓN PASO A PASO

*Este guión está redactado en primera persona para que el aprendiz o equipo lo utilice directamente como libreto de defensa ante el panel de jurados.*

### ⏱️ Fase 1: Introducción y Contextualización (Minuto 0:00 - 2:00)
> **"Buenos días estimado jurado y profesorado.**  
> Presentamos **WorkInX**, una plataforma web diseñada para resolver una de las problemáticas sociales y laborales más críticas de nuestro entorno: la barrera del 'primer empleo' que enfrentan los jóvenes, recién egresados y personas sin experiencia laboral previa.
> 
> Las plataformas tradicionales filtran negativamente a los postulantes sin experiencia. **WorkInX** rompe este paradigma conectando a postulantes con empresas a través de **entrevistas laborales estructuradas**, categorizadas por tipo de empresa (Micro, Pequeña, Mediana y Grande), incorporando geolocalización en mapas interactivos y moderación comunitaria automática."

### ⏱️ Fase 2: Arquitectura y Capas de Diseño (Minuto 2:00 - 5:00)
> **"Desde la perspectiva de Ingeniería de Software**, hemos diseñado el sistema bajo una **Arquitectura Cliente-Servidor Desacoplada**:
> 
> * En el **Cliente (Frontend)** construimos una *Single Page Application* (SPA) utilizando **React 19 impulsado por Vite**, garantizando una experiencia fluida sin recargas de página.
> * En el **Servidor (Backend)** implementamos una API RESTful stateless con **Java 17 y Spring Boot 3**, encargada de procesar las reglas de negocio y garantizar la seguridad.
> * En la **Persistencia (Base de Datos)** empleamos **MySQL con el motor InnoDB**, gestionando la integridad relacional y ejecutando reglas automáticas mediante Triggers SQL.
> 
> Esta separación en capas desacopladas (*Layered Architecture*) nos otorga tres grandes ventajas: **Escalabilidad**, ya que la API REST puede alimentar en el futuro aplicaciones móviles Android/iOS; **Mantenibilidad**, pues el diseño de frontend no afecta la lógica de servidor; y **Seguridad Centralizada**."

### ⏱️ Fase 3: Capa de Seguridad y Autenticación (Minuto 5:00 - 8:00)
> **"La seguridad en WorkInX se basa en tres pilares:**
> 
> 1. **Autenticación Stateless con JWT (JSON Web Token):** No almacenamos sesiones en memoria del servidor. El usuario se autentica, recibe un token firmado criptográficamente con HMAC-SHA256 y en cada petición posterior inyecta la cabecera `Authorization: Bearer <token>`.
> 2. **Cifrado de Contraseñas con BCrypt:** Las claves se procesan con `BCryptPasswordEncoder` (Cost Factor 10). Ni los administradores ni la base de datos conocen la clave en texto plano.
> 3. **Intercepción por Filtro `JwtAuthFilter`:** Implementamos la clase `JwtAuthFilter` que extiende de `OncePerRequestFilter`. Cada solicitud HTTP privada es capturada, decodificada y validada antes de tocar los controladores REST."

### ⏱️ Fase 4: Persistencia y Conexión a Base de Datos (Minuto 8:00 - 11:00)
> **"Para la conexión a la base de datos MySQL no utilizamos un ORM pesado**, sino una combinación de **HikariCP** (el pool de conexiones JDBC de mayor rendimiento en Java) y **Spring `JdbcTemplate`**.
> 
> * **HikariCP** mantiene un pool de 10 conexiones activas reutilizables, eliminando la latencia de abrir sockets TCP por cada cliente.
> * **`JdbcTemplate` con `PreparedStatement`** parametriza cada variable (`?`), haciendo al sistema **100% inmune a Inyecciones SQL**.
> * La anotación **`@Transactional`** asegura que las operaciones compuestas respeten el estándar **ACID** (Haciendo Rollback si falla alguna inserción).
> * Finalmente, implementamos **Triggers SQL** como `trg_desactivar_entrevista_por_reportes`, el cual suspende automáticamente cualquier oferta que acumule 3 denuncias comunitarias, aliviando la carga de procesamiento del backend."

### ⏱️ Fase 5: Cierre y Demostración (Minuto 11:00 - 12:00)
> **"En conclusión**, WorkInX combina las mejores prácticas de desarrollo web moderno, patrones de diseño robustos y una infraestructura de base de datos eficiente e inmune a vulnerabilidades comunes. Quedamos a su entera disposición para responder sus preguntas técnicas."

---

## 📐 2. ARQUITECTURA DE SOFTWARE SPA + API REST EN PROFUNDIDAD

```mermaid
graph TD
    subgraph Cliente ["FRONTEND (SPA - React 19 + Vite)"]
        UI[Vistas y Componentes UI]
        Context[AuthContext - Estado Global]
        ServiceLayer[Cliente HTTP api.js]
        UI --> Context
        UI --> ServiceLayer
    end

    subgraph Red ["COMUNICACIÓN HTTP / HTTPS"]
        JSON[Payload JSON + Bearer JWT Header]
    end

    subgraph Servidor ["BACKEND (API REST - Java 17 + Spring Boot 3)"]
        SecurityChain[SecurityFilterChain + JwtAuthFilter]
        Controllers[Controladores REST @RestController]
        Services[Servicios de Negocio @Service]
        DTOs[Data Transfer Objects]
        
        SecurityChain --> Controllers
        Controllers --> Services
        Services --> DTOs
    end

    subgraph BD ["PERSISTENCIA (MySQL InnoDB)"]
        Pool[Pool HikariCP]
        Jdbc[Spring JdbcTemplate]
        Tables[(Tablas Relacionales)]
        Triggers[Triggers de Moderación]
        
        Pool --> Jdbc
        Jdbc --> Tables
        Tables --> Triggers
    end

    ServiceLayer -->|Petición Async Fetch| JSON
    JSON -->|Intercepción HTTP| SecurityChain
    Services -->|Consultas Parametrizadas| Pool
```

### ¿Por qué esta arquitectura y no un Monolito?
1. **Desacoplamiento Tecnológico:** El frontend (React) y el backend (Spring Boot) se compilan y despliegan de forma independiente.
2. **Reutilización de API:** La misma API REST sirve a la aplicación web actual y queda lista para consumir por clientes móviles o servicios de terceros.
3. **Rendimiento SPA:** En React, la navegación entre páginas no recarga el navegador ni solicita documentos HTML adicionales al servidor; únicamente se intercambian datos JSON ligeros.

---

## 🏢 3. DESGLOSE DETALLADO DE CAPAS DE DISEÑO

### 3.1 Capas del Backend (Spring Boot)

#### 1. Capa de Controladores (`@RestController`)
- **Ubicación:** `com.workinx.backend.controller.*`
- **Función:** Es la puerta de entrada de las peticiones HTTP.
- **Responsabilidades:**
  - Mapea verbos HTTP (`GET`, `POST`, `PUT`, `DELETE`).
  - Recibe y valida los cuerpos JSON transformándolos a DTOs (`@RequestBody`).
  - Extrae el usuario autenticado desde el contexto de seguridad (`@AuthenticationPrincipal`).
  - Devuelve respuestas estructuradas mediante `ResponseEntity` con códigos de estado HTTP semánticos (`200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `409 Conflict`).

#### 2. Capa de Seguridad (`Spring Security`)
- **Ubicación:** `com.workinx.backend.config.SecurityConfig` y `security.*`
- **Función:** Controla el acceso a las rutas antes de permitir el paso a los controladores.
- **Responsabilidades:**
  - Define rutas públicas (`/api/auth/login`, `/api/auth/registro-*`, `/api/entrevistas`) y rutas protegidas.
  - Intercepta el encabezado `Authorization: Bearer <token>` mediante `JwtAuthFilter`.
  - Habilita la política CORS global para la interacción con el cliente React.

#### 3. Capa de Servicios de Negocio (`@Service`)
- **Ubicación:** `com.workinx.backend.service.*`
- **Función:** Contiene la inteligencia y las reglas de dominio del sistema.
- **Responsabilidades:**
  - Valida reglas complejas (verificación de duplicados de correo/documento, cálculo de rangos de edad, validación de contraseñas seguras).
  - Gestiona transacciones con la base de datos usando la anotación `@Transactional`.
  - Cifra contraseñas utilizando `BCryptPasswordEncoder`.

#### 4. Capa de DTOs (`Data Transfer Objects`)
- **Ubicación:** `com.workinx.backend.dto.*`
- **Función:** Desacopla el modelo interno de base de datos de los datos que viajan por la red.
- **Responsabilidades:** Soporta alias de serialización Jackson (`@JsonAlias`) para aceptar tanto `camelCase` como `snake_case` sin romper el contrato de datos.

#### 5. Capa de Persistencia (`JdbcTemplate` + HikariCP)
- **Ubicación:** `com.workinx.backend.util.Formatters` e inyecciones `JdbcTemplate` en los servicios.
- **Función:** Ejecuta sentencias SQL parametrizadas directamente contra MySQL.

---

### 3.2 Capas del Frontend (React 19)

1. **Vistas y Páginas (`src/pages/`)**: Componentes de alto nivel que representan las pantallas de la SPA (`Home`, `Entrevistas`, `Login`, `PerfilUsuario`, `PerfilEmpresa`).
2. **Componentes UI y Modales (`src/components/`)**: Elementos modulares como `Header`, `Footer`, `MapaPicker` (con mapa Leaflet) y modales de interacción (`PostularEntrevistaModal`, `ReportarEntrevistaModal`).
3. **Context API (`src/context/AuthContext.jsx`)**: Almacena el estado global de la sesión (`token`, `usuario`, `isAuthenticated`, `esCandidato`, `esEmpresa`) y sincroniza los cambios con `localStorage`.
4. **Hooks Personales (`src/hooks/useAuth.js`)**: Encapsula el acceso al contexto de autenticación para consumirlo limpiamente en cualquier componente.
5. **Capa de Servicios HTTP (`src/services/`)**: Módulos cliente (`api.js`, `auth.service.js`, `entrevistas.service.js`) que encapsulan las llamadas nativas `fetch`.

---

## ⚙️ 4. CATÁLOGO Y EXPLICACIÓN CÓDIGO A CÓDIGO DE PATRONES DE DISEÑO

### 4.1 Inversión de Control (IoC) e Inyección de Dependencias (DI)
* **Concepto:** En lugar de instanciar las dependencias manualmente con `new AuthService()`, el contenedor IoC de Spring crea, administra e inyecta las instancias requeridas en los constructores.
* **Código en [AuthController.java](file:///c:/xampp/htdocs/WorkInX/backend/src/main/java/com/workinx/backend/controller/AuthController.java#L25-L31):**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final Validators validators;

    // Spring inyecta automáticamente los Beans de AuthService y Validators
    public AuthController(AuthService authService, Validators validators) {
        this.authService = authService;
        this.validators = validators;
    }
}
```

---

### 4.2 Data Transfer Object (DTO) Pattern
* **Concepto:** Evita exponer las tablas relacionales directamente en las respuestas HTTP y protege al backend contra ataques de asignación masiva (*Mass Assignment Vulnerability*).
* **Código en [LoginRequest.java](file:///c:/xampp/htdocs/WorkInX/backend/src/main/java/com/workinx/backend/dto/LoginRequest.java#L9-L13):**
```java
@Data
public class LoginRequest {
    private String correo;
    private String password;
}
```

---

### 4.3 Chain of Responsibility (Cadena de Filtros de Seguridad)
* **Concepto:** La petición HTTP recorre una serie de eslabones o filtros en cadena. Si un filtro falla (ej: token JWT caducado), la cadena se interrumpe y la petición no llega a los controladores.
* **Código en [SecurityConfig.java](file:///c:/xampp/htdocs/WorkInX/backend/src/main/java/com/workinx/backend/config/SecurityConfig.java#L36-L59):**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/login").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Filtro en cadena

    return http.build();
}
```

---

### 4.4 Singleton Pattern
* **Concepto:** Garantiza que una clase tenga una única instancia compartida durante toda la ejecución de la aplicación.
* **Código:** Todos los componentes anotados con `@Service`, `@Component` o `@Configuration` son manejados por Spring como **Singletons** por defecto.

---

### 4.5 Provider / Context Pattern (React)
* **Concepto:** Permite transmitir el estado global de la aplicación (como el token de sesión) a cualquier componente sin tener que pasar propiedades manualmente por cada nivel intermedio.
* **Código en [AuthContext.jsx](file:///c:/xampp/htdocs/WorkInX/frontend/src/context/AuthContext.jsx#L48-L63):**
```jsx
export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => localStorage.getItem("workinx_token"));
  const [usuario, setUsuario] = useState(...);

  const value = useMemo(() => ({
    token, usuario, isAuthenticated: Boolean(token), login, logout
  }), [token, usuario]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
```

---

### 4.6 Custom Hook Pattern (React)
* **Concepto:** Abstrae la complejidad del consumo del contexto de autenticación en una función ejecutable reutilizable.
* **Código en [useAuth.js](file:///c:/xampp/htdocs/WorkInX/frontend/src/hooks/useAuth.js#L4-L12):**
```javascript
export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth debe ser utilizado dentro de un AuthProvider");
  }
  return context;
}
```

---

## 🔒 5. ARQUITECTURA DE SEGURIDAD Y CRIPTOGRAFÍA

### 5.1 Estructura del JWT (JSON Web Token)
El token de autenticación devuelto al usuario tiene una estructura estándar de tres partes separadas por puntos (`.`):

```text
  HEADER.PAYLOAD.SIGNATURE
  
  1. Header: Algoritmo de firma ("HS256") y tipo de token ("JWT").
  2. Payload: Reclamaciones ("id": 15, "correo": "candidato@test.com", "rol": "candidato", "exp": 1757280000).
  3. Signature: Hash criptográfico generado con la clave secreta `jwt.secret`.
```

### 5.2 Algoritmo de Cifrado de Contraseñas (BCrypt)
Ubicación: [SecurityConfig.java](file:///c:/xampp/htdocs/WorkInX/backend/src/main/java/com/workinx/backend/config/SecurityConfig.java#L67-L69)

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```
* **Factor de Costo 10:** BCrypt aplica $2^{10} = 1024$ rondas de hashing por cada contraseña.
* **Salteado Aleatorio (*Salting*):** BCrypt genera un *salt* único de 128 bits para cada hash. Incluso si dos usuarios eligen la misma contraseña, sus hashes almacenados en la base de datos serán completamente distintos.

### 5.3 Proceso del Interceptor `JwtAuthFilter`
Ubicación: [JwtAuthFilter.java](file:///c:/xampp/htdocs/WorkInX/backend/src/main/java/com/workinx/backend/security/JwtAuthFilter.java#L34-L70)

1. Captura la solicitud HTTP.
2. Lee el encabezado `Authorization`.
3. Si comienza por `Bearer `, extrae la subcadena del token.
4. Invoca a `jwtUtil.parsearToken(token)` para validar la firma y la fecha de expiración.
5. Construye una instancia de `UsuarioAutenticado` y registra una autoridad con formato `ROLE_<ROL>` en `SecurityContextHolder.getContext().setAuthentication(...)`.
6. Si el token es inválido o expiró, limpia el contexto y Spring Security retorna un código de error `401 Unauthorized`.

---

## 🗄️ 6. CONEXIÓN A BASE DE DATOS: HIKARICP, JDBCTEMPLATE, TRANSACCIONALIDAD Y TRIGGERS

### 6.1 HikariCP (Pool de Conexiones JDBC)
Ubicación: [application.properties](file:///c:/xampp/htdocs/WorkInX/backend/src/main/resources/application.properties#L15-L18)

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=20000
```
- **Maximum Pool Size (10):** El servidor mantendrá un máximo de 10 conexiones activas abiertas simultáneamente con MySQL.
- **Minimum Idle (2):** Siempre habrá al menos 2 conexiones listas e inactivas esperando solicitudes entrantes.
- **Connection Timeout (20.000 ms):** Si un hilo espera más de 20 segundos por una conexión libre, se lanza un tiempo de espera excedido (*Timeout*).

---

### 6.2 Spring `JdbcTemplate` e Inmunidad a SQL Injection
En lugar de concatenar cadenas SQL de forma insegura:
`"SELECT * FROM usuarios WHERE correo = '" + correo + "'"` ❌ *(Vulnerable a SQL Injection)*

Utilizamos **`PreparedStatement` con `JdbcTemplate`**:
`jdbc.queryForList("SELECT id FROM usuarios WHERE correo = ? LIMIT 1", correo);` ✅ *(Inmune)*

#### ¿Por qué es inmune?
Al utilizar `?`, el motor de MySQL compila primero la estructura del comando SQL (Árbol de Sintaxis Abstracta - AST) y luego trata el valor de `correo` estrictamente como un dato literal, desarmando cualquier intento de inyectar comandos maliciosos como `' OR '1'='1`.

---

### 6.3 Manejo Transaccional (`@Transactional`)
En [AuthService.java](file:///c:/xampp/htdocs/WorkInX/backend/src/main/java/com/workinx/backend/service/AuthService.java#L39-L75):

```java
@Transactional
public Map<String, Object> registrarEmpresa(...) {
    // Paso 1: Inserción en la tabla `usuarios`
    // Paso 2: Inserción en la tabla `empresas` vinculada por el ID generado
}
```
* **Atomicidad (ACID):** Si el Paso 1 tiene éxito pero el Paso 2 falla (ej. por violación de clave foránea), la anotación `@Transactional` revoca inmediatamente los cambios del Paso 1 mediante un **ROLLBACK** SQL automático.

---

### 6.4 Triggers SQL en MySQL (Lógica de Moderación Autónoma)
Ubicación: [procedures_triggers.sql](file:///c:/xampp/htdocs/WorkInX/database/procedures_triggers.sql#L22-L40)

```sql
DELIMITER $$
CREATE TRIGGER `trg_desactivar_entrevista_por_reportes` 
AFTER INSERT ON `reportes_entrevistas` 
FOR EACH ROW BEGIN
    DECLARE total_reportes_activos INT;

    SELECT COUNT(*)
    INTO total_reportes_activos
    FROM reportes_entrevistas
    WHERE entrevista_id = NEW.entrevista_id
      AND estado IN ('pendiente', 'en_revision');

    -- Regla de suspensión comunitaria
    IF total_reportes_activos >= 3 THEN
        UPDATE entrevistas
        SET activa = FALSE,
            estado = 'pausada'
        WHERE id = NEW.entrevista_id;
    END IF;
END
$$
DELIMITER ;
```
* **Ventaja:** La regla se ejecuta directamente en la capa de datos. No depende de que el servidor Java esté activo o realice cómputos pesados en memoria; MySQL aplica la restricción de forma atómica y transparente.

---

## 🔄 7. FLUJO COMPLETO DE DATOS PASO A PASO

A continuación se detalla la traza completa de la ejecución para **Crear una Oferta de Entrevista**:

```text
1. [Empresa completa FormularioEntrevista.jsx]
   │  Llena título, salario, requisitos y selecciona coordenadas en MapaPicker.jsx
   ▼
2. [Petición HTTP en entrevistas.service.js]
   │  Invoca apiRequest("/api/entrevistas", { method: "POST", body: JSON.stringify(datos) })
   ▼
3. [Inyección de Token en api.js]
   │  api.js lee "workinx_token" de localStorage y agrega el encabezado:
   │  "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
   ▼
4. [Recepción en Servidor - JwtAuthFilter]
   │  Filtra la petición, valida la firma HMAC-SHA256 y coloca la entidad UsuarioAutenticado en SecurityContext.
   ▼
5. [Evaluación de Rutas en SecurityConfig]
   │  Verifica que el usuario posea la autoridad "ROLE_EMPRESA".
   ▼
6. [Mapeo en EntrevistasController.java]
   │  Mapea el JSON recibido al DTO CrearEntrevistaRequest y captura el usuario con @AuthenticationPrincipal.
   ▼
7. [Procesamiento en EntrevistasService.java]
   │  Ejecuta la validación de formato con Validators.java, calcula las coordenadas geograficas (latitud/longitud)
   │  y prepara la sentencia SQL INSERT parametrizada con PreparedStatement.
   ▼
8. [Ejecución en MySQL mediante HikariCP]
   │  Toma una conexión del pool, ejecuta el INSERT en las tablas entrevistas y entrevistas_requisitos.
   ▼
9. [Respuesta al Cliente]
   │  Devuelve un objeto JSON formateado por Formatters.java con código HTTP 201 Created.
```

---

## 📖 8. GLOSARIO COMPLETO DE ANOTACIONES DE SPRING BOOT

| Anotación | Capa | Función y Propósito Técnico |
| :--- | :--- | :--- |
| `@SpringBootApplication` | Raíz | Habilita el escaneo de componentes, la autoconfiguración de Spring y marca la clase principal con el método `main()`. |
| `@RestController` | Controlador | Combina `@Controller` y `@ResponseBody`. Convierte automáticamente los retornos de los métodos en JSON. |
| `@RequestMapping` | Controlador | Declara la ruta URL base a nivel de clase (ej: `@RequestMapping("/api/auth")`). |
| `@PostMapping` / `@GetMapping` | Controlador | Mapea las solicitudes HTTP que coinciden con los verbos `POST` (Creación) o `GET` (Lectura). |
| `@PutMapping` / `@DeleteMapping` | Controlador | Mapea solicitudes de modificación `PUT` o eliminación `DELETE`. |
| `@RequestBody` | Controlador | Deserializa el cuerpo del mensaje JSON recibido en la solicitud HTTP a una instancia Java DTO. |
| `@PathVariable` | Controlador | Extrae parámetros dinámicos incrustados en la plantilla URI (ej: `/api/entrevistas/{id}`). |
| `@AuthenticationPrincipal` | Controlador | Extrae el objeto del usuario autenticado guardado por el filtro de seguridad en el hilo de la petición. |
| `@Service` | Servicio | Indica que la clase contiene la lógica de negocio del dominio y debe ser registrada como Bean. |
| `@Component` | General | Marca una clase general (filtros, utilidades, validadores) como componente administrado por Spring. |
| `@Configuration` | Configuración | Indica que la clase contiene métodos anotados con `@Bean` para configurar Spring Boot. |
| `@Bean` | Configuración | Registra el objeto retornado por un método dentro del contenedor de Inyección de Dependencias. |
| `@EnableWebSecurity` | Seguridad | Activa los componentes y filtros de seguridad web de Spring Security. |
| `@Transactional` | Persistencia | Garantiza que las operaciones de base de datos ejecutadas en el método sigan las propiedades ACID. |

---

## ❓ 9. BANCO DE 10 PREGUNTAS TRAMPA DEL JURADO Y SUS RESPUESTAS TÉCNICAS

> **1. Jurado:** ¿Por qué utilizaron `JdbcTemplate` en lugar de un ORM como Hibernate / Spring Data JPA?  
> **Respuesta Senior:** *"Optamos por `JdbcTemplate` porque nos otorga control total sobre la optimización de las consultas SQL, eliminando el overhead de memoria y el problema de consultas N+1 común en los ORMs. Además, se adapta de forma nativa a la estructura relacional existente del proyecto y maximiza el rendimiento con HikariCP."*

> **2. Jurado:** ¿Cómo manejan el problema de los recursos compartidos si dos usuarios se postulan al mismo tiempo?  
> **Respuesta Senior:** *"El motor de almacenamiento MySQL InnoDB utiliza bloqueos a nivel de fila (*Row-Level Locking*). Al realizar postulaciones mediante transacciones atómicas anotadas con `@Transactional`, la base de datos gestiona el aislamiento de transacciones de forma segura."*

> **3. Jurado:** ¿Dónde se almacenan las Hojas de Vida (CV) y qué vulnerabilidades podrían surgir?  
> **Respuesta Senior:** *"Los archivos se almacenan en el sistema de archivos del servidor en la carpeta `uploads/cv/` con nombres únicos generados mediante timestamps. En `PostulacionesService` implementamos doble filtro: comprobación estricta de extensiones permitidas (`.pdf`, `.doc`, `.docx`) y límite de peso máximo de 5MB para evitar ataques de denegación de servicio (DoS por saturación de disco)."*

> **4. Jurado:** ¿Por qué el token JWT es seguro si cualquier persona puede decodificar su contenido en sitios como `jwt.io`?  
> **Respuesta Senior:** *"El token JWT no está diseñado para ser secreto, sino para ser **inalterable**. Aunque el cliente pueda leer el payload, no puede modificar datos (como cambiar su rol a 'admin') porque al hacerlo rompería la **firma digital**, la cual solo puede ser calculada con nuestra clave secreta del servidor utilizando HMAC-SHA256."*

> **5. Jurado:** ¿Qué ventaja tiene usar Vite sobre Create React App (CRA)?  
> **Respuesta Senior:** *"Vite utiliza empaquetado basado en módulos ES nativos (*ES Modules*) impulsado por esbuild (escrito en Go). Esto hace que el servidor de desarrollo inicie casi instantáneamente y las compilaciones de producción sean hasta 10 veces más rápidas que con Webpack/CRA."*

> **6. Jurado:** ¿Cómo mitigan la vulnerabilidad de Cross-Site Scripting (XSS) en el Frontend?  
> **Respuesta Senior:** *"React escapa automáticamente todas las variables renderizadas en el JSX antes de insertarlas en el DOM. Además, evitamos el uso de funciones peligrosas como `dangerouslySetInnerHTML`."*

> **7. Jurado:** ¿Cómo se gestionan las sesiones de usuario si el servidor Spring Boot se reinicia?  
> **Respuesta Senior:** *"Al ser una arquitectura de autenticación **Stateless**, el servidor no almacena sesiones en memoria. Si el servidor se reinicia, los usuarios conservan su token JWT en el cliente y pueden continuar realizando peticiones sin ser desconectados."*

> **8. Jurado:** ¿Qué sucede si la base de datos MySQL se cae? ¿Cómo responde el sistema?  
> **Respuesta Senior:** *"El cliente HTTP `api.js` en el frontend captura los errores de red o códigos HTTP 500 y presenta un mensaje amigable al usuario notificando la indisponibilidad temporal. En el backend, HikariCP reintenta la conexión hasta alcanzar el tiempo límite definido."*

> **9. Jurado:** ¿Qué es un DTO y qué pasaría si no se utilizara?  
> **Respuesta Senior:** *"Un DTO es un objeto de transferencia de datos. Si no se usara, tendríamos que exponer nuestras entidades internas directamente a la API, arriesgándonos a ataques de sobreescritura de campos sensibles (*Mass Assignment*) o enviando datos innecesarios a través de la red."*

> **10. Jurado:** ¿Cómo estructuraron el mapa de entrevistas presenciales?  
> **Respuesta Senior:** *"Integraciones mediante la librería open-source **Leaflet** y OpenStreetMap. La empresa selecciona las coordenadas mediante el componente `MapaPicker.jsx` capturando latitud y longitud decimales que se almacenan en la base de datos y se renderizan en `MapaVista.jsx`."*
