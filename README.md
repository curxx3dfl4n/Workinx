<div align="center">
  <h1>🚀 WorkInX</h1>
  <p><strong>Plataforma de conexión laboral enfocada en el primer empleo</strong></p>
</div>

<hr />

## 📖 Acerca del Proyecto

**WorkInX** nace con la misión de democratizar el acceso a oportunidades laborales para jóvenes, recién egresados y personas sin experiencia. Muchas veces, el principal obstáculo para conseguir un trabajo es precisamente "la falta de experiencia previa". WorkInX soluciona esto conectando a **empresas** dispuestas a dar primeras oportunidades con **talento emergente**.

La plataforma permite a los candidatos buscar y postularse a entrevistas, y a las empresas publicar vacantes de manera estructurada.

## 🛠 Arquitectura y Tecnologías

El proyecto fue desarrollado utilizando el stack **MERN** (con MySQL en lugar de MongoDB) garantizando escalabilidad, velocidad y un desarrollo moderno.

### Frontend
- **React.js 19** + **Vite**: Interfaz de usuario rápida e interactiva.
- **React Router**: Manejo de rutas de la Single Page Application (SPA).
- **CSS Vanilla (Módulos)**: Estilos responsivos y personalizados, utilizando buenas prácticas de UI/UX, Flexbox y Grid.
- **Lucide React**: Sistema de iconografía minimalista y moderno.

### Backend
- **Node.js** + **Express**: Servidor backend rápido y ligero para la API REST.
- **MySQL2**: Conexión a la base de datos relacional.
- **JWT (JSON Web Tokens)**: Sistema de seguridad y autenticación sin estado.
- **Bcrypt.js**: Encriptación de contraseñas de usuarios y empresas.
- **Multer**: Manejo seguro de subida de archivos (currículums, etc).
- **Cors**: Políticas de seguridad de orígenes cruzados.

## 📂 Estructura de Directorios

```text
WorkInX/
│
├── frontend/                # Aplicación cliente (React 19 + Vite)
│   ├── src/
│   │   ├── assets/          # Imágenes estáticas e íconos del proyecto
│   │   ├── components/      # Componentes UI organizados
│   │   │   ├── layout/      # Header, Footer
│   │   │   └── modals/      # Modales de interacción (Postular, Reportar, etc.)
│   │   ├── context/         # Estado global (AuthContext)
│   │   ├── hooks/           # Custom hooks (useAuth)
│   │   ├── pages/           # Vistas organizadas por dominio
│   │   │   ├── auth/        # Login, Registro, RegistroEmpresa, RegistroUsuario
│   │   │   ├── entrevistas/ # Entrevistas, DetalleEntrevista
│   │   │   ├── perfil/      # PerfilEmpresa, PerfilUsuario
│   │   │   └── Home.jsx     # Página de inicio
│   │   ├── services/        # Capa de consumo API (Cliente HTTP, auth, entrevistas, etc.)
│   │   ├── styles/          # Hojas de estilo globales
│   │   └── utils/           # Formateadores y utilidades de interfaz
│   └── package.json         # Dependencias del cliente
│
├── backend/                 # Servidor API REST (Node.js + Express)
│   ├── src/
│   │   ├── config/          # Configuración y conexión a DB con fallbacks
│   │   ├── controllers/     # Controladores HTTP delgados
│   │   ├── middlewares/     # Interceptores (Auth JWT, Upload Multer, ErrorHandler)
│   │   ├── routes/          # Definición de endpoints de la API REST
│   │   ├── services/        # Lógica de negocio y persistencia en DB
│   │   └── utils/           # Validadores, constantes y formateadores
│   └── package.json         # Dependencias del servidor
│
├── docs/                   # Documentación oficial del proyecto
│   ├── MANUAL_TECNICO.md   # Manual Técnico completo (Arquitectura, DB, API, Seguridad)
│   └── MANUAL_USUARIO.md   # Manual de Usuario paso a paso (Candidatos, Empresas, Visitantes)
│
└── database/                # Scripts SQL de inicialización
    ├── workinx.sql          # Esquema de tablas y vistas principal
    ├── inserts_demo.sql     # Datos semilla (Seeders)
    └── procedures_triggers.sql # Procedimientos y triggers en base de datos
```

