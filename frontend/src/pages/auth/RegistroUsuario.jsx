/**
 * @file RegistroUsuario.jsx
 * @description Formulario estructurado para el registro de nuevos postulantes candidatos.
 * Incluye validaciones en tiempo real para contraseñas, documentos y aceptación de términos.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useState } from "react";
import { useNavigate } from "react-router";
import {
  UserRound,
  IdCard,
  Mail,
  LockKeyhole,
  Phone,
  MapPin,
  ShieldCheck,
  ArrowRight,
} from "lucide-react";
import { authService } from "../../services/auth.service";

/**
 * Componente funcional de Registro de Candidato.
 * @returns {JSX.Element} Pantalla de Registro de Candidato
 */
function RegistroUsuario() {
  const navigate = useNavigate();

  const [mensajeError, setMensajeError] = useState("");
  const [mensajeExito, setMensajeExito] = useState("");
  const [cargando, setCargando] = useState(false);

  const [usuario, setUsuario] = useState({
    nombreCompleto: "",
    tipoDocumento: "",
    documento: "",
    correo: "",
    password: "",
    confirmarPassword: "",
    telefono: "",
    ciudad: "",
    edadRango: "",
    aceptaTerminos: false,
  });

  const nombresDocumento = {
    CC: "Cédula de Ciudadanía",
    TI: "Tarjeta de Identidad",
    CE: "Cédula de Extranjería",
    PPT: "Permiso por Protección Temporal",
  };

  const nombreDocumentoSeleccionado = nombresDocumento[usuario.tipoDocumento];

  const reglasDocumento = {
    TI: {
      minimo: 10,
      maximo: 10,
      mensaje: "La Tarjeta de Identidad debe tener exactamente 10 dígitos.",
    },
    CC: {
      minimo: 6,
      maximo: 10,
      mensaje: "La Cédula de Ciudadanía debe tener entre 6 y 10 dígitos.",
    },
    CE: {
      minimo: 6,
      maximo: 7,
      mensaje: "La Cédula de Extranjería debe tener entre 6 y 7 dígitos.",
    },
    PPT: {
      minimo: 7,
      maximo: 7,
      mensaje: "El PPT debe tener exactamente 7 dígitos.",
    },
  };

  const reglaDocumento = reglasDocumento[usuario.tipoDocumento];

  const documentoValido =
    reglaDocumento &&
    usuario.documento.length >= reglaDocumento.minimo &&
    usuario.documento.length <= reglaDocumento.maximo;

  const mostrarErrorDocumento =
    usuario.documento.length > 0 && usuario.tipoDocumento && !documentoValido;

  const manejarCambio = (event) => {
    const { name, value } = event.target;

    if (name === "tipoDocumento") {
      setUsuario({
        ...usuario,
        tipoDocumento: value,
        documento: "",
      });

      setMensajeError("");
      setMensajeExito("");
      return;
    }

    if (name === "telefono") {
      const soloNumeros = value.replace(/\D/g, "");

      setUsuario({
        ...usuario,
        telefono: soloNumeros,
      });

      setMensajeError("");
      setMensajeExito("");
      return;
    }

    if (name === "documento") {
      const soloNumeros = value.replace(/\D/g, "");
      const reglaActual = reglasDocumento[usuario.tipoDocumento];
      const maximo = reglaActual ? reglaActual.maximo : 10;

      setUsuario({
        ...usuario,
        documento: soloNumeros.slice(0, maximo),
      });

      setMensajeError("");
      setMensajeExito("");
      return;
    }

    setUsuario({
      ...usuario,
      [name]: value,
    });

    setMensajeError("");
    setMensajeExito("");
  };

  const manejarTerminos = (event) => {
    setUsuario({
      ...usuario,
      aceptaTerminos: event.target.checked,
    });

    setMensajeError("");
    setMensajeExito("");
  };

  const passwordTieneMinimo = usuario.password.length >= 8;
  const passwordTieneMayuscula = /[A-ZÁÉÍÓÚÑ]/.test(usuario.password);
  const passwordTieneNumero = /[0-9]/.test(usuario.password);
  const passwordTieneEspecial = /[!@#$%^&*(),.?":{}|<>_\-+=/\\[\];'`~]/.test(
    usuario.password
  );

  const passwordValida =
    passwordTieneMinimo &&
    passwordTieneMayuscula &&
    passwordTieneNumero &&
    passwordTieneEspecial;

  const passwordsNoCoinciden =
    usuario.confirmarPassword.length > 0 &&
    usuario.password !== usuario.confirmarPassword;

  const categoriaEdad =
    usuario.edadRango === "-17"
      ? "Menor de edad"
      : usuario.edadRango === "+18"
      ? "Mayor de edad"
      : "";

  const formularioValido =
    usuario.nombreCompleto &&
    usuario.tipoDocumento &&
    usuario.documento &&
    documentoValido &&
    usuario.correo &&
    usuario.password &&
    usuario.confirmarPassword &&
    usuario.telefono &&
    usuario.ciudad &&
    usuario.edadRango &&
    usuario.aceptaTerminos &&
    passwordValida &&
    usuario.password === usuario.confirmarPassword;

  const crearCuentaUsuario = async (event) => {
    event.preventDefault();

    setMensajeError("");
    setMensajeExito("");

    if (!documentoValido) {
      setMensajeError(
        reglaDocumento?.mensaje || "Selecciona un tipo de documento válido."
      );
      return;
    }

    if (!passwordValida) {
      setMensajeError(
        "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial."
      );
      return;
    }

    if (usuario.password !== usuario.confirmarPassword) {
      setMensajeError("Las contraseñas no coinciden. Verifica ambos campos.");
      return;
    }

    if (!usuario.edadRango) {
      setMensajeError("Debes seleccionar si eres menor o mayor de edad.");
      return;
    }

    if (!usuario.aceptaTerminos) {
      setMensajeError(
        "Debes aceptar los términos y condiciones para crear tu cuenta."
      );
      return;
    }

    const datosUsuario = {
      nombreCompleto: usuario.nombreCompleto,
      tipoDocumento: usuario.tipoDocumento,
      documento: usuario.documento,
      correo: usuario.correo,
      password: usuario.password,
      telefono: usuario.telefono,
      ciudad: usuario.ciudad,
      edadRango: usuario.edadRango,
      aceptaTerminos: usuario.aceptaTerminos,
    };

    try {
      setCargando(true);

      await authService.registrarCandidato(datosUsuario);

      setMensajeExito(
        "Cuenta creada correctamente. Ahora puedes iniciar sesión."
      );

      setTimeout(() => {
        navigate("/login");
      }, 1500);
    } catch (error) {
      console.error("Error registrando candidato:", error);
      setMensajeError(error.message || "No se pudo crear la cuenta.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <section className="user-register-page">
      <div className="user-register-container">
        <div className="user-register-info">
          <p className="section-tag">Registro de candidato</p>

          <h1>Crea tu cuenta y empieza a buscar entrevistas laborales.</h1>

          <p>
            Registra tu perfil como candidato para acceder a oportunidades,
            entrevistas laborales y vacantes relacionadas con tu búsqueda.
          </p>

          <div className="user-age-info">
            <h3>Clasificación por edad</h3>
            <p>
              WorkInX marcará tu cuenta como menor o mayor de edad según la
              opción que selecciones durante el registro.
            </p>
          </div>
        </div>

        <div className="user-register-card">
          <h2>Datos del candidato</h2>

          <p className="auth-subtitle">
            Completa tu información personal para crear tu cuenta en WorkInX.
          </p>

          <form onSubmit={crearCuentaUsuario} className="auth-form">
            <label>
              Nombre completo
              <div className="input-group">
                <UserRound size={20} />
                <input
                  type="text"
                  name="nombreCompleto"
                  placeholder="Ej: Samuel Duque"
                  value={usuario.nombreCompleto}
                  onChange={manejarCambio}
                  required
                />
              </div>
            </label>

            <label>
              Documento de identidad
              <div className="document-row">
                <div className="document-type">
                  <IdCard size={20} />
                  <select
                    name="tipoDocumento"
                    value={usuario.tipoDocumento}
                    onChange={manejarCambio}
                    required
                  >
                    <option value="" disabled>
                      Tipo
                    </option>
                    <option value="CC">CC</option>
                    <option value="TI">TI</option>
                    <option value="CE">CE</option>
                    <option value="PPT">PPT</option>
                  </select>
                </div>

                <div
                  className={
                    mostrarErrorDocumento
                      ? "input-group document-number input-error"
                      : "input-group document-number"
                  }
                >
                  <input
                    type="text"
                    name="documento"
                    inputMode="numeric"
                    pattern="[0-9]*"
                    maxLength={reglaDocumento ? reglaDocumento.maximo : 10}
                    placeholder="Número de documento"
                    value={usuario.documento}
                    onChange={manejarCambio}
                    required
                    disabled={!usuario.tipoDocumento}
                  />
                </div>
              </div>

              {nombreDocumentoSeleccionado && (
                <p className="document-help">
                  {nombreDocumentoSeleccionado} seleccionado.
                </p>
              )}

              {reglaDocumento && (
                <p
                  className={
                    documentoValido && usuario.documento.length > 0
                      ? "document-rule rule-ok"
                      : "document-rule rule-error"
                  }
                >
                  {reglaDocumento.mensaje}
                </p>
              )}
            </label>

            <label>
              Correo electrónico
              <div className="input-group">
                <Mail size={20} />
                <input
                  type="email"
                  name="correo"
                  placeholder="ejemplo@correo.com"
                  value={usuario.correo}
                  onChange={manejarCambio}
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
                  placeholder="Crea una contraseña segura"
                  value={usuario.password}
                  onChange={manejarCambio}
                  required
                />
              </div>
            </label>

            <div className="password-rules">
              <p className={passwordTieneMinimo ? "rule-ok" : "rule-error"}>
                Mínimo 8 caracteres
              </p>

              <p className={passwordTieneMayuscula ? "rule-ok" : "rule-error"}>
                Al menos una mayúscula
              </p>

              <p className={passwordTieneNumero ? "rule-ok" : "rule-error"}>
                Al menos un número
              </p>

              <p className={passwordTieneEspecial ? "rule-ok" : "rule-error"}>
                Al menos un carácter especial
              </p>
            </div>

            <label>
              Confirmar contraseña
              <div
                className={
                  passwordsNoCoinciden
                    ? "input-group input-error"
                    : "input-group"
                }
              >
                <LockKeyhole size={20} />
                <input
                  type="password"
                  name="confirmarPassword"
                  placeholder="Repite la contraseña"
                  value={usuario.confirmarPassword}
                  onChange={manejarCambio}
                  required
                />
              </div>
            </label>

            {passwordsNoCoinciden && (
              <p className="form-error">
                Las contraseñas no coinciden. Intenta nuevamente.
              </p>
            )}

            <label>
              Teléfono
              <div className="input-group">
                <Phone size={20} />
                <input
                  type="text"
                  name="telefono"
                  inputMode="numeric"
                  pattern="[0-9]*"
                  placeholder="3000000000"
                  value={usuario.telefono}
                  onChange={manejarCambio}
                  required
                />
              </div>
            </label>

            <label>
              Ciudad de residencia
              <div className="input-group">
                <MapPin size={20} />
                <input
                  type="text"
                  name="ciudad"
                  placeholder="Ej: Medellín"
                  value={usuario.ciudad}
                  onChange={manejarCambio}
                  required
                />
              </div>
            </label>

            <div className="age-block">
              <h3>Selecciona tu rango de edad</h3>

              <div className="age-options">
                <button
                  type="button"
                  className={
                    usuario.edadRango === "-17"
                      ? "age-option selected-option"
                      : "age-option"
                  }
                  onClick={() => {
                    setUsuario({
                      ...usuario,
                      edadRango: "-17",
                    });
                    setMensajeError("");
                    setMensajeExito("");
                  }}
                >
                  <span>-17</span>
                  <strong>Menor de edad</strong>
                </button>

                <button
                  type="button"
                  className={
                    usuario.edadRango === "+18"
                      ? "age-option selected-option"
                      : "age-option"
                  }
                  onClick={() => {
                    setUsuario({
                      ...usuario,
                      edadRango: "+18",
                    });
                    setMensajeError("");
                    setMensajeExito("");
                  }}
                >
                  <span>+18</span>
                  <strong>Mayor de edad</strong>
                </button>
              </div>
            </div>

            {categoriaEdad && (
              <div className="result-box">
                <ShieldCheck size={24} />
                <p>
                  Tu cuenta quedará marcada como{" "}
                  <strong>{categoriaEdad}</strong>.
                </p>
              </div>
            )}

            <label className="terms-box">
              <input
                type="checkbox"
                checked={usuario.aceptaTerminos}
                onChange={manejarTerminos}
              />

              <span>
                <ShieldCheck size={22} />
                Acepto los términos y condiciones de WorkInX.
              </span>
            </label>

            {mensajeError && <p className="form-error">{mensajeError}</p>}

            {mensajeExito && <p className="form-success">{mensajeExito}</p>}

            <button
              type="submit"
              className="auth-button"
              disabled={!formularioValido || cargando}
            >
              {cargando ? "Creando cuenta..." : "Crear cuenta de candidato"}
              <ArrowRight size={20} />
            </button>
          </form>
        </div>
      </div>
    </section>
  );
}

export default RegistroUsuario;
