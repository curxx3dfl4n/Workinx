/**
 * @file Login.jsx
 * @description Vista de formulario de Inicio de Sesión para candidatos y empresas.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { Mail, LockKeyhole, LogIn, UserPlus, Building2 } from "lucide-react";
import { authService } from "../../services/auth.service";
import { useAuth } from "../../hooks/useAuth";

/**
 * Vista funcional del formulario de Autenticación de Usuario.
 * @returns {JSX.Element} Pantalla de Login
 */
function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();

  const [mensajeError, setMensajeError] = useState("");
  const [cargando, setCargando] = useState(false);

  const manejarLogin = async (event) => {
    event.preventDefault();

    setMensajeError("");
    setCargando(true);

    const datos = new FormData(event.target);
    const correo = datos.get("correo");
    const password = datos.get("password");

    try {
      const resultado = await authService.login(correo, password);

      login(resultado.token, resultado.usuario);

      navigate(resultado.usuario.ruta_perfil || "/perfil/usuario");
    } catch (error) {
      console.error("Error al iniciar sesión:", error);
      setMensajeError(error.message || "No se pudo iniciar sesión.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <section className="login-page">
      <div className="login-container">
        <div className="login-info">
          <p className="section-tag">Acceso a WorkInX</p>

          <h1>Inicia sesión y continúa buscando oportunidades laborales.</h1>

          <p>
            Accede a tu cuenta para buscar entrevistas, revisar oportunidades,
            postularte y mantener actualizado tu perfil profesional.
          </p>

          <div className="login-benefits">
            <div>
              <UserPlus size={24} />
              <span>Perfil candidato</span>
            </div>

            <div>
              <Building2 size={24} />
              <span>Acceso empresas</span>
            </div>

            <div>
              <LogIn size={24} />
              <span>Ingreso seguro</span>
            </div>
          </div>
        </div>

        <div className="login-card">
          <h2>Iniciar sesión</h2>

          <p className="auth-subtitle">
            Ingresa tus datos para acceder a WorkInX.
          </p>

          <form onSubmit={manejarLogin} className="auth-form">
            <label>
              Correo electrónico
              <div className="input-group">
                <Mail size={20} />
                <input
                  type="email"
                  name="correo"
                  placeholder="ejemplo@correo.com"
                  required
                />
              </div>
            </label>

            <label>
              Contraseña
              <div className="input-group">
                <LockKeyhole size={20} />
                <input
                  type="password"
                  name="password"
                  placeholder="Ingresa tu contraseña"
                  required
                />
              </div>
            </label>

            {mensajeError && <p className="form-error">{mensajeError}</p>}

            <button type="submit" className="auth-button" disabled={cargando}>
              {cargando ? "Ingresando..." : "Iniciar sesión"}
            </button>
          </form>

          <p className="auth-link">
            ¿No tienes cuenta? <Link to="/registro">Regístrate aquí</Link>
          </p>
        </div>
      </div>
    </section>
  );
}

export default Login;
