import { useEffect, useRef } from "react";
import "leaflet/dist/leaflet.css";

/**
 * MapaVista — Mapa de solo lectura para que el CANDIDATO vea la ubicación.
 *
 * Props:
 *   latitud   {number}
 *   longitud  {number}
 *   titulo    {string}  — texto del popup del marcador
 */
function MapaVista({ latitud, longitud, titulo }) {
  const mapaRef = useRef(null);
  const instanciaRef = useRef(null);

  useEffect(() => {
    if (!latitud || !longitud) return;
    if (instanciaRef.current) return;

    import("leaflet").then((L) => {
      // Fix de íconos en Vite
      delete L.default.Icon.Default.prototype._getIconUrl;
      L.default.Icon.Default.mergeOptions({
        iconUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png",
        iconRetinaUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png",
        shadowUrl: "https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png",
      });

      const mapa = L.default.map(mapaRef.current, {
        zoomControl: true,
        dragging: true,
        scrollWheelZoom: false, // evitar scroll accidental
      }).setView([latitud, longitud], 15);

      instanciaRef.current = mapa;

      L.default.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
        attribution: '© <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
        maxZoom: 19,
      }).addTo(mapa);

      L.default.marker([latitud, longitud])
        .addTo(mapa)
        .bindPopup(`<strong>${titulo || "Lugar de entrevista"}</strong>`)
        .openPopup();
    });

    return () => {
      if (instanciaRef.current) {
        instanciaRef.current.remove();
        instanciaRef.current = null;
      }
    };
  }, [latitud, longitud]);

  if (!latitud || !longitud) return null;

  return (
    <div className="mapa-vista-wrapper">
      <div ref={mapaRef} className="mapa-vista-container" />
      <p className="mapa-vista-footer">
        📍 Mapa interactivo — OpenStreetMap
      </p>
    </div>
  );
}

export default MapaVista;
