# WorkInX — Backend Spring Boot

Backend migrado de **Node.js + Express** a **Spring Boot 3.3 + JDBC**.

---

## ✅ Requisitos Previos

### 1. Java 17 o superior
Descarga e instala **Java 17 LTS** (o 21):
- 🔗 https://adoptium.net/es/temurin/releases/?version=17
- Selecciona: Windows → x64 → JDK → .msi

Verifica tras instalar (abre una terminal **nueva**):
```bash
java -version
# Debe mostrar: openjdk version "17..."
```

### 2. Maven 3.9+
Descarga **Apache Maven**:
- 🔗 https://maven.apache.org/download.cgi
- Descarga el `.zip` de "Binary zip archive"
- Extrae en `C:\maven`
- Agrega `C:\maven\bin` al PATH del sistema

Verifica:
```bash
mvn -version
# Debe mostrar: Apache Maven 3.x.x
```

---

## ⚙️ Configuración

Edita `src/main/resources/application.properties`:

```properties
# Puerto (mismo que Node.js original)
server.port=3000

# Base de datos (mismos valores que tu .env)
spring.datasource.url=jdbc:mysql://localhost:3306/workinx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota
spring.datasource.username=root
spring.datasource.password=          ← tu contraseña MySQL aquí

# JWT (misma clave que tenías en Node.js)
jwt.secret=tu_secreto_super_seguro
jwt.expiration=7200000

# URL pública del servidor (para generar URLs de CVs)
app.url=http://localhost:3000
```

---

## 🚀 Ejecutar el Servidor

```bash
cd backend-springboot
mvn spring-boot:run
```

El servidor arranca en: **http://localhost:3000**

---

## 🧪 Verificar que funciona

```bash
# Health check
curl http://localhost:3000/api/health

# Respuesta esperada:
# { "status": "ok", "mensaje": "...", "database": "connected" }
```

---

## 📡 Endpoints (exactamente los mismos que Node.js)

| Método | Ruta | Auth |
|--------|------|------|
| GET | `/api/health` | Público |
| POST | `/api/auth/registro-candidato` | Público |
| POST | `/api/auth/registro-empresa` | Público |
| POST | `/api/auth/login` | Público |
| GET | `/api/auth/perfil` | JWT |
| GET | `/api/entrevistas` | Público |
| GET | `/api/entrevistas/{id}` | Público |
| GET | `/api/entrevistas/empresa/mis-entrevistas` | JWT empresa |
| POST | `/api/entrevistas` | JWT empresa |
| PUT | `/api/entrevistas/{id}` | JWT empresa |
| DELETE | `/api/entrevistas/{id}` | JWT empresa |
| POST | `/api/postulaciones` | JWT candidato |
| GET | `/api/postulaciones/mis-postulaciones` | JWT candidato |
| GET | `/api/postulaciones/entrevista/{id}` | JWT empresa |
| PUT | `/api/postulaciones/{id}/estado` | JWT empresa |
| POST | `/api/reportes` | JWT |

---

## 🔄 Frontend React

El frontend **no necesita cambios**. El archivo `.env` del frontend sigue siendo:
```env
VITE_API_URL=http://localhost:3000
```

---

## 📁 Estructura del Proyecto

```
backend-springboot/
├── pom.xml                          # Dependencias Maven (= package.json)
└── src/main/java/com/workinx/backend/
    ├── WorkinxApplication.java      # Punto de entrada
    ├── config/
    │   ├── SecurityConfig.java      # Spring Security + CORS
    │   └── WebConfig.java           # Sirve /uploads/**
    ├── security/
    │   ├── JwtUtil.java             # Genera/valida JWT
    │   ├── JwtAuthFilter.java       # Interceptor Bearer token
    │   └── UsuarioAutenticado.java  # Record del usuario del token
    ├── controller/                  # Equivalente a routes + controllers
    │   ├── HealthController.java
    │   ├── AuthController.java
    │   ├── EntrevistasController.java
    │   ├── PostulacionesController.java
    │   └── ReportesController.java
    ├── service/                     # Lógica de negocio (= services de Node)
    │   ├── AuthService.java
    │   ├── EntrevistasService.java
    │   ├── PostulacionesService.java
    │   └── ReportesService.java
    ├── dto/                         # Objetos de entrada (= req.body)
    ├── exception/                   # Manejo centralizado de errores
    └── util/
        ├── Validators.java          # = utils/validators.js
        └── Formatters.java          # = utils/formatters.js
```
