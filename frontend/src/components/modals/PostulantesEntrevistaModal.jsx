/**
 * @file PostulantesEntrevistaModal.jsx
 * @description Modal para que las empresas revisen los candidatos inscritos en sus ofertas de entrevista,
 * descarguen o visualicen hojas de vida (CV) y actualicen el estado de selección.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import {
  X,
  UserRound,
  Mail,
  Phone,
  MapPin,
  IdCard,
  FileText,
  ExternalLink,
  CalendarDays,
  CheckCircle2,
  XCircle,
  Eye,
} from "lucide-react";
import {
  convertirEdad,
  convertirEstado,
  formatearFecha,
} from "../../utils/formatters";

function PostulantesEntrevistaModal({
  entrevista,
  postulantes,
  cargando,
  onClose,
  onCambiarEstado,
}) {
  if (!entrevista) {
    return null;
  }

  return (
    <div className="applicants-modal-overlay" onClick={onClose}>
      <div
        className="applicants-modal"
        onClick={(event) => event.stopPropagation()}
      >
        <button type="button" className="close-modal-button" onClick={onClose}>
          <X size={22} />
        </button>

        <div className="applicants-modal-header">
          <p className="section-tag">Postulantes</p>

          <h2>{entrevista.titulo}</h2>

          <p>
            Consulta los candidatos postulados y actualiza el estado de cada
            postulación.
          </p>
        </div>

        {cargando ? (
          <div className="empty-results">
            <h3>Cargando postulantes...</h3>
            <p>Estamos consultando la información de los candidatos.</p>
          </div>
        ) : (
          <div className="applicants-list">
            {postulantes.map((postulante) => (
              <article key={postulante.id} className="applicant-card">
                <div className="applicant-main">
                  <div className="applicant-avatar">
                    <UserRound size={28} />
                  </div>

                  <div>
                    <span className="application-status">
                      Estado: {convertirEstado(postulante.estado)}
                    </span>

                    <h3>{postulante.nombre_completo}</h3>

                    <div className="applicant-info">
                      <p>
                        <Mail size={17} />
                        {postulante.correo}
                      </p>

                      <p>
                        <Phone size={17} />
                        {postulante.telefono || "Sin teléfono"}
                      </p>

                      <p>
                        <MapPin size={17} />
                        {postulante.ciudad_residencia}
                      </p>

                      <p>
                        <IdCard size={17} />
                        {postulante.tipo_documento} - {postulante.documento}
                      </p>

                      <p>
                        <UserRound size={17} />
                        {convertirEdad(postulante.categoria_edad)}
                      </p>

                      <p>
                        <CalendarDays size={17} />
                        {formatearFecha(postulante.fecha_postulacion)}
                      </p>
                    </div>

                    {postulante.mensaje && (
                      <p className="application-message">
                        “{postulante.mensaje}”
                      </p>
                    )}
                  </div>
                </div>

                <div className="applicant-actions">
                  <span className="cv-label">
                    <FileText size={17} />
                    {postulante.cv_nombre || "CV adjunto"}
                  </span>

                  {postulante.cv_url && (
                    <a
                      href={postulante.cv_url}
                      target="_blank"
                      rel="noreferrer"
                      className="details-button"
                    >
                      <ExternalLink size={18} />
                      Ver CV
                    </a>
                  )}

                  <button
                    type="button"
                    className="details-button"
                    onClick={() => onCambiarEstado(postulante.id, "revisado")}
                  >
                    <Eye size={18} />
                    Revisado
                  </button>

                  <button
                    type="button"
                    className="edit-button"
                    onClick={() => onCambiarEstado(postulante.id, "aceptado")}
                  >
                    <CheckCircle2 size={18} />
                    Aceptar
                  </button>

                  <button
                    type="button"
                    className="delete-button"
                    onClick={() => onCambiarEstado(postulante.id, "rechazado")}
                  >
                    <XCircle size={18} />
                    Rechazar
                  </button>
                </div>
              </article>
            ))}

            {postulantes.length === 0 && (
              <div className="empty-results">
                <h3>Aún no hay postulantes.</h3>
                <p>
                  Cuando un candidato se postule a esta entrevista, aparecerá
                  aquí.
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

export default PostulantesEntrevistaModal;