## 📚 Documentación Oficial del Proyecto

Para la entrega y evaluación técnica en el marco del programa formativo SENA (ADSO), se han elaborado los siguientes manuales detallados:

- 📘 **[Manual Técnico Completo](docs/MANUAL_TECNICO.md)**: Arquitectura del software, diagramas Mermaid, diccionario de datos, catálogo completo de endpoints de la API REST, seguridad, triggers y guía de despliegue.
- 👥 **[Manual de Usuario](docs/MANUAL_USUARIO.md)**: Guía paso a paso ilustrada para visitantes, candidatos (registro, postulación con CV, seguimiento de estados) y empresas (clasificación empresarial, publicación y gestión de postulantes).

## 🚀 Guía de Instalación y Ejecución Local

Para levantar el proyecto en tu entorno local, asegúrate de tener instalados **Node.js (v18+)** y **MySQL**.

### 1. Base de Datos
1. Abre tu gestor de MySQL (XAMPP, MySQL Workbench, etc).
2. Ejecuta los scripts en la carpeta `database/` en el siguiente orden:
   - `workinx.sql` (Crea la DB y las tablas)
   - `procedures_triggers.sql`
   - `inserts_demo.sql` (Opcional, carga datos de prueba)

### 2. Configurar el Backend
```bash
cd backend
npm install
```
Crea un archivo llamado `.env` en la raíz de la carpeta `backend` basado en `.env.example`:
```env
PORT=3000
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=tu_contraseña_aqui
DB_NAME=workinx
DB_PORT=3306
JWT_SECRET=tu_secreto_super_seguro
APP_URL=http://localhost:3000
```
Inicia el servidor en modo desarrollo:
```bash
npm run dev
```

### 3. Configurar el Frontend
Abre una nueva terminal en paralelo:
```bash
cd frontend
npm install
```
Crea un archivo llamado `.env` en la raíz de la carpeta `frontend` basado en `.env.example`:
```env
VITE_API_URL=http://localhost:3000
```
Inicia el servidor de desarrollo:
```bash
npm run dev
```
La aplicación abrirá automáticamente en `http://localhost:5173`.

---

## 📡 Endpoints Principales (API REST)

La API cuenta con endpoints protegidos por JWT. A continuación un resumen:

- **Sistema / Monitoreo:**
  - `GET /api/health` - Estado de conexión y salud del servidor y la base de datos.
- **Auth:**
  - `POST /api/auth/registro-candidato` - Registro de candidatos.
  - `POST /api/auth/registro-empresa` - Registro de empresas.
  - `POST /api/auth/login` - Autenticación universal.
  - `GET /api/auth/perfil` - (Protegido) Obtener datos del usuario logueado.
- **Entrevistas:**
  - `GET /api/entrevistas` - Listado con filtros.
  - `GET /api/entrevistas/:id` - Detalle de entrevista.
  - `GET /api/entrevistas/empresa/mis-entrevistas` - (Protegido) Listar entrevistas de la empresa.
  - `POST /api/entrevistas` - (Protegido) Crear entrevista (solo Empresas).
  - `PUT /api/entrevistas/:id` - (Protegido) Actualizar entrevista (solo Empresas).
  - `DELETE /api/entrevistas/:id` - (Protegido) Eliminar entrevista (solo Empresas).
- **Postulaciones:**
  - `POST /api/postulaciones` - (Protegido) Aplicar a una entrevista con CV (solo Candidatos).
  - `GET /api/postulaciones/mis-postulaciones` - (Protegido) Listar postulaciones del candidato.
  - `GET /api/postulaciones/entrevista/:id` - (Protegido) Ver postulantes a una entrevista (solo Empresas).
  - `PUT /api/postulaciones/:id/estado` - (Protegido) Actualizar estado de postulación (solo Empresas).
- **Reportes:**
  - `POST /api/reportes` - (Protegido) Reportar entrevista sospechosa.

## 🤝 Contribuciones
Este proyecto fue creado con el objetivo de fomentar el acceso al empleo joven. Siéntete libre de clonarlo, enviar Pull Requests o reportar incidencias (Issues) si encuentras algún problema en la interfaz o la API.
