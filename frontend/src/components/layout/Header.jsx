/**
 * @file Header.jsx
 * @description Componente de encabezado de navegación superior con comportamiento adaptativo de scroll
 * y enlaces dinámicos según el estado de autenticación del usuario.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router";
import { BriefcaseBusiness, UserRound, LogOut } from "lucide-react";
import { useAuth } from "../../hooks/useAuth";

/**
 * Componente funcional del encabezado principal de navegación.
 * @returns {JSX.Element} Barra de navegación superior
 */
function Header() {
  const [scrolled, setScrolled] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { isAuthenticated, rutaPerfil, logout } = useAuth();

  const esPaginaEntrevistas = location.pathname.startsWith("/entrevistas");

  useEffect(() => {
    const cambiarHeader = () => {
      setScrolled(window.scrollY > 60);
    };

    cambiarHeader();
    window.addEventListener("scroll", cambiarHeader);

    return () => {
      window.removeEventListener("scroll", cambiarHeader);
    };
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const claseHeader = `
    header
    ${scrolled ? "header-scrolled" : ""}
    ${!scrolled && esPaginaEntrevistas ? "header-light" : ""}
    ${!scrolled && !esPaginaEntrevistas ? "header-home" : ""}
  `;

  return (
    <header className={claseHeader}>
      <Link to="/" className="logo">
        <BriefcaseBusiness size={30} />
        <span>WorkInX</span>
      </Link>

      <nav className="nav">
        <Link to="/">Inicio</Link>
        <a href="/#quienes-somos">Quiénes somos</a>
        <a href="/#mision-vision">Misión y visión</a>
        <Link to="/entrevistas">Entrevistas</Link>
        <Link to="/reto-jpa" style={{ color: '#f59e0b', fontWeight: 'bold' }}>Reto JPA</Link>

        {!isAuthenticated ? (
          <>
            <Link to="/login">Iniciar sesión</Link>
            <Link to="/registro" className="nav-button">
              Registrarse
            </Link>
          </>
        ) : (
          <>
            <Link to={rutaPerfil} className="profile-button">
              <UserRound size={18} />
              Mi perfil
            </Link>

            <button
              type="button"
              className="logout-button"
              onClick={handleLogout}
            >
              <LogOut size={18} />
              Cerrar sesión
            </button>
          </>
        )}
      </nav>
    </header>
  );
}

export default Header;
