/**
 * @file FormularioEntrevista.jsx
 * @description Formulario asistente para la publicación y edición de ofertas de entrevista laboral por parte de las empresas.
 * Incluye selector de fechas, rango de salarios, listas de requisitos y selector geográfico MapaPicker.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router";
import { X, Send, BriefcaseBusiness, MapPin, ChevronRight, ChevronLeft, ArrowLeft } from "lucide-react";
import MapaPicker from "../../components/MapaPicker";
import DatePicker, { registerLocale } from "react-datepicker";
import { es } from "date-fns/locale/es";
import { format } from "date-fns";
import "react-datepicker/dist/react-datepicker.css";
import { entrevistasService } from "../../services/entrevistas.service";

registerLocale("es", es);

const OPCIONES_CATEGORIAS = [
  "Tecnología y Software",
  "Administración y Oficina",
  "Ventas y Marketing",
  "Finanzas y Contabilidad",
  "Recursos Humanos",
  "Logística y Transporte",
  "Atención al Cliente",
  "Salud y Medicina",
  "Educación y Docencia",
  "Diseño y Medios",
  "Ingeniería",
  "Operaciones y Mantenimiento",
  "Hostelería y Turismo",
  "Legal"
];

function FormularioEntrevista() {
  const { id } = useParams();
  const navigate = useNavigate();
  const esEdicion = Boolean(id);

  const [pasoActual, setPasoActual] = useState(1);
  const [mensajeError, setMensajeError] = useState("");
  const [cargando, setCargando] = useState(esEdicion);
  const [guardando, setGuardando] = useState(false);
  const [mostrarMapa, setMostrarMapa] = useState(false);

  const parseCategorias = (catString) => {
    if (!catString) return [];
    return catString.split(",").map(c => c.trim()).filter(Boolean);
  };

  const [formulario, setFormulario] = useState({
    titulo: "",
    categorias: [],
    descripcion: "",
    tipo: "",
    modalidad: "",
    ubicacion: "",
    lugarEntrevista: "",
    salarioAConvenir: false,
    salarioFijo: "",
    requisitos: "",
    palabrasClave: "",
    latitud: null,
    longitud: null,
  });

  const [fechaEntrevistaObj, setFechaEntrevistaObj] = useState(null);
  const [fechaLimiteObj, setFechaLimiteObj] = useState(null);

  useEffect(() => {
    if (esEdicion) {
      const cargarEntrevista = async () => {
        try {
          const resultado = await entrevistasService.obtenerEntrevistaPorId(id);
          const e = resultado.entrevista;
          setFormulario({
            titulo: e.titulo || "",
            categorias: parseCategorias(e.categoria),
            descripcion: e.descripcion || "",
            tipo: e.tipo || "",
            modalidad: e.modalidad || "",
            ubicacion: e.ubicacion || "",
            lugarEntrevista: e.lugarEntrevista || "",
            salarioAConvenir: e.salarioAConvenir || false,
            salarioFijo: (e && !e.salarioAConvenir) 
              ? (e.salarioMin || e.salarioMax || "")
              : "",
            requisitos: Array.isArray(e.requisitos) ? e.requisitos.join("\n") : e.requisitos || "",
            palabrasClave: Array.isArray(e.palabrasClave) ? e.palabrasClave.join(", ") : e.palabrasClave || "",
            latitud: e.latitud || null,
            longitud: e.longitud || null,
          });

          if (e.fechaEntrevista) {
            setFechaEntrevistaObj(new Date(`${e.fechaEntrevista}T${e.horaEntrevista || '00:00'}:00`));
          }
          if (e.fechaLimite) {
            setFechaLimiteObj(new Date(`${e.fechaLimite}T23:59:00`));
          }
        } catch (error) {
          console.error(error);
          setMensajeError("No se pudo cargar la entrevista para editar.");
        } finally {
          setCargando(false);
        }
      };
      cargarEntrevista();
    }
  }, [id, esEdicion]);

  const esRemoto = formulario.modalidad === "Remoto" || formulario.modalidad === "remoto";

  const manejarCambio = (event) => {
    const { name, value, type, checked } = event.target;

    if (type === "checkbox") {
      setFormulario({
        ...formulario,
        [name]: checked,
        salarioFijo: checked ? "" : formulario.salarioFijo,
      });
      setMensajeError("");
      return;
    }

    if (name === "salarioFijo") {
      const soloNumeros = value.replace(/\D/g, "");
      setFormulario({ ...formulario, [name]: soloNumeros });
      setMensajeError("");
      return;
    }

    if (name === "modalidad" && (value === "Remoto" || value === "remoto")) {
      setFormulario({ ...formulario, [name]: value, latitud: null, longitud: null });
      setMensajeError("");
      return;
    }

    setFormulario({ ...formulario, [name]: value });
    setMensajeError("");
  };

  const formatearMonedaVisual = (valor) => {
    if (!valor) return "";
    const num = Number(valor);
    if (isNaN(num)) return "";
    return "$ " + num.toLocaleString("es-CO");
  };

  const manejarSeleccionMapa = ({ lat, lng, direccion }) => {
    setFormulario((prev) => ({
      ...prev,
      latitud: lat,
      longitud: lng,
      lugarEntrevista: direccion || prev.lugarEntrevista,
    }));
    setMostrarMapa(false);
  };

  const validarYAvanzar = () => {
    setMensajeError("");
    if (pasoActual === 1) {
      if (!formulario.titulo.trim()) return setMensajeError("Debes ingresar un título.");
      if (formulario.categorias.length === 0) return setMensajeError("Debes seleccionar al menos una categoría.");
      if (formulario.descripcion.trim().length < 20) return setMensajeError("La descripción debe tener mínimo 20 caracteres.");
    }
    if (pasoActual === 2) {
      if (!formulario.tipo) return setMensajeError("Debes seleccionar el tipo de entrevista.");
      if (!formulario.modalidad) return setMensajeError("Debes seleccionar la modalidad.");
      if (!formulario.salarioAConvenir && !formulario.salarioFijo) return setMensajeError("Debes proponer un salario o marcarlo a convenir.");
    }
    setPasoActual((p) => p + 1);
  };

  const guardarEntrevista = async (event) => {
    event.preventDefault();

    if (!fechaEntrevistaObj) {
      return setMensajeError("Debes seleccionar la fecha y hora de la entrevista.");
    }
    if (!fechaLimiteObj) {
      return setMensajeError("Debes seleccionar la fecha límite de postulación.");
    }
    if (fechaLimiteObj > fechaEntrevistaObj) {
      return setMensajeError("La fecha límite no puede ser después de la entrevista.");
    }

    if (!esRemoto && (!formulario.latitud || !formulario.longitud)) {
      return setMensajeError("Debes seleccionar la ubicación en el mapa para entrevistas presenciales/híbridas.");
    }
    if (esRemoto && !formulario.lugarEntrevista.trim()) {
      return setMensajeError("Debes proveer un enlace de videollamada para entrevistas remotas.");
    }

    try {
      setGuardando(true);
      setMensajeError("");

      const salarioTexto = formulario.salarioAConvenir
        ? "Salario a convenir"
        : `$${Number(formulario.salarioFijo).toLocaleString("es-CO")}`;

      const fechaEntrevistaStr = format(fechaEntrevistaObj, "yyyy-MM-dd");
      const horaEntrevistaStr = format(fechaEntrevistaObj, "HH:mm");
      const fechaLimiteStr = format(fechaLimiteObj, "yyyy-MM-dd");

      const payload = {
        titulo: formulario.titulo,
        categoria: formulario.categorias.join(", "),
        descripcion: formulario.descripcion,
        tipo: formulario.tipo,
        modalidad: formulario.modalidad,
        ubicacion: formulario.ubicacion,
        lugarEntrevista: formulario.lugarEntrevista,
        salarioAConvenir: formulario.salarioAConvenir,
        salarioMin: formulario.salarioAConvenir ? null : Number(formulario.salarioFijo),
        salarioMax: formulario.salarioAConvenir ? null : Number(formulario.salarioFijo),
        fechaEntrevista: fechaEntrevistaStr,
        horaEntrevista: horaEntrevistaStr,
        fechaLimite: fechaLimiteStr,
        requisitos: formulario.requisitos.split("\n").map((i) => i.trim()).filter(Boolean),
        palabrasClave: formulario.palabrasClave.split(",").map((i) => i.trim()).filter(Boolean),
        latitud: esRemoto ? null : formulario.latitud,
        longitud: esRemoto ? null : formulario.longitud,
      };

      if (esEdicion) {
        await entrevistasService.actualizarEntrevista(id, payload);
      } else {
        await entrevistasService.crearEntrevista(payload);
      }

      navigate("/perfil/empresa");
    } catch (error) {
      console.error(error);
      setMensajeError(error.message || "No se pudo guardar la entrevista.");
    } finally {
      setGuardando(false);
    }
  };

  if (cargando) {
    return <div style={{ padding: "40px", textAlign: "center" }}>Cargando datos de la entrevista...</div>;
  }

  return (
    <div className="page-container page-wizard-container">


      <div className="wizard-card" style={{ maxWidth: "700px", margin: "0 auto", background: "#fff", padding: "32px", borderRadius: "16px", boxShadow: "0 12px 40px rgba(0,0,0,0.06)" }}>
        <div className="company-modal-header wizard-header" style={{ marginBottom: "24px" }}>
          <div className="company-modal-icon">
            <BriefcaseBusiness size={28} />
          </div>
          <div>
            <p className="section-tag" style={{ margin: "0 0 4px 0", color: "#64748b", fontWeight: "600", fontSize: "0.85rem", textTransform: "uppercase", letterSpacing: "0.5px" }}>
              {esEdicion ? "Editar entrevista" : "Nueva entrevista"}
            </p>
            <h2 style={{ margin: 0, fontSize: "1.5rem", color: "#0f172a" }}>Paso {pasoActual} de 3</h2>
          </div>
        </div>

        <div className="wizard-progress">
          <div className={`progress-bar ${pasoActual >= 1 ? "active" : ""}`} />
          <div className={`progress-bar ${pasoActual >= 2 ? "active" : ""}`} />
          <div className={`progress-bar ${pasoActual >= 3 ? "active" : ""}`} />
        </div>

        <form className="company-interview-form" onSubmit={(e) => e.preventDefault()}>
          
          {/* PASO 1 */}
          {pasoActual === 1 && (
            <div className="wizard-step">
              <label>
                Cargo o título de la entrevista
                <div className="input-group">
                  <input type="text" name="titulo" placeholder="Ej: Auxiliar administrativo" value={formulario.titulo} onChange={manejarCambio} required />
                </div>
              </label>

              <label>
                Categorías (Máximo 3)
                <div className="categorias-grid">
                  {OPCIONES_CATEGORIAS.map(cat => {
                    const checked = formulario.categorias.includes(cat);
                    return (
                      <label key={cat} className={`cat-checkbox ${checked ? 'cat-selected' : ''}`}>
                        <input 
                          type="checkbox" 
                          checked={checked}
                          onChange={(e) => {
                            const nuevas = e.target.checked 
                              ? [...formulario.categorias, cat] 
                              : formulario.categorias.filter(c => c !== cat);
                            if (nuevas.length <= 3) {
                              setFormulario({ ...formulario, categorias: nuevas });
                              setMensajeError("");
                            } else {
                              setMensajeError("Solo puedes seleccionar hasta 3 categorías.");
                            }
                          }}
                        />
                        <span>{cat}</span>
                      </label>
                    );
                  })}
                </div>
              </label>

              <label>
                Descripción del puesto
                <textarea className="company-textarea" name="descripcion" placeholder="Describe la entrevista..." value={formulario.descripcion} onChange={manejarCambio} required />
              </label>
            </div>
          )}

          {/* PASO 2 */}
          {pasoActual === 2 && (
            <div className="wizard-step">
              <label>
                Tipo de entrevista
                <div className="pills-group">
                  {["Primer empleo", "Profesional", "Emprendedor"].map(tipo => (
                    <button 
                      type="button" 
                      key={tipo} 
                      className={`pill-btn ${formulario.tipo === tipo ? 'pill-active' : ''}`} 
                      onClick={() => setFormulario({...formulario, tipo})}
                    >
                      {tipo}
                    </button>
                  ))}
                </div>
              </label>

              <label>
                Modalidad
                <div className="pills-group">
                  {["Presencial", "Remoto", "Híbrido"].map(mod => (
                    <button 
                      type="button" 
                      key={mod} 
                      className={`pill-btn ${formulario.modalidad === mod ? 'pill-active' : ''}`} 
                      onClick={() => manejarCambio({target: {name: 'modalidad', value: mod}})}
                    >
                      {mod}
                    </button>
                  ))}
                </div>
              </label>

              <label style={{ marginTop: "12px", display: "block" }}>
                Salario fijo mensual propuesto (COP)
                <div className="input-group">
                  <input type="text" name="salarioFijo" placeholder="Ej: $ 1.500.000" disabled={formulario.salarioAConvenir} value={formatearMonedaVisual(formulario.salarioFijo)} onChange={manejarCambio} required={!formulario.salarioAConvenir} />
                </div>
              </label>

              <label className="terms-box">
                <input type="checkbox" name="salarioAConvenir" checked={formulario.salarioAConvenir} onChange={manejarCambio} />
                <span>Salario a convenir</span>
              </label>
            </div>
          )}

          {/* PASO 3 */}
          {pasoActual === 3 && (
            <div className="wizard-step">
              <div className="company-form-grid">
                <label>
                  Fecha y hora de entrevista
                  <div className="datepicker-wrapper">
                    <DatePicker 
                      selected={fechaEntrevistaObj} 
                      onChange={(date) => { setFechaEntrevistaObj(date); setMensajeError(""); }} 
                      showTimeSelect 
                      timeFormat="HH:mm"
                      timeIntervals={15}
                      dateFormat="d MMMM, yyyy - h:mm aa"
                      placeholderText="Selecciona el día y la hora..."
                      minDate={new Date()}
                      className="custom-datepicker"
                      locale="es"
                    />
                  </div>
                </label>

                <label>
                  Fecha límite para postularse
                  <div className="datepicker-wrapper">
                    <DatePicker 
                      selected={fechaLimiteObj} 
                      onChange={(date) => { setFechaLimiteObj(date); setMensajeError(""); }} 
                      dateFormat="d MMMM, yyyy"
                      placeholderText="Día límite de cierre..."
                      minDate={new Date()}
                      className="custom-datepicker"
                      locale="es"
                    />
                  </div>
                </label>
              </div>

              <label>
                Ubicación general
                <div className="input-group">
                  <input type="text" name="ubicacion" placeholder="Ej: Medellín, Antioquia" value={formulario.ubicacion} onChange={manejarCambio} required />
                </div>
              </label>

              <label>
                {esRemoto ? "Enlace de videollamada o plataforma virtual" : "Lugar exacto de la entrevista (Requerido)"}
                {esRemoto ? (
                  <div className="input-group">
                    <input type="text" name="lugarEntrevista" placeholder="Ej: Enlace de Meet, Zoom o Teams" value={formulario.lugarEntrevista} onChange={manejarCambio} required />
                  </div>
                ) : (
                  <div className="mapa-selector-container">
                    <div className="input-group" onClick={() => setMostrarMapa(true)} style={{ cursor: "pointer", borderColor: formulario.latitud ? "#10b981" : "#d0d5dd", background: formulario.latitud ? "#ecfdf5" : "#ffffff" }}>
                      <input type="text" name="lugarEntrevista" placeholder="Haz clic aquí para seleccionar en el mapa" value={formulario.lugarEntrevista} readOnly required style={{ cursor: "pointer" }} />
                      <MapPin size={20} style={{ color: formulario.latitud ? "#10b981" : "#0b4db8" }} />
                    </div>
                    {!formulario.latitud ? (
                      <p style={{ color: "#dc2626", fontSize: "0.85rem", marginTop: "6px", fontWeight: "600" }}>
                        * Es obligatorio seleccionar la ubicación exacta en el mapa.
                      </p>
                    ) : (
                      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: "6px" }}>
                        <p style={{ color: "#059669", fontSize: "0.85rem", fontWeight: "600" }}>✅ Ubicación guardada</p>
                        <button type="button" className="mapa-clear-btn" onClick={() => setFormulario((p) => ({ ...p, latitud: null, longitud: null, lugarEntrevista: "" }))} style={{ padding: "4px 8px", fontSize: "0.8rem", marginTop: "0" }}>
                          <X size={12} /> Quitar
                        </button>
                      </div>
                    )}
                  </div>
                )}
              </label>

              <label>
                Requisitos (Uno por línea)
                <textarea className="company-textarea" style={{minHeight: "80px"}} name="requisitos" placeholder="Ser mayor de edad..." value={formulario.requisitos} onChange={manejarCambio} required />
              </label>

              <label>
                Palabras clave (Separadas por comas)
                <div className="input-group">
                  <input type="text" name="palabrasClave" placeholder="Ej: atención, finanzas, ventas" value={formulario.palabrasClave} onChange={manejarCambio} required />
                </div>
              </label>
            </div>
          )}

          {mensajeError && <p className="form-error">{mensajeError}</p>}

          <div className="wizard-actions" style={{ marginTop: "32px", display: "flex", justifyContent: "space-between" }}>
            {pasoActual > 1 && (
              <button type="button" className="back-button" onClick={() => setPasoActual(p => p - 1)}>
                <ChevronLeft size={18} /> Atrás
              </button>
            )}
            
            {pasoActual < 3 ? (
              <button type="button" className="auth-button" style={{ marginLeft: "auto" }} onClick={validarYAvanzar}>
                Siguiente <ChevronRight size={18} />
              </button>
            ) : (
              <button type="button" className="auth-button" style={{ marginLeft: "auto" }} onClick={guardarEntrevista} disabled={guardando}>
                <Send size={18} /> {guardando ? "Guardando..." : (esEdicion ? "Guardar cambios" : "Publicar entrevista")}
              </button>
            )}
          </div>
        </form>
      </div>

      <div style={{ maxWidth: "700px", margin: "24px auto 0", textAlign: "left", paddingLeft: "8px" }}>
        <button onClick={() => navigate("/perfil/empresa")} className="btn-back-link" style={{ color: "#64748b", background: "none", border: "none", display: "inline-flex", alignItems: "center", gap: "6px", cursor: "pointer", fontSize: "0.95rem", fontWeight: "600", transition: "0.2s ease", padding: 0 }}>
          <ArrowLeft size={16} /> Volver al panel de empresa
        </button>
      </div>

      {mostrarMapa && (
        <MapaPicker
          latitudInicial={formulario.latitud}
          longitudInicial={formulario.longitud}
          onSeleccionar={manejarSeleccionMapa}
          onCerrar={() => setMostrarMapa(false)}
        />
      )}
    </div>
  );
}

export default FormularioEntrevista;
