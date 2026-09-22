/**
 * @file Entrevistas.jsx
 * @description Vista principal de catálogo de ofertas de entrevista.
 * Incluye barra de búsqueda multi-criterio, filtros dinámicos por tipo, modalidad y salario,
 * y modal de reporte para denunciar contenido inapropiado.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import ReportarEntrevistaModal from "../../components/modals/ReportarEntrevistaModal";
import { entrevistasService } from "../../services/entrevistas.service";
import { useAuth } from "../../hooks/useAuth";
import {
  Search,
  MapPin,
  Building2,
  BriefcaseBusiness,
  Banknote,
  AlertTriangle,
  Eye,
  X,
} from "lucide-react";

const modalidades = ["Todas", "Presencial", "Remoto", "Híbrido"];
const tipos = ["Todos", "Primer empleo", "Profesional", "Emprendedor"];

const rangosSalariales = [
  { id: "todos", texto: "Todos" },
  { id: "menos-1m", texto: "Menos de $1M" },
  { id: "1m-1-5m", texto: "$1M - $1.5M" },
  { id: "1-5m-2-5m", texto: "$1.5M - $2.5M" },
  { id: "2-5m-4m", texto: "$2.5M - $4M" },
  { id: "mas-4m", texto: "Más de $4M" },
  { id: "convenir", texto: "Salario a convenir" },
];

function Entrevistas() {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [entrevistas, setEntrevistas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mensajeError, setMensajeError] = useState("");

  const [busqueda, setBusqueda] = useState("");
  const [modalidadSeleccionada, setModalidadSeleccionada] = useState("Todas");
  const [tipoSeleccionado, setTipoSeleccionado] = useState("Todos");
  const [rangoSalarial, setRangoSalarial] = useState("todos");
  const [entrevistaAReportar, setEntrevistaAReportar] = useState(null);

  useEffect(() => {
    const cargarEntrevistas = async () => {
      try {
        setCargando(true);
        setMensajeError("");

        const resultado = await entrevistasService.listarEntrevistas();
        setEntrevistas(resultado.entrevistas || []);
      } catch (error) {
        console.error("Error cargando entrevistas:", error);
        setMensajeError(error.message || "No se pudieron cargar las entrevistas.");
      } finally {
        setCargando(false);
      }
    };

    cargarEntrevistas();
  }, []);

  const limpiarFiltros = () => {
    setBusqueda("");
    setModalidadSeleccionada("Todas");
    setTipoSeleccionado("Todos");
    setRangoSalarial("todos");
  };

  const cumpleRangoSalarial = (entrevista) => {
    if (rangoSalarial === "todos") return true;

    if (rangoSalarial === "convenir") {
      return entrevista.salarioMin === null && entrevista.salarioMax === null;
    }

    if (entrevista.salarioMin === null || entrevista.salarioMax === null) {
      return false;
    }

    const salarioMin = Number(entrevista.salarioMin);
    const salarioMax = Number(entrevista.salarioMax);

    if (rangoSalarial === "menos-1m") {
      return salarioMax < 1000000;
    }

    if (rangoSalarial === "1m-1-5m") {
      return salarioMin >= 1000000 && salarioMax <= 1500000;
    }

    if (rangoSalarial === "1-5m-2-5m") {
      return salarioMin >= 1500000 && salarioMax <= 2500000;
    }

    if (rangoSalarial === "2-5m-4m") {
      return salarioMin >= 2500000 && salarioMax <= 4000000;
    }

    if (rangoSalarial === "mas-4m") {
      return salarioMin > 4000000;
    }

    return true;
  };

  const entrevistasFiltradas = entrevistas.filter((entrevista) => {
    const textoBusqueda = busqueda.toLowerCase();
    const palabrasClave = entrevista.palabrasClave || [];

    const coincideBusqueda =
      entrevista.titulo?.toLowerCase().includes(textoBusqueda) ||
      entrevista.empresa?.toLowerCase().includes(textoBusqueda) ||
      entrevista.descripcion?.toLowerCase().includes(textoBusqueda) ||
      entrevista.categoria?.toLowerCase().includes(textoBusqueda) ||
      palabrasClave.some((palabra) =>
        palabra.toLowerCase().includes(textoBusqueda)
      );

    const coincideModalidad =
      modalidadSeleccionada === "Todas" ||
      entrevista.modalidad === modalidadSeleccionada;

    const coincideTipo =
      tipoSeleccionado === "Todos" || entrevista.tipo === tipoSeleccionado;

    return (
      coincideBusqueda &&
      coincideModalidad &&
      coincideTipo &&
      cumpleRangoSalarial(entrevista)
    );
  });

  const reportarEntrevista = (event, entrevista) => {
    event.stopPropagation();

    if (!isAuthenticated) {
      alert("Debes iniciar sesión para reportar una entrevista.");
      navigate("/login");
      return;
    }

    setEntrevistaAReportar(entrevista);
  };

  const verDetalles = (entrevista) => {
    navigate(`/entrevistas/${entrevista.id}`);
  };

  if (cargando) {
    return (
      <section className="interviews-page">
        <div className="empty-results">
          <h3>Cargando entrevistas...</h3>
          <p>Estamos consultando las entrevistas publicadas en WorkInX.</p>
        </div>
      </section>
    );
  }

  if (mensajeError) {
    return (
      <section className="interviews-page">
        <div className="empty-results">
          <h3>No se pudieron cargar las entrevistas.</h3>
          <p>{mensajeError}</p>
        </div>
      </section>
    );
  }

  return (
    <section className="interviews-page">
      <div className="interviews-header">
        <p className="section-tag">Entrevistas laborales</p>

        <h1>Encuentra entrevistas según tu perfil y tus necesidades.</h1>

        <p>
          Busca oportunidades por palabra clave, modalidad, tipo de entrevista y
          rango salarial.
        </p>
      </div>

      <div className="interviews-layout">
        <aside className="filters-panel">
          <div className="filter-block">
            <h3>Buscar</h3>

            <div className="search-box">
              <Search size={20} />
              <input
                type="text"
                placeholder="Cargo, empresa o palabra clave"
                value={busqueda}
                onChange={(event) => setBusqueda(event.target.value)}
              />
            </div>
          </div>

          <div className="filter-block">
            <h3>Modalidad</h3>

            <div className="filter-options">
              {modalidades.map((modalidad) => (
                <button
                  key={modalidad}
                  type="button"
                  className={
                    modalidadSeleccionada === modalidad
                      ? "filter-chip selected-filter"
                      : "filter-chip"
                  }
                  onClick={() => setModalidadSeleccionada(modalidad)}
                >
                  {modalidad}
                </button>
              ))}
            </div>
          </div>

          <div className="filter-block">
            <h3>Tipo de entrevista</h3>

            <div className="filter-options">
              {tipos.map((tipo) => (
                <button
                  key={tipo}
                  type="button"
                  className={
                    tipoSeleccionado === tipo
                      ? "filter-chip selected-filter"
                      : "filter-chip"
                  }
                  onClick={() => setTipoSeleccionado(tipo)}
                >
                  {tipo}
                </button>
              ))}
            </div>
          </div>

          <div className="filter-block">
            <h3>Rango salarial</h3>

            <div className="salary-options">
              {rangosSalariales.map((rango) => (
                <button
                  key={rango.id}
                  type="button"
                  className={
                    rangoSalarial === rango.id
                      ? "salary-option selected-filter"
                      : "salary-option"
                  }
                  onClick={() => setRangoSalarial(rango.id)}
                >
                  <Banknote size={18} />
                  {rango.texto}
                </button>
              ))}
            </div>
          </div>

          <button
            type="button"
            className="clear-filters"
            onClick={limpiarFiltros}
          >
            <X size={18} />
            Limpiar filtros
          </button>
        </aside>

        <div className="interviews-content">
          <div className="results-summary">
            <h2>Resultados disponibles</h2>
            <p>{entrevistasFiltradas.length} entrevistas encontradas</p>
          </div>

          <div className="interviews-list">
            {entrevistasFiltradas.map((entrevista) => (
              <article
                key={entrevista.id}
                className="interview-card"
                onClick={() => verDetalles(entrevista)}
              >
                <div className="interview-main">
                  <div className="interview-company">
                    <Building2 size={20} />
                    <span>{entrevista.empresa}</span>
                  </div>

                  <h3>{entrevista.titulo}</h3>

                  <p className="interview-description">
                    {entrevista.descripcion}
                  </p>

                  <div className="interview-tags">
                    <span>
                      <BriefcaseBusiness size={16} />
                      {entrevista.tipo}
                    </span>

                    <span>
                      <MapPin size={16} />
                      {entrevista.ubicacion}
                    </span>

                    <span>
                      <Banknote size={16} />
                      {entrevista.salarioTexto}
                    </span>
                  </div>
                </div>

                <div className="interview-actions">
                  <button
                    type="button"
                    className="details-button"
                    onClick={(event) => {
                      event.stopPropagation();
                      verDetalles(entrevista);
                    }}
                  >
                    <Eye size={18} />
                    Ver detalles
                  </button>

                  <button
                    type="button"
                    className="report-button"
                    onClick={(event) => reportarEntrevista(event, entrevista)}
                  >
                    <AlertTriangle size={18} />
                    {isAuthenticated ? "Reportar" : "Ingresar"}
                  </button>
                </div>
              </article>
            ))}

            {entrevistasFiltradas.length === 0 && (
              <div className="empty-results">
                <h3>No encontramos entrevistas con esos filtros.</h3>
                <p>
                  Intenta cambiar la búsqueda, la modalidad, el tipo de
                  entrevista o el rango salarial.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>

      <ReportarEntrevistaModal
        entrevista={entrevistaAReportar}
        onClose={() => setEntrevistaAReportar(null)}
      />
    </section>
  );
}

export default Entrevistas;
