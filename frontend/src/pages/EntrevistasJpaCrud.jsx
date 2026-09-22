import React, { useState, useEffect } from 'react';
import { entrevistasJpaService } from '../services/entrevistasJpa.service';
import '../styles/entrevistasJpa.css';

/**
 * Módulo de Gestión Avanzada de Entrevistas.
 * Diseño pulido, alertas elegantes y arquitectura súper organizada.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
export default function EntrevistasJpaCrud() {
  const [entrevistas, setEntrevistas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [servidorOffline, setServidorOffline] = useState(false);
  const [error, setError] = useState(null);
  const [mensajeExito, setMensajeExito] = useState(null);

  // Estado de Paginación
  const [paginaActual, setPaginaActual] = useState(0);
  const [tamanoPagina, setTamanoPagina] = useState(5);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [totalElementos, setTotalElementos] = useState(0);

  // Filtros de Búsqueda
  const [modoBusqueda, setModoBusqueda] = useState('');
  const [filtroTitulo, setFiltroTitulo] = useState('');
  const [filtroDescripcion, setFiltroDescripcion] = useState('');
  const [filtroModalidad, setFiltroModalidad] = useState('');
  const [filtroEstado, setFiltroEstado] = useState('publicada');

  // Formulario Crear / Editar
  const [idEditando, setIdEditando] = useState(null);
  const [titulo, setTitulo] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [tipo, setTipo] = useState('primer_empleo');
  const [modalidad, setModalidad] = useState('presencial');
  const [ubicacion, setUbicacion] = useState('Medellín, Antioquia');
  const [estado, setEstado] = useState('publicada');
  const [activa, setActiva] = useState(true);

  useEffect(() => {
    cargarEntrevistas();
  }, [paginaActual, tamanoPagina, modoBusqueda]);

  const cargarEntrevistas = async () => {
    try {
      setCargando(true);
      setError(null);

      const params = {
        page: paginaActual,
        size: tamanoPagina,
        modo: modoBusqueda,
        titulo: filtroTitulo,
        descripcion: filtroDescripcion,
        modalidad: filtroModalidad,
        estado: filtroEstado
      };

      const data = await entrevistasJpaService.listarPaginado(params);
      
      if (data.serverOffline) {
        setServidorOffline(true);
        setEntrevistas([]);
        setTotalPaginas(0);
        setTotalElementos(0);
      } else {
        setServidorOffline(false);
        setEntrevistas(data.content || []);
        setTotalPaginas(data.totalPages || 0);
        setTotalElementos(data.totalElements || 0);
      }
    } catch (err) {
      console.error('Error al cargar entrevistas:', err);
      setError(err.message);
    } finally {
      setCargando(false);
    }
  };

  const handleEjecutarBusqueda = (e) => {
    e.preventDefault();
    setPaginaActual(0);
    cargarEntrevistas();
  };

  const handleLimpiarBusqueda = () => {
    setModoBusqueda('');
    setFiltroTitulo('');
    setFiltroDescripcion('');
    setFiltroModalidad('');
    setFiltroEstado('publicada');
    setPaginaActual(0);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      titulo,
      descripcion,
      tipo,
      modalidad,
      ubicacion,
      estado,
      activa
    };

    try {
      if (idEditando) {
        await entrevistasJpaService.actualizar(idEditando, payload);
        setMensajeExito(`Entrevista #${idEditando} actualizada con éxito.`);
      } else {
        await entrevistasJpaService.crear(payload);
        setMensajeExito('Nueva oferta de entrevista registrada exitosamente.');
      }
      limpiarFormulario();
      cargarEntrevistas();
    } catch (err) {
      setError(err.message);
    }
  };

  const handleEditar = (item) => {
    setIdEditando(item.id);
    setTitulo(item.titulo || '');
    setDescripcion(item.descripcion || '');
    setTipo(item.tipo || 'primer_empleo');
    setModalidad(item.modalidad || 'presencial');
    setUbicacion(item.ubicacion || 'Medellín, Antioquia');
    setEstado(item.estado || 'publicada');
    setActiva(item.activa !== false);
  };

  const handleEliminar = async (id) => {
    if (!window.confirm(`¿Estás seguro de que deseas eliminar la entrevista #${id}?`)) return;

    try {
      await entrevistasJpaService.eliminar(id);
      setMensajeExito(`Entrevista #${id} eliminada correctamente.`);
      cargarEntrevistas();
    } catch (err) {
      setError(err.message);
    }
  };

  const limpiarFormulario = () => {
    setIdEditando(null);
    setTitulo('');
    setDescripcion('');
    setTipo('primer_empleo');
    setModalidad('presencial');
    setUbicacion('Medellín, Antioquia');
    setEstado('publicada');
    setActiva(true);
  };

  return (
    <div className="container mx-auto px-6 max-w-7xl jpa-module-container">
      {/* Banner Superior Hero */}
      <div className="jpa-hero-card mb-6">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="jpa-hero-title">
              <span>💼</span> Gestión de Entrevistas Laborales
            </h1>
            <p className="jpa-hero-subtitle">
              Panel de administración de ofertas, filtrado dinámico y control de publicaciones.
            </p>
          </div>
          <div className="flex items-center gap-3">
            {servidorOffline ? (
              <span className="bg-amber-500/10 text-amber-300 border border-amber-500/30 text-xs px-3 py-1.5 rounded-full font-medium flex items-center gap-1.5 backdrop-blur-sm">
                <span className="w-2 h-2 rounded-full bg-amber-400"></span> Esperando Backend (Puerto 3000)
              </span>
            ) : (
              <span className="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 text-xs px-3 py-1.5 rounded-full font-medium flex items-center gap-1.5 backdrop-blur-sm">
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span> Servidor Conectado
              </span>
            )}
          </div>
        </div>
      </div>

      {/* NOTIFICACIONES Y ALERTAS ELEGANTES */}
      {error && (
        <div className="jpa-alert jpa-alert-error">
          <div className="jpa-alert-icon-box">🛡️</div>
          <div className="jpa-alert-content">
            <h4 className="jpa-alert-title">Aviso de Validación / Sistema</h4>
            <p className="jpa-alert-desc">{error}</p>
          </div>
          <button onClick={() => setError(null)} className="jpa-alert-close" title="Cerrar aviso">✕</button>
        </div>
      )}

      {mensajeExito && (
        <div className="jpa-alert jpa-alert-success">
          <div className="jpa-alert-icon-box">✨</div>
          <div className="jpa-alert-content">
            <h4 className="jpa-alert-title">Operación Exitosa</h4>
            <p className="jpa-alert-desc">{mensajeExito}</p>
          </div>
          <button onClick={() => setMensajeExito(null)} className="jpa-alert-close" title="Cerrar aviso">✕</button>
        </div>
      )}

      {/* ESTRUCTURA PRINCIPAL DE 2 COLUMNAS */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 items-start">
        
        {/* COLUMNA IZQUIERDA: FORMULARIO CREAR / EDITAR */}
        <div className="jpa-card lg:col-span-1 sticky top-24">
          <div className="jpa-card-header">
            <h2 className="jpa-card-title">
              <span>{idEditando ? '✏️' : '➕'}</span>
              {idEditando ? `Editar Entrevista #${idEditando}` : 'Nueva Oferta'}
            </h2>
            {idEditando && (
              <span className="text-[11px] bg-amber-50 text-amber-700 px-2 py-0.5 rounded font-semibold border border-amber-200">
                Edición activa
              </span>
            )}
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="jpa-label">
                Título del Cargo <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                value={titulo}
                onChange={(e) => setTitulo(e.target.value)}
                placeholder="Ej. Desarrollador Java Backend"
                required
                className="jpa-input"
              />
            </div>

            <div>
              <label className="jpa-label">Descripción</label>
              <textarea
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
                placeholder="Detalles del cargo o requisitos..."
                rows="3"
                className="jpa-textarea"
              ></textarea>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="jpa-label">Modalidad</label>
                <select
                  value={modalidad}
                  onChange={(e) => setModalidad(e.target.value)}
                  className="jpa-select"
                >
                  <option value="presencial">Presencial</option>
                  <option value="remoto">Remoto</option>
                  <option value="hibrido">Híbrido</option>
                </select>
              </div>

              <div>
                <label className="jpa-label">Estado</label>
                <select
                  value={estado}
                  onChange={(e) => setEstado(e.target.value)}
                  className="jpa-select"
                >
                  <option value="publicada">Publicada</option>
                  <option value="pausada">Pausada</option>
                  <option value="cerrada">Cerrada</option>
                </select>
              </div>
            </div>

            <div className="flex items-center gap-2 pt-1">
              <input
                type="checkbox"
                id="activaEntrevista"
                checked={activa}
                onChange={(e) => setActiva(e.target.checked)}
                className="w-4 h-4 text-blue-600 rounded border-slate-300 focus:ring-blue-500"
              />
              <label htmlFor="activaEntrevista" className="text-xs font-semibold text-slate-700">
                Publicación Activa
              </label>
            </div>

            <div className="pt-2 flex gap-2">
              <button type="submit" className="jpa-btn-primary w-full">
                {idEditando ? 'Guardar Cambios' : 'Publicar Entrevista'}
              </button>
              {idEditando && (
                <button type="button" onClick={limpiarFormulario} className="jpa-btn-secondary">
                  Cancelar
                </button>
              )}
            </div>
          </form>
        </div>

        {/* COLUMNA DERECHA: PANEL DE BÚSQUEDA + TABLA DE RESULTADOS */}
        <div className="lg:col-span-2 space-y-6">
          
          {/* TABLA PRINCIPAL Y CONTROLES */}
          <div className="jpa-card">
            
            {/* BARRA SUPERIOR DE LA TABLA CON FILTROS INTEGRADOS */}
            <div className="jpa-card-header">
              <div>
                <h2 className="jpa-card-title">
                  <span>📋</span> Ofertas de Entrevistas
                </h2>
                <p className="text-xs text-slate-500 mt-0.5">Mostrando {entrevistas.length} de {totalElementos} registros</p>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={cargarEntrevistas}
                  className="text-xs text-slate-600 hover:text-blue-600 bg-slate-100 px-2.5 py-1 rounded-lg border border-slate-200 transition font-medium mr-2"
                >
                  🔄 Recargar
                </button>
                <label className="text-xs font-medium text-slate-500">Mostrar:</label>
                <select
                  value={tamanoPagina}
                  onChange={(e) => {
                    setTamanoPagina(Number(e.target.value));
                    setPaginaActual(0);
                  }}
                  className="jpa-select px-2 py-1"
                  style={{ width: 'auto' }}
                >
                  <option value={5}>5 por pág.</option>
                  <option value={10}>10 por pág.</option>
                  <option value={20}>20 por pág.</option>
                </select>
              </div>
            </div>

            {/* FILTROS DE BÚSQUEDA INTEGRADOS */}
            <div className="jpa-filter-box">
              <form onSubmit={handleEjecutarBusqueda} className="grid grid-cols-1 sm:grid-cols-4 gap-3 items-end">
                <div>
                  <label className="jpa-label">Criterio</label>
                  <select
                    value={modoBusqueda}
                    onChange={(e) => setModoBusqueda(e.target.value)}
                    className="jpa-select"
                  >
                    <option value="">Ver Todas (General)</option>
                    <option value="and">Por Título y Estado</option>
                    <option value="or">Por Título, Desc. o Modalidad</option>
                  </select>
                </div>

                <div>
                  <label className="jpa-label">Título</label>
                  <input
                    type="text"
                    value={filtroTitulo}
                    onChange={(e) => setFiltroTitulo(e.target.value)}
                    placeholder="Ej. Desarrollador"
                    className="jpa-input"
                  />
                </div>

                <div>
                  <label className="jpa-label">
                    {modoBusqueda === 'or' ? 'Modalidad' : 'Estado'}
                  </label>
                  <select
                    value={modoBusqueda === 'or' ? filtroModalidad : filtroEstado}
                    onChange={(e) => modoBusqueda === 'or' ? setFiltroModalidad(e.target.value) : setFiltroEstado(e.target.value)}
                    className="jpa-select"
                  >
                    {modoBusqueda === 'or' ? (
                      <>
                        <option value="">Todas las modalidades</option>
                        <option value="presencial">Presencial</option>
                        <option value="remoto">Remoto</option>
                        <option value="hibrido">Híbrido</option>
                      </>
                    ) : (
                      <>
                        <option value="publicada">Publicada</option>
                        <option value="pausada">Pausada</option>
                        <option value="cerrada">Cerrada</option>
                      </>
                    )}
                  </select>
                </div>

                <div className="flex gap-2">
                  <button type="submit" className="jpa-btn-primary w-full">
                    Filtrar
                  </button>
                  <button type="button" onClick={handleLimpiarBusqueda} className="jpa-btn-secondary">
                    Limpiar
                  </button>
                </div>
              </form>
            </div>

            {/* CONTENIDO DE LA TABLA */}
            {cargando ? (
              <div className="text-slate-400 py-12 text-center text-xs flex flex-col items-center justify-center gap-2">
                <div className="w-5 h-5 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                Cargando registros...
              </div>
            ) : servidorOffline ? (
              <div className="bg-slate-50 border border-dashed border-slate-300 rounded-xl p-8 text-center my-4">
                <div className="text-2xl mb-2">🔌</div>
                <h3 className="text-sm font-semibold text-slate-700 mb-1">El servidor Backend está desconectado</h3>
                <p className="text-xs text-slate-500 max-w-md mx-auto mb-4">
                  Para ver y administrar las entrevistas en vivo, inicia el backend en Spring Boot (Puerto 3000).
                </p>
                <button
                  type="button"
                  onClick={cargarEntrevistas}
                  className="jpa-btn-primary text-xs"
                >
                  🔄 Intentar Reconectar
                </button>
              </div>
            ) : entrevistas.length === 0 ? (
              <div className="text-slate-400 py-12 text-center text-xs">No se encontraron entrevistas registradas.</div>
            ) : (
              <>
                <div className="overflow-x-auto">
                  <table className="jpa-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Título del Cargo</th>
                        <th>Modalidad</th>
                        <th>Estado</th>
                        <th className="text-right">Acciones</th>
                      </tr>
                    </thead>
                    <tbody>
                      {entrevistas.map((item) => (
                        <tr key={item.id}>
                          <td className="font-mono text-blue-600 font-bold">#{item.id}</td>
                          <td>
                            <div className="font-semibold text-slate-800">{item.titulo}</div>
                            <div className="text-[11px] text-slate-400 truncate max-w-xs">{item.descripcion || '-'}</div>
                          </td>
                          <td className="capitalize font-medium text-slate-600">{item.modalidad || 'presencial'}</td>
                          <td>
                            <span className={`jpa-badge ${
                              item.estado === 'publicada' ? 'jpa-badge-publicada' :
                              item.estado === 'pausada' ? 'jpa-badge-pausada' : 'jpa-badge-cerrada'
                            }`}>
                              {item.estado || 'publicada'}
                            </span>
                          </td>
                          <td className="text-right space-x-1.5">
                            <button
                              onClick={() => handleEditar(item)}
                              className="jpa-btn-secondary py-1 px-2.5 text-xs"
                            >
                              Editar
                            </button>
                            <button
                              onClick={() => handleEliminar(item.id)}
                              className="jpa-btn-secondary py-1 px-2.5 text-xs text-rose-600 hover:text-rose-700 hover:bg-rose-50"
                            >
                              Eliminar
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>

                {/* PAGINACIÓN */}
                <div className="flex justify-between items-center mt-6 pt-4 border-t border-slate-100 text-xs">
                  <span className="text-slate-500 font-medium">
                    Página <strong className="text-slate-800">{paginaActual + 1}</strong> de <strong className="text-slate-800">{totalPaginas || 1}</strong>
                  </span>

                  <div className="flex gap-1.5">
                    <button
                      disabled={paginaActual === 0}
                      onClick={() => setPaginaActual((prev) => Math.max(0, prev - 1))}
                      className="jpa-page-btn"
                    >
                      ← Anterior
                    </button>

                    {Array.from({ length: totalPaginas }, (_, i) => i).map((pageIndex) => (
                      <button
                        key={pageIndex}
                        onClick={() => setPaginaActual(pageIndex)}
                        className={`jpa-page-btn ${paginaActual === pageIndex ? 'active' : ''}`}
                      >
                        {pageIndex + 1}
                      </button>
                    ))}

                    <button
                      disabled={paginaActual >= totalPaginas - 1}
                      onClick={() => setPaginaActual((prev) => prev + 1)}
                      className="jpa-page-btn"
                    >
                      Siguiente →
                    </button>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>

      </div>
    </div>
  );
}
