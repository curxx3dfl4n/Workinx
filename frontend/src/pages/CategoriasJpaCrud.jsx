/**
 * @file CategoriasJpaCrud.jsx
 * @description Componente de vista para la gestión integral de Categorías JPA en WorkInX.
 * Implementa los Retos SENA ADSO:
 * 1. Búsqueda combinada por 2 campos (AND) y 3 campos (OR)
 * 2. Manejo de excepciones globales
 * 3. Logs del sistema e información de auditoría
 * 4. 5 validaciones con anotaciones en la entidad (@Valid)
 * 5. Paginación interactiva con JPA Repository Pageable
 * MILLA EXTRA: Rate Limiting (60 req/min) y Anti-SQLi / Exploits
 * PLUS: Auditoría NoSQL en MongoDB
 * @author Equipo WorkInX - SENA ADSO 2026
 */

import React, { useState, useEffect } from 'react';
import { categoriasJpaService } from '../services/categoriasJpa.service';

/**
 * Componente principal para el CRUD y consulta de Categorías JPA.
 * Ofrece una interfaz completa con soporte para filtros multicriterio, paginación en tiempo real,
 * formulario reactivo con validaciones y alertas contextuales de éxito y error.
 * 
 * @component
 * @returns {JSX.Element} Vista del panel de administración de Categorías JPA.
 */
