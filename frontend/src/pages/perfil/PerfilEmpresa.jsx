/**
 * @file PerfilEmpresa.jsx
 * @description Vista del panel de administración para empresas reclutadoras.
 * Permite publicar nuevas ofertas, editar o eliminar publicaciones existentes y revisar postulantes inscritos.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useEffect, useState, useCallback } from "react";
import { useNavigate } from "react-router";
import {
  Building2,
  Mail,
  Phone,
  MapPin,
  Plus,
  Eye,
  Pencil,
  Trash2,
  CalendarDays,
  UsersRound,
} from "lucide-react";
import PostulantesEntrevistaModal from "../../components/modals/PostulantesEntrevistaModal";
import { authService } from "../../services/auth.service";
import { entrevistasService } from "../../services/entrevistas.service";
import { postulacionesService } from "../../services/postulaciones.service";
import { useAuth } from "../../hooks/useAuth";
import {
  convertirClasificacion,
  convertirTipoEntidad,
} from "../../utils/formatters";

/**
 * Componente funcional de Panel de Administración de Empresa.
 * @returns {JSX.Element} Panel de Empresa
 */
function PerfilEmpresa() {
  const navigate = useNavigate();
  const { isAuthenticated, logout } = useAuth();

  const [perfil, setPerfil] = useState(null);
  const [cargandoPerfil, setCargandoPerfil] = useState(true);
  const [mensajeError, setMensajeError] = useState("");

  const [entrevistasEmpresa, setEntrevistasEmpresa] = useState([]);
  const [cargandoEntrevistas, setCargandoEntrevistas] = useState(false);

  const [entrevistaPostulantes, setEntrevistaPostulantes] = useState(null);
  const [postulantes, setPostulantes] = useState([]);
  const [cargandoPostulantes, setCargandoPostulantes] = useState(false);

  const cargarEntrevistasEmpresa = useCallback(async () => {
    try {
      setCargandoEntrevistas(true);
      const resultado = await entrevistasService.listarMisEntrevistas();
      setEntrevistasEmpresa(resultado.entrevistas || []);
    } catch (error) {
      console.error("Error cargando entrevistas de empresa:", error);
      setMensajeError(error.message || "No se pudieron cargar las entrevistas.");
    } finally {
      setCargandoEntrevistas(false);
    }
  }, []);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }

    const obtenerPerfilEmpresa = async () => {
      try {
        setCargandoPerfil(true);
        setMensajeError("");

        const resultado = await authService.obtenerPerfil();

        if (resultado.tipo_perfil !== "empresa") {
          navigate("/perfil/usuario");
          return;
        }

        setPerfil(resultado.perfil);
        await cargarEntrevistasEmpresa();
      } catch (error) {
        console.error("Error cargando perfil de empresa:", error);
        if (error.status === 401) {
          logout();
          navigate("/login");
          return;
        }
        setMensajeError(error.message || "No se pudo cargar el perfil de empresa.");
      } finally {
        setCargandoPerfil(false);
      }
    };

    obtenerPerfilEmpresa();
  }, [navigate, isAuthenticated, logout, cargarEntrevistasEmpresa]);

  const abrirCrearEntrevista = () => {
    navigate("/perfil/empresa/entrevistas/nueva");
  };

  const abrirEditarEntrevista = (entrevista) => {
    navigate(`/perfil/empresa/entrevistas/editar/${entrevista.id}`);
  };

  const verEntrevista = (entrevista) => {
    navigate(`/entrevistas/${entrevista.id}`);
  };

  const eliminarEntrevista = async (entrevista) => {
    const confirmar = confirm(
      `¿Seguro que deseas eliminar la entrevista "${entrevista.titulo}"?`
    );

    if (!confirmar) return;

    try {
      setMensajeError("");
      await entrevistasService.eliminarEntrevista(entrevista.id);
      await cargarEntrevistasEmpresa();
    } catch (error) {
      console.error("Error eliminando entrevista:", error);
      setMensajeError(error.message || "No se pudo eliminar la entrevista.");
    }
  };

  const abrirPostulantes = async (entrevista) => {
    try {
      setMensajeError("");
      setCargandoPostulantes(true);
      setEntrevistaPostulantes(entrevista);
      setPostulantes([]);

      const resultado =
        await postulacionesService.listarPostulacionesPorEntrevista(entrevista.id);

      setPostulantes(resultado.postulantes || []);
    } catch (error) {
      console.error("Error cargando postulantes:", error);
      setMensajeError(error.message || "No se pudieron cargar los postulantes.");
    } finally {
      setCargandoPostulantes(false);
    }
  };

  const cambiarEstadoPostulacion = async (postulacionId, nuevoEstado) => {
    try {
      setMensajeError("");

      await postulacionesService.actualizarEstadoPostulacion(
        postulacionId,
        nuevoEstado
      );

      setPostulantes((postulantesActuales) =>
        postulantesActuales.map((postulante) =>
          postulante.id === postulacionId
            ? { ...postulante, estado: nuevoEstado }
            : postulante
        )
      );
    } catch (error) {
      console.error("Error cambiando estado:", error);
      setMensajeError(error.message || "No se pudo actualizar el estado.");
    }
  };

  if (cargandoPerfil) {
    return (
      <section className="company-profile-page">
        <div className="empty-results">
          <h3>Cargando perfil empresarial...</h3>
          <p>Estamos consultando la información de tu empresa.</p>
        </div>
      </section>
    );
  }

  if (mensajeError && !perfil) {
    return (
      <section className="company-profile-page">
        <div className="empty-results">
          <h3>No se pudo cargar el perfil.</h3>
          <p>{mensajeError}</p>
        </div>
      </section>
    );
  }

  if (!perfil) {
    return (
      <section className="company-profile-page">
        <div className="empty-results">
          <h3>Perfil no encontrado.</h3>
          <p>Inicia sesión nuevamente para consultar tu información.</p>
        </div>
      </section>
    );
  }

  return (
    <section className="company-profile-page">
      <div className="company-profile-header">
        <div>
          <p className="section-tag">Panel de empresa</p>
          <h1>Perfil empresarial</h1>
          <p>
            Gestiona la información de tu empresa y administra las entrevistas
            laborales publicadas.
          </p>
        </div>

        <button
          type="button"
          className="auth-button company-create-button"
          onClick={abrirCrearEntrevista}
        >
          <Plus size={20} />
          Publicar nueva entrevista
        </button>
      </div>

      {mensajeError && <p className="form-error">{mensajeError}</p>}

      <div className="company-profile-layout">
        <aside className="company-profile-card">
          <div className="company-avatar">
            <Building2 size={42} />
          </div>

          <h2>{perfil.nombre_empresa}</h2>

          <p className="company-profile-subtitle">
            {convertirClasificacion(perfil.clasificacion_empresa)} · Entidad{" "}
            {convertirTipoEntidad(perfil.tipo_entidad)}
          </p>

          <div className="company-profile-info">
            <p>
              <Mail size={18} />
              {perfil.correo}
            </p>

            <p>
              <Phone size={18} />
              {perfil.telefono_contacto || perfil.telefono}
            </p>

            <p>
              <MapPin size={18} />
              {perfil.direccion}
            </p>

            <p>
              <Building2 size={18} />
              {perfil.industria}
            </p>
          </div>
        </aside>

        <div className="company-posts-panel">
          <div className="company-posts-header">
            <div>
              <h2>Mis entrevistas publicadas</h2>
              <p>{entrevistasEmpresa.length} entrevistas registradas</p>
            </div>
          </div>

          {cargandoEntrevistas ? (
            <div className="empty-results">
              <h3>Cargando entrevistas...</h3>
              <p>Estamos consultando las entrevistas publicadas por tu empresa.</p>
            </div>
          ) : (
            <div className="company-posts-list">
              {entrevistasEmpresa.map((entrevista) => (
                <article key={entrevista.id} className="company-post-card">
                  <div>
                    <span className="company-post-category">
                      {entrevista.categoria}
                    </span>

                    <h3>{entrevista.titulo}</h3>

                    <p>{entrevista.descripcion}</p>

                    <div className="company-post-dates">
                      <span>
                        <CalendarDays size={16} />
                        Publicada: {entrevista.fechaPublicacion}
                      </span>

                      <span>
                        <CalendarDays size={16} />
                        Editada:{" "}
                        {entrevista.fechaEdicion
                          ? entrevista.fechaEdicion
                          : "Sin editar"}
                      </span>
                    </div>

                    <div className="company-post-tags">
                      <span>{entrevista.tipo}</span>
                      <span>{entrevista.modalidad}</span>
                      <span>{entrevista.salarioTexto}</span>
                    </div>
                  </div>

                  <div className="company-panel-actions">
                    <button
                      type="button"
                      className="details-button"
                      onClick={() => verEntrevista(entrevista)}
                    >
                      <Eye size={18} />
                      Ver
                    </button>

                    <button
                      type="button"
                      className="details-button"
                      onClick={() => abrirPostulantes(entrevista)}
                    >
                      <UsersRound size={18} />
                      Postulantes
                    </button>

                    <button
                      type="button"
                      className="edit-button"
                      onClick={() => abrirEditarEntrevista(entrevista)}
                    >
                      <Pencil size={18} />
                      Editar
                    </button>

                    <button
                      type="button"
                      className="delete-button"
                      onClick={() => eliminarEntrevista(entrevista)}
                    >
                      <Trash2 size={18} />
                      Eliminar
                    </button>
                  </div>
                </article>
              ))}

              {entrevistasEmpresa.length === 0 && (
                <div className="empty-results">
                  <h3>No tienes entrevistas publicadas.</h3>
                  <p>
                    Usa el botón “Publicar nueva entrevista” para registrar tu
                    primera entrevista laboral.
                  </p>
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      <PostulantesEntrevistaModal
        entrevista={entrevistaPostulantes}
        postulantes={postulantes}
        cargando={cargandoPostulantes}
        onCambiarEstado={cambiarEstadoPostulacion}
        onClose={() => {
          setEntrevistaPostulantes(null);
          setPostulantes([]);
        }}
      />
    </section>
  );
}

export default PerfilEmpresa;
