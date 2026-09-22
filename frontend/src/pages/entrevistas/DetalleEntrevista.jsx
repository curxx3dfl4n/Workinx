/**
 * @file DetalleEntrevista.jsx
 * @description Vista detallada de una oferta de entrevista individual.
 * Incluye la descripción, mapa geolocalizado, botón de postulación y reporte.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router";
import ReportarEntrevistaModal from "../../components/modals/ReportarEntrevistaModal";
import PostularEntrevistaModal from "../../components/modals/PostularEntrevistaModal";
import MapaVista from "../../components/MapaVista";
import { entrevistasService } from "../../services/entrevistas.service";
import { postulacionesService } from "../../services/postulaciones.service";
import { useAuth } from "../../hooks/useAuth";
import {
  ArrowLeft,
  Building2,
  MapPin,
  BriefcaseBusiness,
  Banknote,
  AlertTriangle,
  Send,
  CalendarDays,
  Clock,
  CheckCircle2,
} from "lucide-react";

/**
 * Componente funcional para la visualización detallada de la oferta de entrevista.
 * @returns {JSX.Element} Vista de Detalle de Entrevista
 */
function DetalleEntrevista() {
  const navigate = useNavigate();
  const { id } = useParams();
  const { isAuthenticated, esCandidato, esEmpresa } = useAuth();

  const [entrevista, setEntrevista] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [mensajeError, setMensajeError] = useState("");

  const [entrevistaAReportar, setEntrevistaAReportar] = useState(null);
  const [entrevistaAPostular, setEntrevistaAPostular] = useState(null);
  const [misPostulaciones, setMisPostulaciones] = useState([]);

  useEffect(() => {
    const cargarDetalle = async () => {
      try {
        setCargando(true);
        setMensajeError("");

        const resultado = await entrevistasService.obtenerEntrevistaPorId(id);
        setEntrevista(resultado.entrevista);

        // Si el usuario es candidato, consultamos sus postulaciones para saber si ya aplicó
        if (isAuthenticated && esCandidato) {
          try {
            const resPostulaciones = await postulacionesService.listarMisPostulaciones();
            setMisPostulaciones(resPostulaciones.postulaciones || []);
          } catch (postError) {
            console.warn("No se pudieron cargar postulaciones previas:", postError);
          }
        }
      } catch (error) {
        console.error("Error cargando detalle de entrevista:", error);
        setMensajeError(error.message || "No se pudo cargar la entrevista.");
      } finally {
        setCargando(false);
      }
    };

    cargarDetalle();
  }, [id, isAuthenticated, esCandidato]);

  if (cargando) {
    return (
      <section className="interview-detail-page">
        <div className="detail-container">
          <div className="detail-card">
            <h1>Cargando entrevista...</h1>
            <p>Estamos consultando la información de esta entrevista.</p>
          </div>
        </div>
      </section>
    );
  }

  if (mensajeError || !entrevista) {
    return (
      <section className="interview-detail-page">
        <div className="detail-container">
          <div className="detail-card">
            <h1>Entrevista no encontrada</h1>
            <p>
              {mensajeError ||
                "La entrevista que intentas consultar no existe o fue eliminada."}
            </p>

            <Link to="/entrevistas" className="primary-button">
              Volver a entrevistas
            </Link>
          </div>
        </div>
      </section>
    );
  }

  const yaPostulado = misPostulaciones.some(
    (postulacion) => Number(postulacion.entrevista_id) === Number(entrevista.id)
  );

  const guardarPostulacionExitosa = (nuevaPostulacion) => {
    setMisPostulaciones((prev) => [...prev, nuevaPostulacion]);
  };

  const reportarEntrevista = () => {
    if (!isAuthenticated) {
      alert("Debes iniciar sesión para reportar una entrevista.");
      navigate("/login");
      return;
    }

    setEntrevistaAReportar(entrevista);
  };

  const postularEntrevista = () => {
    if (!isAuthenticated) {
      alert("Debes iniciar sesión para postularte a una entrevista.");
      navigate("/login");
      return;
    }

    if (!esCandidato) {
      alert("Solo los candidatos pueden postularse a entrevistas.");
      return;
    }

    if (yaPostulado) {
      return;
    }

    setEntrevistaAPostular(entrevista);
  };

  return (
    <section className="interview-detail-page">
      <div className="detail-container">
        <Link to="/entrevistas" className="back-to-list">
          <ArrowLeft size={20} />
          Volver a entrevistas
        </Link>

        <div className="detail-hero-card">
          <div>
            <p className="section-tag">{entrevista.categoria}</p>

            <h1>{entrevista.titulo}</h1>

            <div className="detail-company">
              <Building2 size={22} />
              <span>{entrevista.empresa}</span>
            </div>
          </div>

          <div className="detail-actions">
            <button
              type="button"
              className={
                yaPostulado || esEmpresa
                  ? "auth-button detail-apply-button applied-button"
                  : "auth-button detail-apply-button"
              }
              onClick={postularEntrevista}
              disabled={yaPostulado || esEmpresa}
            >
              <Send size={18} />
              {yaPostulado
                ? "Ya te postulaste"
                : !isAuthenticated
                ? "Iniciar sesión"
                : esEmpresa
                ? "Solo candidatos"
                : "Postularme"}
            </button>

            <button
              type="button"
              className="report-button detail-report-button"
              onClick={reportarEntrevista}
            >
              <AlertTriangle size={18} />
              {isAuthenticated ? "Reportar entrevista" : "Ingresar para reportar"}
            </button>
          </div>
        </div>

        <div className="detail-layout">
          <div className="detail-main">
            <article className="detail-card">
              <h2>Descripción</h2>
              <p>{entrevista.descripcion}</p>
            </article>

            <article className="detail-card">
              <h2>Requisitos</h2>

              {entrevista.requisitos && entrevista.requisitos.length > 0 ? (
                <ul className="requirements-list">
                  {entrevista.requisitos.map((requisito, index) => (
                    <li key={index}>
                      <CheckCircle2 size={20} />
                      <span>{requisito}</span>
                    </li>
                  ))}
                </ul>
              ) : (
                <p>No se registraron requisitos específicos.</p>
              )}
            </article>

            <article className="detail-card">
              <h2>Palabras clave</h2>

              {entrevista.palabrasClave &&
              entrevista.palabrasClave.length > 0 ? (
                <div className="skills-list">
                  {entrevista.palabrasClave.map((palabra) => (
                    <span key={palabra}>{palabra}</span>
                  ))}
                </div>
              ) : (
                <p>No se registraron palabras clave.</p>
              )}
            </article>
          </div>

          <aside className="detail-sidebar">
            <div className="detail-info-card">
              <h2>Información general</h2>

              <div className="detail-info-item">
                <MapPin size={20} />
                <div>
                  <strong>Ubicación</strong>
                  <p>{entrevista.ubicacion}</p>
                </div>
              </div>

              <div className="detail-info-item">
                <MapPin size={20} />
                <div>
                  <strong>Lugar de entrevista</strong>
                  <p>{entrevista.lugarEntrevista}</p>
                </div>
              </div>

              {/* Mapa — solo si tiene coordenadas y NO es remoto */}
              {entrevista.latitud &&
                entrevista.longitud &&
                entrevista.modalidad !== "Remoto" &&
                entrevista.modalidad !== "remoto" && (
                  <MapaVista
                    latitud={entrevista.latitud}
                    longitud={entrevista.longitud}
                    titulo={entrevista.lugarEntrevista}
                  />
                )}

              <div className="detail-info-item">
                <BriefcaseBusiness size={20} />
                <div>
                  <strong>Tipo</strong>
                  <p>{entrevista.tipo}</p>
                </div>
              </div>

              <div className="detail-info-item">
                <BriefcaseBusiness size={20} />
                <div>
                  <strong>Modalidad</strong>
                  <p>{entrevista.modalidad}</p>
                </div>
              </div>

              <div className="detail-info-item">
                <Banknote size={20} />
                <div>
                  <strong>Salario</strong>
                  <p>{entrevista.salarioTexto}</p>
                </div>
              </div>

              <div className="detail-info-item">
                <CalendarDays size={20} />
                <div>
                  <strong>Fecha de entrevista</strong>
                  <p>{entrevista.fechaEntrevista}</p>
                </div>
              </div>

              <div className="detail-info-item">
                <Clock size={20} />
                <div>
                  <strong>Hora de entrevista</strong>
                  <p>{entrevista.horaEntrevista}</p>
                </div>
              </div>

              <div className="detail-info-item">
                <CalendarDays size={20} />
                <div>
                  <strong>Fecha límite</strong>
                  <p>{entrevista.fechaLimite}</p>
                </div>
              </div>
            </div>
          </aside>
        </div>
      </div>

      <ReportarEntrevistaModal
        entrevista={entrevistaAReportar}
        onClose={() => setEntrevistaAReportar(null)}
      />

      <PostularEntrevistaModal
        entrevista={entrevistaAPostular}
        onClose={() => setEntrevistaAPostular(null)}
        onPostulacionExitosa={guardarPostulacionExitosa}
      />
    </section>
  );
}

export default DetalleEntrevista;
