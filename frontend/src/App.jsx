import { Routes, Route } from "react-router";
import { AuthProvider } from "./context/AuthContext";
import Header from "./components/layout/Header";
import Footer from "./components/layout/Footer";
import Home from "./pages/Home";
import Login from "./pages/auth/Login";
import Registro from "./pages/auth/Registro";
import RegistroEmpresa from "./pages/auth/RegistroEmpresa";
import RegistroUsuario from "./pages/auth/RegistroUsuario";
import Entrevistas from "./pages/entrevistas/Entrevistas";
import DetalleEntrevista from "./pages/entrevistas/DetalleEntrevista";
import FormularioEntrevista from "./pages/entrevistas/FormularioEntrevista";
import PerfilEmpresa from "./pages/perfil/PerfilEmpresa";
import PerfilUsuario from "./pages/perfil/PerfilUsuario";

import EntrevistasJpaCrud from "./pages/EntrevistasJpaCrud";

function App() {
  return (
    <AuthProvider>
      <Header />

      <main>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Registro />} />
          <Route path="/registro/empresa" element={<RegistroEmpresa />} />
          <Route path="/registro/usuario" element={<RegistroUsuario />} />
          <Route path="/entrevistas" element={<Entrevistas />} />
          <Route path="/perfil/empresa/entrevistas/nueva" element={<FormularioEntrevista />} />
          <Route path="/perfil/empresa/entrevistas/editar/:id" element={<FormularioEntrevista />} />
          <Route path="/entrevistas/:id" element={<DetalleEntrevista />} />
          <Route path="/perfil/empresa" element={<PerfilEmpresa />} />
          <Route path="/perfil/usuario" element={<PerfilUsuario />} />
          <Route path="/reto-jpa" element={<EntrevistasJpaCrud />} />
        </Routes>
      </main>

      <Footer />
    </AuthProvider>
  );
}

export default App;