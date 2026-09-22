/**
 * @file PostularEntrevistaModal.jsx
 * @description Modal de diálogo para el envío de postulación y carga de archivo CV (Hoja de Vida).
 * Validaciones cliente de formato (.pdf, .doc, .docx) y tamaño máximo (5MB).
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useState } from "react";
import { X, Send, FileText, Building2, CheckCircle2 } from "lucide-react";
import { postulacionesService } from "../../services/postulaciones.service";
import { useAuth } from "../../hooks/useAuth";

/**
 * Componente modal de postulación a entrevista.
 * 
 * @param {Object} props - Propiedades del componente
 * @param {Object} props.entrevista - Objeto de entrevista objetivo
 * @param {Function} props.onClose - Callback para cerrar el modal
 * @param {Function} props.onPostulacionExitosa - Callback al completar la postulación
 * @returns {JSX.Element|null} Diálogo modal de postulación
 */
function PostularEntrevistaModal({ entrevista, onClose, onPostulacionExitosa }) {
  const { isAuthenticated } = useAuth();

  const [mensaje, setMensaje] = useState("");
  const [cv, setCv] = useState(null);
  const [confirmacion, setConfirmacion] = useState(false);
  const [mensajeError, setMensajeError] = useState("");
  const [cargando, setCargando] = useState(false);

  if (!entrevista) {
    return null;
  }

  const validarArchivo = (archivo) => {
    const extensionesPermitidas = [".pdf", ".doc", ".docx"];
    const extension = archivo.name
      .slice(archivo.name.lastIndexOf("."))
      .toLowerCase();

    const pesoMaximo = 5 * 1024 * 1024;

    if (!extensionesPermitidas.includes(extension)) {
      return "El CV debe estar en formato PDF, DOC o DOCX.";
    }

    if (archivo.size > pesoMaximo) {
      return "El archivo no debe superar los 5 MB.";
    }

    return "";
  };

  const manejarArchivo = (event) => {
    const archivo = event.target.files[0];

    if (!archivo) {
      setCv(null);
      return;
    }

    const error = validarArchivo(archivo);

    if (error) {
      setMensajeError(error);
      setCv(null);
      event.target.value = "";
      return;
    }

    setCv(archivo);
    setMensajeError("");
  };

  const enviarPostulacion = async (event) => {
    event.preventDefault();
    setMensajeError("");

    if (!cv) {
      setMensajeError("Debes adjuntar tu hoja de vida para postularte.");
      return;
    }

    if (!confirmacion) {
      setMensajeError("Debes confirmar que la información enviada es correcta.");
      return;
    }

    if (!isAuthenticated) {
      setMensajeError("Debes iniciar sesión para postularte.");
      return;
    }

    const formData = new FormData();
    formData.append("entrevistaId", entrevista.id);
    formData.append("mensaje", mensaje.trim());
    formData.append("cv", cv);

    try {
      setCargando(true);

      const resultado = await postulacionesService.crearPostulacion(formData);

      if (onPostulacionExitosa) {
        onPostulacionExitosa({
          ...resultado.postulacion,
          entrevista_titulo: entrevista.titulo,
          empresa: entrevista.empresa,
          cv_nombre: cv.name,
        });
      }

      alert("Postulación enviada correctamente.");
      onClose();
    } catch (error) {
      console.error("Error enviando postulación:", error);
      setMensajeError(error.message || "No se pudo enviar la postulación.");
    } finally {
      setCargando(false);
    }
  };

  const puedeEnviar = cv && confirmacion && !cargando;

  return (
    <div className="apply-modal-overlay" onClick={onClose}>
      <div className="apply-modal" onClick={(event) => event.stopPropagation()}>
        <button type="button" className="close-modal-button" onClick={onClose}>
          <X size={22} />
        </button>

        <div className="apply-modal-header">
          <div className="apply-icon">
            <Send size={28} />
          </div>

          <div>
            <p className="section-tag">Postulación</p>
            <h2>Postúlate a esta entrevista</h2>
          </div>
        </div>

        <div className="apply-interview">
          <Building2 size={20} />

          <div>
            <strong>{entrevista.titulo}</strong>
            <p>{entrevista.empresa}</p>
          </div>
        </div>

        <form className="apply-form" onSubmit={enviarPostulacion}>
          <label className="apply-label">
            Mensaje para la empresa
            <textarea
              className="apply-textarea"
              placeholder="Escribe una breve presentación o mensaje para la empresa..."
              value={mensaje}
              onChange={(event) => setMensaje(event.target.value)}
            />
          </label>

          <label className="cv-upload-box">
            <FileText size={28} />

            <div>
              <strong>Adjuntar hoja de vida / CV</strong>
              <p>Formatos permitidos: PDF, DOC o DOCX. Máximo 5 MB.</p>
            </div>

            <input
              type="file"
              accept=".pdf,.doc,.docx"
              onChange={manejarArchivo}
            />
          </label>

          {cv && (
            <div className="cv-selected">
              <CheckCircle2 size={22} />
              <p>
                Archivo seleccionado: <strong>{cv.name}</strong>
              </p>
            </div>
          )}

          <label className="terms-box">
            <input
              type="checkbox"
              checked={confirmacion}
              onChange={(event) => {
                setConfirmacion(event.target.checked);
                setMensajeError("");
              }}
            />

            <span>
              <CheckCircle2 size={22} />
              Confirmo que mi información y mi hoja de vida son correctas.
            </span>
          </label>

          {mensajeError && <p className="form-error">{mensajeError}</p>}

          <div className="apply-modal-actions">
            <button type="button" className="back-button" onClick={onClose}>
              Cancelar
            </button>

            <button type="submit" className="auth-button" disabled={!puedeEnviar}>
              <Send size={18} />
              {cargando ? "Enviando..." : "Enviar postulación"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default PostularEntrevistaModal;
