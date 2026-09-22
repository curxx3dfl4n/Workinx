/**
 * @file MapaPicker.jsx
 * @description Componente de mapa interactivo con Leaflet para la selección geográfica de citaciones.
 * Permite a la empresa buscar direcciones y seleccionar mediante pin de mapa la latitud/longitud.
 * 
 * @author Equipo WorkInX - SENA ADSO 2026
 */
import { useEffect, useRef, useState } from "react";
import { MapPin, Search, X } from "lucide-react";

import "leaflet/dist/leaflet.css";

const LAT_DEFAULT = 6.2442;
const LNG_DEFAULT = -75.5812;
const ZOOM_DEFAULT = 11;
const ZOOM_SELECCION = 16;

const BOUNDS_ANTIOQUIA = [
  [5.4, -77.1],
  [8.9, -73.8],
];

/**
 * Componente modal interactivo para selección geográfica de coordenadas.
 *
 * @param {Object} props - Propiedades del componente
 * @param {number} [props.latitudInicial] - Latitud existente
 * @param {number} [props.longitudInicial] - Longitud existente
 * @param {Function} props.onSeleccionar - Callback invocado al confirmar la selección
 * @param {Function} props.onCerrar - Callback para cerrar la ventana modal
 * @returns {JSX.Element} Modal del mapa selector
 */