export default function CategoriasJpaCrud() {
  // ==========================================
  // Estados Generales de la Vista y Consulta
  // ==========================================
  const [categorias, setCategorias] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState(null);
  const [mensajeExito, setMensajeExito] = useState(null);

  // ==========================================
  // RETO 5: Estados para Paginación (Pageable)
  // ==========================================
  const [paginaActual, setPaginaActual] = useState(0);
  const [tamanoPagina, setTamanoPagina] = useState(5);
  const [totalPaginas, setTotalPaginas] = useState(0);
  const [totalElementos, setTotalElementos] = useState(0);

  // ==========================================
  // RETO 1: Filtros de Búsqueda Avanzada (AND / OR)
  // ==========================================
  const [modoBusqueda, setModoBusqueda] = useState(''); // '': sin filtro, 'and': 2 campos, 'or': 3 campos
  const [filtroNombre, setFiltroNombre] = useState('');
  const [filtroDescripcion, setFiltroDescripcion] = useState('');
  const [filtroActiva, setFiltroActiva] = useState(true);

  // ==========================================
  // Estados para Formulario Crear / Editar (RETO 4)
  // ==========================================
  const [idEditando, setIdEditando] = useState(null); // null indica modo creación; un ID indica modo edición
  const [nombre, setNombre] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [activa, setActiva] = useState(true);

  /**
   * Efecto secundario para recargar los datos cuando cambian la página actual,
   * el tamaño de página o el modo de búsqueda seleccionado.
   */
  useEffect(() => {
    cargarCategoriasPaginadas();
  }, [paginaActual, tamanoPagina, modoBusqueda]);

  /**
   * Consulta las categorías paginadas desde el microservicio JPA aplicando los filtros activos.
   * Actualiza el listado, el total de páginas y el contador global de elementos.
   * 
   * @async
   * @function cargarCategoriasPaginadas
   * @returns {Promise<void>}
   */
  const cargarCategoriasPaginadas = async () => {
    try {
      setCargando(true);
      setError(null);

      // Ensamblaje de parámetros de consulta para la petición HTTP
      const params = {
        page: paginaActual,
        size: tamanoPagina,
        modo: modoBusqueda,
        nombre: filtroNombre,
        descripcion: filtroDescripcion,
        activa: filtroActiva
      };

      const data = await categoriasJpaService.listarPaginado(params);
      
      // Actualización de estados con la estructura Spring Data Page (content, totalPages, totalElements)
      setCategorias(data.content || []);
      setTotalPaginas(data.totalPages || 0);
      setTotalElementos(data.totalElements || 0);
    } catch (err) {
      console.error('Error al cargar datos desde JPA:', err);
      setError(err.message || 'Error al conectar con el Microservicio JPA en http://localhost:8081.');
    } finally {
      setCargando(false);
    }
  };

  /**
   * Manejador del evento de envío del formulario de búsqueda.
   * Reinicia la página a la primera posición (índice 0) y ejecuta la consulta.
   * 
   * @function handleEjecutarBusqueda
   * @param {React.FormEvent<HTMLFormElement>} e - Evento de submit del formulario.
   */
  const handleEjecutarBusqueda = (e) => {
    e.preventDefault();
    setPaginaActual(0);
    cargarCategoriasPaginadas();
  };

  /**
   * Restablece los filtros de búsqueda a sus valores iniciales por defecto.
   * 
   * @function handleLimpiarBusqueda
   */
  const handleLimpiarBusqueda = () => {
    setModoBusqueda('');
    setFiltroNombre('');
    setFiltroDescripcion('');
    setFiltroActiva(true);
    setPaginaActual(0);
  };

  /**
   * Procesa el envío del formulario para crear una nueva categoría o actualizar una existente.
   * Valida en el backend mediante Bean Validation (@Valid) y notifica el resultado.
   * 
   * @async
   * @function handleSubmit
   * @param {React.FormEvent<HTMLFormElement>} e - Evento de submit del formulario.
   * @returns {Promise<void>}
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    try {
      if (idEditando) {
        // Actualización de registro existente
        await categoriasJpaService.actualizar(idEditando, { nombre, descripcion, activa });
        setMensajeExito(`Categoría ID ${idEditando} actualizada con éxito (Auditado en Mongo & JPA).`);
      } else {
        // Creación de nuevo registro
        await categoriasJpaService.crear({ nombre, descripcion, activa });
        setMensajeExito('Nueva categoría creada con éxito con validaciones de entidad @Valid.');
      }
      limpiarFormulario();
      cargarCategoriasPaginadas();
    } catch (err) {
      console.error('Error en formulario:', err);
      setError(err.message);
    }
  };

  /**
   * Prepara el formulario para editar una categoría existente cargando sus datos en los estados.
   * 
   * @function handleEditar
   * @param {Object} cat - Objeto con la información de la categoría a modificar.
   */
  const handleEditar = (cat) => {
    setIdEditando(cat.id);
    setNombre(cat.nombre);
    setDescripcion(cat.descripcion || '');
    setActiva(cat.activa !== false);
  };

  /**
   * Solicita confirmación al usuario y elimina la categoría seleccionada en el backend.
   * 
   * @async
   * @function handleEliminar
   * @param {number|string} id - Identificador de la categoría a eliminar.
   * @returns {Promise<void>}
   */
  const handleEliminar = async (id) => {
    if (!window.confirm(`¿Eliminar la categoría ID ${id} en el Microservicio JPA?`)) return;

    try {
      await categoriasJpaService.eliminar(id);
      setMensajeExito(`Categoría ID ${id} eliminada correctamente.`);
      cargarCategoriasPaginadas();
    } catch (err) {
      setError(err.message);
    }
  };

  /**
   * Restablece los campos del formulario y cancela el modo de edición.
   * 
   * @function limpiarFormulario
   */
  const limpiarFormulario = () => {
    setIdEditando(null);
    setNombre('');
    setDescripcion('');
    setActiva(true);
  };

  return (
    <div className="container mx-auto p-6 max-w-6xl">
      {/* Banner Informativo de Retos SENA */}
      <div className="bg-slate-900 text-white p-6 rounded-xl shadow-xl mb-8 border-l-8 border-indigo-500">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h1 className="text-2xl font-extrabold tracking-tight flex items-center gap-2 text-indigo-300">
              🚀 RETOS SENA ADSO: Microservicio JPA + MongoDB + Paginación
            </h1>
            <p className="text-slate-300 text-sm mt-1">
              Servidor 2 (<code className="text-amber-400 font-mono">http://localhost:8081</code>) | 
              Spring Data JPA + MongoDB + Rate Limiting + Anti-SQLi
            </p>
          </div>
          <div className="flex flex-wrap gap-2 text-xs font-semibold">
            <span className="bg-emerald-800 text-emerald-200 px-3 py-1 rounded-full border border-emerald-600">
              🍃 PLUS: Mongo Audit
            </span>
            <span className="bg-amber-800 text-amber-200 px-3 py-1 rounded-full border border-amber-600">
              ⚡ Rate Limit (60/min)
            </span>
            <span className="bg-indigo-800 text-indigo-200 px-3 py-1 rounded-full border border-indigo-600">
              🔒 5+ Validaciones @Valid
            </span>
          </div>
        </div>
      </div>

      {/* Alertas Contextuales de Error y Éxito */}
      {error && (
        <div className="bg-red-100 border-l-4 border-red-500 text-red-800 p-4 mb-6 rounded-lg shadow">
          <p className="font-bold flex items-center gap-2">⚠️ Error / Alerta de Seguridad</p>
          <p className="text-sm mt-1">{error}</p>
        </div>
      )}

      {mensajeExito && (
        <div className="bg-emerald-100 border-l-4 border-emerald-500 text-emerald-800 p-4 mb-6 rounded-lg shadow flex justify-between items-center">
          <p className="text-sm font-medium">✅ {mensajeExito}</p>
          <button onClick={() => setMensajeExito(null)} className="font-bold text-lg">×</button>
        </div>
      )}

      {/* RETO 1: PANEL DE BÚSQUEDAS AVANZADAS (AND / OR) */}
      <div className="bg-white p-5 rounded-xl shadow-md border border-slate-200 mb-8">
        <h2 className="text-lg font-bold text-slate-800 mb-3 flex items-center gap-2">
          🔍 RETO 1: Búsquedas Combinadas (Operador AND / OR)
        </h2>

        <form onSubmit={handleEjecutarBusqueda} className="grid grid-cols-1 md:grid-cols-4 gap-4 items-end">
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Modo de Búsqueda</label>
            <select
              value={modoBusqueda}
              onChange={(e) => setModoBusqueda(e.target.value)}
              className="w-full px-3 py-2 border rounded-md text-sm outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="">Todas las categorías (Sin filtro)</option>
              <option value="and">2 Campos con operador (AND / Y)</option>
              <option value="or">3 Campos con operador (OR / O)</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Nombre (Campo 1)</label>
            <input
              type="text"
              value={filtroNombre}
              onChange={(e) => setFiltroNombre(e.target.value)}
              placeholder="Ej. Tecnología"
              className="w-full px-3 py-2 border rounded-md text-sm outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">Descripción (Campo 2)</label>
            <input
              type="text"
              value={filtroDescripcion}
              onChange={(e) => setFiltroDescripcion(e.target.value)}
              placeholder="Ej. Software"
              className="w-full px-3 py-2 border rounded-md text-sm outline-none focus:ring-2 focus:ring-indigo-500"
            />
          </div>

          <div className="flex gap-2">
            <button
              type="submit"
              className="flex-1 bg-indigo-600 text-white py-2 rounded-md hover:bg-indigo-700 text-sm font-semibold transition"
            >
              Buscar
            </button>
            <button
              type="button"
              onClick={handleLimpiarBusqueda}
              className="bg-slate-200 text-slate-700 px-3 py-2 rounded-md hover:bg-slate-300 text-sm transition"
            >
              Limpiar
            </button>
          </div>
        </form>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
        {/* RETO 4: FORMULARIO CON 5 VALIDACIONES EN ENTITY */}
        <div className="bg-white p-6 rounded-xl shadow-md border border-slate-200">
          <h2 className="text-lg font-bold mb-4 text-slate-800">
            {idEditando ? `✏️ Editar #${idEditando}` : '➕ Crear Categoría (5 Validaciones)'}
          </h2>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Nombre <span className="text-red-500">* (@NotBlank, @Size, @Pattern)</span>
              </label>
              <input
                type="text"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)}
                placeholder="Ej. Desarrollo Web"
                required
                className="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
              />
              <span className="text-xs text-slate-400">Min 3, Max 80 caracteres. Sin símbolos SQL.</span>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-700 mb-1">
                Descripción <span className="text-slate-400">(@Size max 500)</span>
              </label>
              <textarea
                value={descripcion}
                onChange={(e) => setDescripcion(e.target.value)}
                placeholder="Descripción de la categoría..."
                rows="3"
                className="w-full px-3 py-2 border rounded-md text-sm focus:ring-2 focus:ring-indigo-500 outline-none"
              ></textarea>
            </div>

            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="activa"
                checked={activa}
                onChange={(e) => setActiva(e.target.checked)}
                className="w-4 h-4 text-indigo-600 rounded"
              />
              <label htmlFor="activa" className="text-xs font-semibold text-slate-700">
                Categoría Activa (@NotNull)
              </label>
            </div>

            <div className="pt-2 flex gap-2">
              <button
                type="submit"
                className="flex-1 bg-indigo-600 text-white py-2 rounded-md hover:bg-indigo-700 text-sm font-semibold transition"
              >
                {idEditando ? 'Guardar Cambios' : 'Crear en JPA'}
              </button>
              {idEditando && (
                <button
                  type="button"
                  onClick={limpiarFormulario}
                  className="bg-slate-200 text-slate-700 px-3 py-2 rounded-md hover:bg-slate-300 text-sm"
                >
                  Cancelar
                </button>
              )}
            </div>
          </form>
        </div>

        {/* RETO 5: LISTADO Y CONTROLES DE PAGINACIÓN */}
        <div className="md:col-span-2 bg-white p-6 rounded-xl shadow-md border border-slate-200">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-bold text-slate-800">📋 Categorías Paginadas (JPA Pageable)</h2>
              <p className="text-xs text-slate-500">Mostrando {categorias.length} de {totalElementos} registros totales</p>
            </div>

            <div className="flex items-center gap-2">
              <label className="text-xs text-slate-600">Por pág:</label>
              <select
                value={tamanoPagina}
                onChange={(e) => {
                  setTamanoPagina(Number(e.target.value));
                  setPaginaActual(0);
                }}
                className="border text-xs rounded px-2 py-1 outline-none"
              >
                <option value={5}>5</option>
                <option value={10}>10</option>
                <option value={20}>20</option>
              </select>
            </div>
          </div>

          {cargando ? (
            <p className="text-slate-500 py-8 text-center text-sm">Cargando datos desde JPA en el Servidor 2...</p>
          ) : categorias.length === 0 ? (
            <p className="text-slate-500 py-8 text-center text-sm">No se encontraron resultados para la consulta.</p>
          ) : (
            <>
              {/* Tabla de registros */}
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse text-xs">
                  <thead>
                    <tr className="bg-slate-100 border-b text-slate-700">
                      <th className="p-3 font-semibold">ID</th>
                      <th className="p-3 font-semibold">Nombre</th>
                      <th className="p-3 font-semibold">Descripción</th>
                      <th className="p-3 font-semibold">Estado</th>
                      <th className="p-3 font-semibold text-right">Acciones</th>
                    </tr>
                  </thead>
                  <tbody>
                    {categorias.map((cat) => (
                      <tr key={cat.id} className="border-b hover:bg-slate-50 transition">
                        <td className="p-3 font-mono text-indigo-600 font-bold">#{cat.id}</td>
                        <td className="p-3 font-medium text-slate-900">{cat.nombre}</td>
                        <td className="p-3 text-slate-600 max-w-xs truncate">{cat.descripcion || '-'}</td>
                        <td className="p-3">
                          <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                            cat.activa ? 'bg-emerald-100 text-emerald-800' : 'bg-red-100 text-red-800'
                          }`}>
                            {cat.activa ? 'ACTIVA' : 'INACTIVA'}
                          </span>
                        </td>
                        <td className="p-3 text-right space-x-1">
                          <button
                            onClick={() => handleEditar(cat)}
                            className="bg-amber-100 text-amber-800 px-2 py-1 rounded hover:bg-amber-200 font-medium"
                          >
                            Editar
                          </button>
                          <button
                            onClick={() => handleEliminar(cat.id)}
                            className="bg-red-100 text-red-800 px-2 py-1 rounded hover:bg-red-200 font-medium"
                          >
                            Eliminar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* CONTROLES DE PAGINACIÓN (RETO 5) */}
              <div className="flex justify-between items-center mt-6 pt-4 border-t border-slate-100 text-xs">
                <span className="text-slate-500">
                  Página <strong className="text-slate-800">{paginaActual + 1}</strong> de <strong className="text-slate-800">{totalPaginas || 1}</strong>
                </span>

                <div className="flex gap-2">
                  <button
                    disabled={paginaActual === 0}
                    onClick={() => setPaginaActual((prev) => Math.max(0, prev - 1))}
                    className="px-3 py-1.5 border rounded-md bg-white hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed font-medium"
                  >
                    ← Anterior
                  </button>

                  {Array.from({ length: totalPaginas }, (_, i) => i).map((pageIndex) => (
                    <button
                      key={pageIndex}
                      onClick={() => setPaginaActual(pageIndex)}
                      className={`px-3 py-1.5 border rounded-md font-semibold ${
                        paginaActual === pageIndex ? 'bg-indigo-600 text-white' : 'bg-white text-slate-700 hover:bg-slate-50'
                      }`}
                    >
                      {pageIndex + 1}
                    </button>
                  ))}

                  <button
                    disabled={paginaActual >= totalPaginas - 1}
                    onClick={() => setPaginaActual((prev) => prev + 1)}
                    className="px-3 py-1.5 border rounded-md bg-white hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed font-medium"
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
  );
}
