/**
 * @file Home.jsx
 * @description Vista principal/Landing Page institucional de la plataforma WorkInX.
 * Presenta la propuesta de valor, sección de beneficios para jóvenes/empresas y acceso directo a convocatorias.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { Link } from "react-router";
import {
  Search,
  Building2,
  UserRoundCheck,
  Target,
  Eye,
  UsersRound,
} from "lucide-react";

/**
 * Componente funcional de la página de Inicio (Landing Page).
 * @returns {JSX.Element} Pantalla principal Home
 */
function Home() {
  return (
    <>
      <section className="home-hero" id="inicio">
        <div className="hero-overlay">
          <div className="hero-content">
            <p className="tag">Plataforma de entrevistas laborales</p>

            <h1>
              Encuentra oportunidades laborales pensadas para jóvenes y personas
              sin experiencia.
            </h1>

            <p className="hero-text">
              WorkInX conecta a candidatos con empresas que publican entrevistas,
              vacantes y oportunidades de primer empleo de forma organizada,
              sencilla y accesible.
            </p>

            <div className="hero-actions">
              <Link to="/entrevistas" className="primary-button">
                Buscar entrevistas
              </Link>

              <Link to="/registro" className="secondary-button">
                Crear cuenta
              </Link>
            </div>
          </div>
        </div>
      </section>

      <section className="features-section">
        <div className="section-header">
          <p className="section-tag">Funciones principales</p>
          <h2>¿Qué puedes hacer en WorkInX?</h2>
        </div>

        <div className="features-grid">
          <div className="feature-card">
            <Search size={34} />
            <h3>Buscar entrevistas</h3>
            <p>
              Filtra oportunidades por palabras clave, modalidad, ubicación y
              tipo de empleo.
            </p>
          </div>

          <div className="feature-card">
            <Building2 size={34} />
            <h3>Registrar empresas</h3>
            <p>
              Las empresas podrán crear su perfil, clasificarse y publicar
              entrevistas laborales.
            </p>
          </div>

          <div className="feature-card">
            <UserRoundCheck size={34} />
            <h3>Apoyar primer empleo</h3>
            <p>
              El sistema prioriza oportunidades para jóvenes y candidatos sin
              experiencia laboral.
            </p>
          </div>
        </div>
      </section>

      <section className="about-section" id="quienes-somos">
        <div className="about-content">
          <div>
            <p className="section-tag">Quiénes somos</p>

            <h2>Una plataforma creada para facilitar el acceso laboral</h2>

            <p>
              WorkInX es una plataforma digital orientada a mejorar la conexión
              entre empresas y personas que buscan una oportunidad laboral,
              especialmente jóvenes, recién egresados y candidatos que aún no
              cuentan con experiencia formal.
            </p>

            <p>
              El proyecto nace como una respuesta a la dificultad que muchas
              personas enfrentan al momento de acceder a entrevistas laborales,
              ya sea por falta de experiencia, poca visibilidad de vacantes o
              ausencia de herramientas que centralicen oportunidades confiables.
            </p>
          </div>

          <div className="about-card">
            <UsersRound size={42} />
            <h3>Conexión entre talento y empresas</h3>
            <p>
              WorkInX busca crear un espacio donde las oportunidades laborales
              sean más visibles, accesibles y organizadas para todos.
            </p>
          </div>
        </div>
      </section>

      <section className="mission-section" id="mision-vision">
        <div className="section-header">
          <p className="section-tag">Propósito institucional</p>
          <h2>Misión y visión</h2>
        </div>

        <div className="mission-grid">
          <div className="mission-card">
            <Target size={40} />
            <h3>Misión</h3>
            <p>
              Facilitar el acceso a entrevistas laborales mediante una plataforma
              digital que conecte a candidatos con empresas, promoviendo
              oportunidades para jóvenes, personas sin experiencia y usuarios en
              búsqueda activa de empleo.
            </p>
          </div>

          <div className="mission-card">
            <Eye size={40} />
            <h3>Visión</h3>
            <p>
              Ser una plataforma reconocida por impulsar la inclusión laboral,
              centralizar oportunidades de entrevista y fortalecer la relación
              entre empresas y nuevos talentos en el mercado laboral.
            </p>
          </div>
        </div>
      </section>
    </>
  );
}

export default Home;