function MapaPicker({ latitudInicial, longitudInicial, onSeleccionar, onCerrar }) {
  const mapaRef = useRef(null);
  const instanciaRef = useRef(null);
  const marcadorRef = useRef(null);

  const [busqueda, setBusqueda] = useState("");
  const [buscando, setBuscando] = useState(false);
  const [direccionActual, setDireccionActual] = useState("");
  const [latActual, setLatActual] = useState(latitudInicial || null);
  const [lngActual, setLngActual] = useState(longitudInicial || null);
  const [errorBusqueda, setErrorBusqueda] = useState("");

  // Inicializar Leaflet
  useEffect(() => {
    if (instanciaRef.current) return;

    import("leaflet").then((L) => {
      delete L.default.Icon.Default.prototype._getIconUrl;
      L.default.Icon.Default.mergeOptions({
        iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
        iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
        shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
      });

      const lat = latitudInicial || LAT_DEFAULT;
      const lng = longitudInicial || LNG_DEFAULT;
      const zoom = latitudInicial ? ZOOM_SELECCION : ZOOM_DEFAULT;

      const mapa = L.default.map(mapaRef.current, {
        maxBounds: BOUNDS_ANTIOQUIA,
        maxBoundsViscosity: 1.0,
        minZoom: 7,
      }).setView([lat, lng], zoom);

      instanciaRef.current = mapa;

      L.default.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: '© OpenStreetMap',
        maxZoom: 19,
      }).addTo(mapa);

      if (latitudInicial && longitudInicial) {
        marcadorRef.current = L.default.marker([latitudInicial, longitudInicial]).addTo(mapa);
      }

      mapa.on("click", async (e) => {
        const { lat, lng } = e.latlng;
        // Solo permitir clics dentro de los bounds
        const bounds = L.default.latLngBounds(BOUNDS_ANTIOQUIA);
        if (!bounds.contains(e.latlng)) {
          setErrorBusqueda("Por favor, selecciona una ubicación dentro de Antioquia.");
          return;
        }
        setErrorBusqueda("");
        colocarMarcador(L.default, mapa, lat, lng);
        const dir = await geocodificarInverso(lat, lng);
        setDireccionActual(dir);
        setLatActual(lat);
        setLngActual(lng);
      });
    });

    return () => {
      if (instanciaRef.current) {
        instanciaRef.current.remove();
        instanciaRef.current = null;
      }
    };
  }, []);

  const colocarMarcador = (L, mapa, lat, lng) => {
    if (marcadorRef.current) {
      marcadorRef.current.setLatLng([lat, lng]);
    } else {
      marcadorRef.current = L.marker([lat, lng]).addTo(mapa);
    }
  };

  // Geocodificación inversa — texto desde coordenadas
  const geocodificarInverso = async (lat, lng) => {
    try {
      const res = await fetch(
        `https://nominatim.openstreetmap.org/reverse?lat=${lat}&lon=${lng}&format=json&accept-language=es`,
        { headers: { "Accept-Language": "es" } }
      );
      const data = await res.json();
      return data.display_name || `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
    } catch {
      return `${lat.toFixed(5)}, ${lng.toFixed(5)}`;
    }
  };

  // Búsqueda por texto → geocodificación directa
  const buscarLugar = async (e) => {
    e.preventDefault();
    if (!busqueda.trim()) return;
    setErrorBusqueda("");
    setBuscando(true);

    try {
      const viewbox = "-77.1,8.9,-73.8,5.4"; // Left, Top, Right, Bottom
      const res = await fetch(
        `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(busqueda)}&format=json&limit=1&accept-language=es&viewbox=${viewbox}&bounded=1`,
        { headers: { "Accept-Language": "es" } }
      );
      const data = await res.json();

      if (!data.length) {
        setErrorBusqueda("No se encontró ninguna ubicación. Intenta ser más específico.");
        setBuscando(false);
        return;
      }

      const { lat, lon, display_name } = data[0];
      const latN = parseFloat(lat);
      const lngN = parseFloat(lon);

      import("leaflet").then((L) => {
        const mapa = instanciaRef.current;
        colocarMarcador(L.default, mapa, latN, lngN);
        mapa.setView([latN, lngN], ZOOM_SELECCION);
      });

      setDireccionActual(display_name);
      setLatActual(latN);
      setLngActual(lngN);
    } catch {
      setErrorBusqueda("Error al buscar. Verifica tu conexión.");
    } finally {
      setBuscando(false);
    }
  };

  const confirmar = () => {
    if (!latActual || !lngActual) return;
    onSeleccionar({ lat: latActual, lng: lngActual, direccion: direccionActual });
  };

  return (
    <div className="mapa-picker-overlay" onClick={onCerrar}>
      <div className="mapa-picker-modal" onClick={(e) => e.stopPropagation()}>
        <div className="mapa-picker-header">
          <div>
            <MapPin size={20} />
            <span>Seleccionar ubicación en el mapa</span>
          </div>
          <button type="button" className="mapa-close-btn" onClick={onCerrar}>
            <X size={20} />
          </button>
        </div>

        {/* Barra de búsqueda */}
        <form className="mapa-search-bar" onSubmit={buscarLugar}>
          <input
            type="text"
            placeholder="Buscar dirección, ciudad, lugar..."
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            className="mapa-search-input"
          />
          <button type="submit" className="mapa-search-btn" disabled={buscando}>
            <Search size={18} />
            {buscando ? "Buscando..." : "Buscar"}
          </button>
        </form>

        {errorBusqueda && <p className="mapa-error">{errorBusqueda}</p>}

        <p className="mapa-hint">
          💡 Haz clic en el mapa para marcar el punto exacto, o usa la búsqueda.
        </p>

        {/* Contenedor del mapa */}
        <div ref={mapaRef} className="mapa-container" />

        {/* Dirección seleccionada */}
        {direccionActual && (
          <div className="mapa-direccion-seleccionada">
            <MapPin size={16} />
            <span>{direccionActual}</span>
          </div>
        )}

        <div className="mapa-picker-actions">
          <button type="button" className="back-button" onClick={onCerrar}>
            Cancelar
          </button>
          <button
            type="button"
            className="auth-button"
            onClick={confirmar}
            disabled={!latActual}
          >
            <MapPin size={16} />
            Confirmar ubicación
          </button>
        </div>
      </div>
    </div>
  );
}

export default MapaPicker;
