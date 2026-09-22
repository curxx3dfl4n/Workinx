import { Link } from "react-router";
import { Building2, UserRound, ArrowRight } from "lucide-react";

function Registro() {
  return (
    <section className="register-page">
      <div className="register-header">
        <p className="section-tag">Crear cuenta</p>
        <h1>Elige cómo quieres registrarte en WorkInX</h1>
        <p>
          Puedes crear una cuenta como candidato para buscar entrevistas o como
          empresa para publicar oportunidades laborales.
        </p>
      </div>

      <div className="register-options">
        <div className="register-option-card">
          <UserRound size={46} />
          <h2>Candidato</h2>
          <p>
            Crea un perfil para buscar entrevistas, postularte y encontrar
            oportunidades de primer empleo.
          </p>

          <Link to="/registro/usuario" className="option-button">
            Registrarme como candidato
            <ArrowRight size={20} />
          </Link>
        </div>

        <div className="register-option-card featured-option">
          <Building2 size={46} />
          <h2>Empresa</h2>
          <p>
            Registra tu empresa, clasifícala según su número de empleados y
            publica entrevistas laborales.
          </p>

          <Link to="/registro/empresa" className="option-button">
            Registrar empresa
            <ArrowRight size={20} />
          </Link>
        </div>
      </div>
    </section>
  );
}

export default Registro;
