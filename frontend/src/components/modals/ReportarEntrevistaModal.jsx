/**
 * @file ReportarEntrevistaModal.jsx
 * @description Modal de diálogo para enviar un reporte de moderación comunitaria sobre una entrevista laboral.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useState } from "react";
import { AlertTriangle, X, Send, Building2 } from "lucide-react";
import { reportesService } from "../../services/reportes.service";
import { useAuth } from "../../hooks/useAuth";

const tiposReporte = [
  { id: "fraude", texto: "Fraude" },
  { id: "informacion_falsa", texto: "Información falsa" },
  { id: "contenido_inapropiado", texto: "Contenido inapropiado" },
  { id: "spam", texto: "Spam" },
  { id: "otro", texto: "Otro" },
];

/**
 * Componente modal para reportar publicaciones inapropiadas o estafas.
 *
 * @param {Object} props - Propiedades del componente
 * @param {Object} props.entrevista - Objeto de la entrevista reportada
 * @param {Function} props.onClose - Callback para cerrar el modal
 * @returns {JSX.Element|null} Modal de reporte
 */
function ReportarEntrevistaModal({ entrevista, onClose }) {
  const { isAuthenticated } = useAuth();

  const [tipoReporte, setTipoReporte] = useState("");
  const [descripcion, setDescripcion] = useState("");
  const [evidencia, setEvidencia] = useState("");
  const [mensajeError, setMensajeError] = useState("");
  const [cargando, setCargando] = useState(false);

  if (!entrevista) {
    return null;
  }

  const limpiarFormulario = () => {
    setTipoReporte("");
    setDescripcion("");
    setEvidencia("");
    setMensajeError("");
    setCargando(false);
  };

  const cerrarModal = () => {
    limpiarFormulario();
    onClose();
  };

  const enviarReporte = async (event) => {
    event.preventDefault();
    setMensajeError("");

    if (!tipoReporte) {
      setMensajeError("Debes seleccionar un motivo para reportar la entrevista.");
      return;
    }

    if (descripcion.trim().length < 10) {
      setMensajeError("La descripción debe tener mínimo 10 caracteres.");
      return;
    }

    if (!isAuthenticated) {
      setMensajeError("Debes iniciar sesión para reportar una entrevista.");
      return;
    }

    try {
      setCargando(true);

      await reportesService.crearReporte({
        entrevistaId: entrevista.id,
        tipoReporte,
        descripcion: descripcion.trim(),
        evidencia: evidencia.trim(),
      });

      alert("Reporte enviado correctamente.");
      cerrarModal();
    } catch (error) {
      console.error("Error enviando reporte:", error);
      setMensajeError(error.message || "No se pudo enviar el reporte.");
    } finally {
      setCargando(false);
    }
  };

  return (
    <div className="report-modal-overlay" onClick={cerrarModal}>
      <div className="report-modal" onClick={(event) => event.stopPropagation()}>
        <button type="button" className="close-modal-button" onClick={cerrarModal}>
          <X size={22} />
        </button>

        <div className="report-modal-header">
          <div className="report-icon">
            <AlertTriangle size={28} />
          </div>

          <div>
            <p className="section-tag">Reportar entrevista</p>
            <h2>Cuéntanos qué sucede con esta entrevista</h2>
          </div>
        </div>

        <div className="reported-interview">
          <Building2 size={20} />

          <div>
            <strong>{entrevista.titulo}</strong>
            <p>{entrevista.empresa}</p>
          </div>
        </div>

        <form className="report-form" onSubmit={enviarReporte}>
          <div className="report-block">
            <h3>Motivo del reporte</h3>

            <div className="report-options">
              {tiposReporte.map((tipo) => (
                <button
                  key={tipo.id}
                  type="button"
                  className={
                    tipoReporte === tipo.id
                      ? "report-option selected-report-option"
                      : "report-option"
                  }
                  onClick={() => {
                    setTipoReporte(tipo.id);
                    setMensajeError("");
                  }}
                >
                  {tipo.texto}
                </button>
              ))}
            </div>
          </div>

          <label className="report-label">
            Descripción del problema
            <textarea
              className="report-textarea"
              placeholder="Explica por qué deseas reportar esta entrevista..."
              value={descripcion}
              onChange={(event) => {
                setDescripcion(event.target.value);
                setMensajeError("");
              }}
              required
            />
          </label>

          <label className="report-label">
            Evidencia o información adicional
            <input
              className="report-input"
              type="text"
              placeholder="Opcional: enlace, comentario o dato adicional"
              value={evidencia}
              onChange={(event) => setEvidencia(event.target.value)}
            />
          </label>

          {mensajeError && <p className="form-error">{mensajeError}</p>}

          <div className="report-modal-actions">
            <button type="button" className="back-button" onClick={cerrarModal}>
              Cancelar
            </button>

            <button type="submit" className="auth-button" disabled={cargando}>
              <Send size={18} />
              {cargando ? "Enviando..." : "Enviar reporte"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default ReportarEntrevistaModal;
