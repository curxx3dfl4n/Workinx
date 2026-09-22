/**
 * @file RegistroEmpresa.jsx
 * @description Formulario multiasistente para el registro de organizaciones y empresas reclutadoras.
 * Clasifica la empresa según el rango de empleados e industria.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useState } from "react";
import { useNavigate } from "react-router";
import {
  Building2,
  Mail,
  LockKeyhole,
  Phone,
  MapPin,
  Factory,
  UsersRound,
  Landmark,
  CheckCircle2,
  ArrowLeft,
  ArrowRight,
  ShieldCheck,
} from "lucide-react";
import { authService } from "../../services/auth.service";

const rangosEmpleados = [
  {
    id: "1-10",
    texto: "1 - 10 empleados",
    clasificacion: "Microempresa",
  },
  {
    id: "11-50",
    texto: "11 - 50 empleados",
    clasificacion: "Pequeña empresa",
  },
  {
    id: "51-200",
    texto: "51 - 200 empleados",
    clasificacion: "Mediana empresa",
  },
  {
    id: "201-500",
    texto: "201 - 500 empleados",
    clasificacion: "Gran empresa",
  },
  {
    id: "500+",
    texto: "Más de 500 empleados",
    clasificacion: "Macroempresa",
  },
];

function RegistroEmpresa() {
  const navigate = useNavigate();

  const [paso, setPaso] = useState(1);
  const [mensajeError, setMensajeError] = useState("");
  const [mensajeExito, setMensajeExito] = useState("");
  const [cargando, setCargando] = useState(false);

  const [empresa, setEmpresa] = useState({
    nombre: "",
    correo: "",
    password: "",
    confirmarPassword: "",
    telefono: "",
    direccion: "",
    industria: "",
    aceptaTerminos: false,
  });

  const [rangoSeleccionado, setRangoSeleccionado] = useState(null);
  const [tipoEntidad, setTipoEntidad] = useState("");

  const manejarCambio = (event) => {
    const { name, value } = event.target;

    if (name === "telefono") {
      const soloNumeros = value.replace(/\D/g, "");

      setEmpresa({
        ...empresa,
        telefono: soloNumeros,
      });

      setMensajeError("");
      setMensajeExito("");
      return;
    }

    setEmpresa({
      ...empresa,
      [name]: value,
    });

    setMensajeError("");
    setMensajeExito("");
  };

  const manejarTerminos = (event) => {
    setEmpresa({
      ...empresa,
      aceptaTerminos: event.target.checked,
    });

    setMensajeError("");
    setMensajeExito("");
  };

  const passwordTieneMinimo = empresa.password.length >= 8;
  const passwordTieneMayuscula = /[A-ZÁÉÍÓÚÑ]/.test(empresa.password);
  const passwordTieneNumero = /[0-9]/.test(empresa.password);
  const passwordTieneEspecial = /[!@#$%^&*(),.?":{}|<>_\-+=/\\[\];'`~]/.test(
    empresa.password
  );

  const passwordValida =
    passwordTieneMinimo &&
    passwordTieneMayuscula &&
    passwordTieneNumero &&
    passwordTieneEspecial;

  const passwordsNoCoinciden =
    empresa.confirmarPassword.length > 0 &&
    empresa.password !== empresa.confirmarPassword;

  const formularioPrincipalValido =
    empresa.nombre &&
    empresa.correo &&
    empresa.password &&
    empresa.confirmarPassword &&
    empresa.telefono &&
    empresa.direccion &&
    empresa.industria &&
    empresa.aceptaTerminos &&
    passwordValida &&
    empresa.password === empresa.confirmarPassword;

  const continuarClasificacion = (event) => {
    event.preventDefault();

    setMensajeError("");
    setMensajeExito("");

    if (!passwordValida) {
      setMensajeError(
        "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial."
      );
      return;
    }

    if (empresa.password !== empresa.confirmarPassword) {
      setMensajeError("Las contraseñas no coinciden. Verifica ambos campos.");
      return;
    }

    if (!empresa.aceptaTerminos) {
      setMensajeError(
        "Debes aceptar los términos y condiciones para continuar."
      );
      return;
    }

    setPaso(2);
    window.scrollTo(0, 0);
  };

  const crearCuentaEmpresa = async () => {
    setMensajeError("");
    setMensajeExito("");

    if (!rangoSeleccionado || !tipoEntidad) {
      setMensajeError("Selecciona el rango de empleados y el tipo de entidad.");
      return;
    }

    if (!empresa.aceptaTerminos) {
      setMensajeError(
        "No puedes crear la cuenta sin aceptar los términos y condiciones."
      );
      return;
    }

    if (!passwordValida) {
      setMensajeError(
        "La contraseña debe tener mínimo 8 caracteres, una mayúscula, un número y un carácter especial."
      );
      return;
    }

    if (empresa.password !== empresa.confirmarPassword) {
      setMensajeError("Las contraseñas no coinciden. Verifica ambos campos.");
      return;
    }

    const datosEmpresa = {
      nombre: empresa.nombre,
      correo: empresa.correo,
      password: empresa.password,
      telefono: empresa.telefono,
      direccion: empresa.direccion,
      industria: empresa.industria,
      rango_empleados: rangoSeleccionado.id,
      tipoEntidad: tipoEntidad,
      aceptaTerminos: empresa.aceptaTerminos,
    };

    try {
      setCargando(true);

      await authService.registrarEmpresa(datosEmpresa);

      setMensajeExito(
        "Empresa creada correctamente. Ahora puedes iniciar sesión."
      );

      setTimeout(() => {
        navigate("/login");
      }, 1500);
    } catch (error) {
      console.error("Error registrando empresa:", error);
      setMensajeError(error.message || "No se pudo crear la empresa.");
    } finally {
      setCargando(false);
    }
  };

  const puedeCrearCuenta =
    rangoSeleccionado &&
    tipoEntidad &&
    empresa.aceptaTerminos &&
    passwordValida &&
    empresa.password === empresa.confirmarPassword;

  return (
    <section className="company-register-page">
      <div className="company-register-container">
        <div className="company-register-info">
          <p className="section-tag">Registro empresarial</p>

          <h1>Registra tu empresa en WorkInX</h1>

          <p>
            Crea una cuenta empresarial para publicar entrevistas laborales,
            conectar con candidatos y clasificar tu empresa según su tamaño y
            tipo de entidad.
          </p>

          <div className="steps-box">
            <div className={paso === 1 ? "step active-step" : "step"}>
              <span>1</span>
              <p>Datos principales</p>
            </div>

            <div className={paso === 2 ? "step active-step" : "step"}>
              <span>2</span>
              <p>Clasificación empresarial</p>
            </div>
          </div>
        </div>

        {paso === 1 && (
          <div className="company-register-card">
            <h2>Datos de la empresa</h2>

            <p className="auth-subtitle">
              Completa la información principal de la empresa.
            </p>

            <form onSubmit={continuarClasificacion} className="auth-form">
              <label>
                Nombre de la empresa
                <div className="input-group">
                  <Building2 size={20} />
                  <input
                    type="text"
                    name="nombre"
                    placeholder="Ej: WorkInX S.A.S"
                    value={empresa.nombre}
                    onChange={manejarCambio}
                    required
                  />
                </div>
              </label>

              <label>
                Correo empresarial
                <div className="input-group">
                  <Mail size={20} />
                  <input
                    type="email"
                    name="correo"
                    placeholder="empresa@correo.com"
                    value={empresa.correo}
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
                    value={empresa.password}
                    onChange={manejarCambio}
                    required
                  />
                </div>
              </label>

              <div className="password-rules">
                <p className={passwordTieneMinimo ? "rule-ok" : "rule-error"}>
                  Mínimo 8 caracteres
                </p>

                <p
                  className={
                    passwordTieneMayuscula ? "rule-ok" : "rule-error"
                  }
                >
                  Al menos una mayúscula
                </p>

                <p className={passwordTieneNumero ? "rule-ok" : "rule-error"}>
                  Al menos un número
                </p>

                <p
                  className={passwordTieneEspecial ? "rule-ok" : "rule-error"}
                >
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
                    value={empresa.confirmarPassword}
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
                Teléfono de contacto
                <div className="input-group">
                  <Phone size={20} />
                  <input
                    type="text"
                    name="telefono"
                    inputMode="numeric"
                    pattern="[0-9]*"
                    placeholder="3000000000"
                    value={empresa.telefono}
                    onChange={manejarCambio}
                    required
                  />
                </div>
              </label>

              <label>
                Dirección
                <div className="input-group">
                  <MapPin size={20} />
                  <input
                    type="text"
                    name="direccion"
                    placeholder="Dirección de la empresa"
                    value={empresa.direccion}
                    onChange={manejarCambio}
                    required
                  />
                </div>
              </label>

              <label>
                Industria o sector
                <div className="input-group">
                  <Factory size={20} />
                  <input
                    type="text"
                    name="industria"
                    placeholder="Ej: Tecnología, salud, educación"
                    value={empresa.industria}
                    onChange={manejarCambio}
                    required
                  />
                </div>
              </label>

              <label className="terms-box">
                <input
                  type="checkbox"
                  checked={empresa.aceptaTerminos}
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
                disabled={!formularioPrincipalValido || cargando}
              >
                Continuar con clasificación
                <ArrowRight size={20} />
              </button>
            </form>
          </div>
        )}

        {paso === 2 && (
          <div className="company-register-card">
            <h2>Clasificación empresarial</h2>

            <p className="auth-subtitle">
              Selecciona el número de empleados y el tipo de entidad. El sistema
              asignará automáticamente el rango de la empresa.
            </p>

            <div className="classification-block">
              <h3>
                <UsersRound size={22} />
                ¿Cuántos empleados tiene la empresa?
              </h3>

              <div className="employee-options">
                {rangosEmpleados.map((rango) => (
                  <button
                    key={rango.id}
                    type="button"
                    className={
                      rangoSeleccionado?.id === rango.id
                        ? "employee-option selected-option"
                        : "employee-option"
                    }
                    onClick={() => {
                      setRangoSeleccionado(rango);
                      setMensajeError("");
                      setMensajeExito("");
                    }}
                  >
                    <span>{rango.texto}</span>
                    <strong>{rango.clasificacion}</strong>
                  </button>
                ))}
              </div>
            </div>

            {rangoSeleccionado && (
              <div className="result-box">
                <CheckCircle2 size={24} />
                <p>
                  Según la cantidad de empleados, tu empresa será clasificada
                  como <strong>{rangoSeleccionado.clasificacion}</strong>.
                </p>
              </div>
            )}

            <div className="classification-block">
              <h3>
                <Landmark size={22} />
                ¿Qué tipo de entidad es?
              </h3>

              <div className="entity-options">
                <button
                  type="button"
                  className={
                    tipoEntidad === "Pública"
                      ? "entity-option selected-option"
                      : "entity-option"
                  }
                  onClick={() => {
                    setTipoEntidad("Pública");
                    setMensajeError("");
                    setMensajeExito("");
                  }}
                >
                  Entidad pública
                </button>

                <button
                  type="button"
                  className={
                    tipoEntidad === "Privada"
                      ? "entity-option selected-option"
                      : "entity-option"
                  }
                  onClick={() => {
                    setTipoEntidad("Privada");
                    setMensajeError("");
                    setMensajeExito("");
                  }}
                >
                  Entidad privada
                </button>
              </div>
            </div>

            {mensajeError && <p className="form-error">{mensajeError}</p>}
            {mensajeExito && <p className="form-success">{mensajeExito}</p>}

            <div className="company-actions">
              <button
                type="button"
                className="back-button"
                onClick={() => {
                  setPaso(1);
                  setMensajeError("");
                  setMensajeExito("");
                  window.scrollTo(0, 0);
                }}
              >
                <ArrowLeft size={20} />
                Volver
              </button>

              <button
                type="button"
                className="auth-button final-company-button"
                disabled={!puedeCrearCuenta || cargando}
                onClick={crearCuentaEmpresa}
              >
                {cargando ? "Creando empresa..." : "Crear cuenta de empresa"}
              </button>
            </div>
          </div>
        )}
      </div>
    </section>
  );
}

export default RegistroEmpresa;
