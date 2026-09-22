<div align="center">
  <h1>🚀 WorkInX - Plataforma de Gestión de Entrevistas Laborales</h1>
  <p><strong>Microservicio JPA & Frontend React | SENA ADSO 2026</strong></p>
</div>

<hr />

## 📖 Acerca del Proyecto

**WorkInX** es una plataforma web integral diseñada para la gestión de entrevistas laborales y emparejamiento de vacantes de primer empleo. Permite a las empresas publicar vacantes y gestionar postulantes, y a los candidatos buscar entrevistas, filtrarlas por modalidades y postularse.

Desarrollada bajo estándares del **programa SENA (ADSO 2026)**, implementando **Spring Boot 3.4 + Java 21** en el Backend, **React + Vite** en el Frontend, y persistiéndose en **MySQL**.

---

## 🛠️ Stack Tecnológico

### Backend Principal & Microservicio JPA
- **Java 21** & **Spring Boot 3.4.4**
- **Spring Data JPA & Hibernate**: Persistencia relacional orientada a objetos.
- **Spring Validation (JSR-380)**: 5 Validaciones en Entidad (`@NotBlank`, `@Size`, `@Pattern`, `@NotNull`).
- **Spring Security & JWT**: Seguridad y autenticación stateless con tokens cifrados.
- **SLF4J & Logback**: Sistema de logs estructurados (`INFO`, `WARN`, `ERROR`).
- **Filter Servlet (Milla Extra)**: Protección Anti-SQL Injection, Anti-Exploits y Rate Limiting por IP (200 req/min).

### Frontend
- **React 19** + **Vite**: Single Page Application (SPA) ultra rápida.
- **React Router 7**: Enrutamiento declarativo del lado del cliente (`/reto-jpa`).
- **Lucide React & Leaflet**: Iconografía moderna y mapas interactivos.

### Base de Datos
- **MySQL 8.0 / MariaDB**: Motor relacional con llaves foráneas (`database/schema.sql`).

---

## 📂 Estructura Limpia del Proyecto

```text
WorkInX/
│
├── backend/                       # Backend Principal (Spring Boot 3.4 + Java 21)
│   ├── src/main/java/com/workinx/backend/
│   │   ├── config/                # Seguridad, CORS y configuraciones
│   │   ├── controller/            # Endpoints REST (/api/v1/entrevistas-jpa)
│   │   ├── dto/                   # Data Transfer Objects
│   │   ├── entity/                # Entidades JPA con 5 Validaciones JSR-380
│   │   ├── exception/             # GlobalExceptionHandler (@RestControllerAdvice)
│   │   ├── repository/            # Repositorio JPA con consultas AND / OR y Pageable
│   │   ├── security/              # SecurityRateLimitingFilter (Anti-SQLi + Rate Limit)
│   │   └── service/               # Lógica de negocio y logs SLF4J
│   ├── pom.xml                    # Dependencias Maven
│   ├── mvnw.cmd                   # Wrapper ejecutable de Maven (Windows)
│   └── mvnw                       # Wrapper ejecutable de Maven (Linux/Mac)
│
├── microservicio-jpa/             # Microservicio JPA Autónomo Independiente
│   ├── src/main/java/com/workinx/microservicio/
│   ├── pom.xml                    # Dependencias independientes
│   └── mvnw.cmd                   # Wrapper ejecutable de Maven autónomo
│
├── frontend/                      # Aplicación Cliente (React 19 + Vite)
│   ├── src/
│   │   ├── components/            # Header (Línea 62), Footer, Modales, Mapas
│   │   ├── context/               # AuthContext (Estado global de sesión JWT)
│   │   ├── pages/                 # EntrevistasJpaCrud (/reto-jpa), Home, Login, etc.
│   │   ├── services/              # Conectores HTTP Fetch / Axios
│   │   └── styles/                # Estilos globales y módulos CSS
│   └── package.json               # Dependencias del cliente React
│
├── database/                      # Scripts SQL para MySQL
│   ├── schema.sql                 # Creación del esquema relacional de tablas
│   ├── inserts_demo.sql           # Datos semilla de prueba
│   └── procedures_triggers.sql    # Triggers y procedimientos almacenados
│
└── docs/                          # Documentación del proyecto
    ├── MANUAL_TECNICO.md
    └── MANUAL_USUARIO.md
```

---

## 🚀 Guía de Instalación y Ejecución en OTRO Computador Paso a Paso

Para correr este proyecto en cualquier computador nuevo, sigue estos sencillos pasos:

### 📋 Pre-requisitos Necesarios:
1. **Java 21 JDK** (o Java 17+) instalado.
2. **Node.js (v18 o superior)** instalado.
3. **MySQL (XAMPP o MySQL Workbench)** activo en el puerto `3306`.

---

### 1️⃣ Paso 1: Configurar la Base de Datos (MySQL)
1. Inicia **MySQL** desde XAMPP o tu gestor de base de datos.
2. Ejecuta el script de creación ubicado en:
   `database/schema.sql` (Crea la base de datos `workinx` y sus tablas).
3. (Opcional) Ejecuta el script de datos demo:
   `database/inserts_demo.sql`.

---

### 2️⃣ Paso 2: Ejecutar el Backend (Spring Boot)
Abre una terminal en la carpeta `backend`:
```bash
cd backend
```
Ejecuta el servidor con el wrapper incluido (no requiere instalar Maven):
- **Windows:**
  ```cmd
  mvnw.cmd spring-boot:run
  ```
- **Linux / Mac:**
  ```bash
  ./mvnw spring-boot:run
  ```
El backend iniciará en **`http://localhost:3000`**.

---

### 3️⃣ Paso 3: Ejecutar el Frontend (React + Vite)
Abre una **segunda terminal** en la carpeta `frontend`:
```bash
cd frontend
npm install
npm run dev
```
La aplicación cliente iniciará en **`http://localhost:5173`**.

---

### 4️⃣ Paso 4 (Opcional): Ejecutar el Microservicio JPA Autónomo
Si deseas ejecutar la versión autónoma del microservicio JPA:
```bash
cd microservicio-jpa
mvnw.cmd spring-boot:run
```

---

## 🛡️ Retos SENA Cumplidos

- **Reto 1:** Consultas compuestas con `AND` (2 campos) y `OR` (3 campos) en JPA.
- **Reto 2:** Controlador global de excepciones (`GlobalExceptionHandler` con HTTP 400).
- **Reto 3:** Logs estructurados SLF4J (`INFO`, `WARN`, `ERROR`).
- **Reto 4:** 5 Validaciones en Entidad (`@NotBlank`, `@Size`, `@Pattern`, `@NotNull`).
- **Reto 5:** Paginación con `Pageable` de JPA y vista en React (`/reto-jpa`).
- **Milla Extra:** Filtro HTTP de seguridad contra SQL Injection, Exploits y Rate Limiting por IP.
