import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchModules, type ModuleSummary } from "../api/moduleApi";
import { useAuth } from "../../auth/context/AuthContext";
import { CreateModuleForm } from "../components/CreateModuleForm";
import { ModulesList } from "../components/ModulesList";
import { ModulesSidebar } from "../components/ModulesSidebar";
import "../styles/modulesStyles.css";

export function ModulesPage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [modules, setModules] = useState<ModuleSummary[]>([]);
  const [isModulesLoading, setIsModulesLoading] = useState(true);
  const [modulesError, setModulesError] = useState("");

  useEffect(() => {
    async function loadModules() {
      await loadModulesPageData();
    }

    void loadModules();
  }, [token]);

  async function loadModulesPageData() {
    if (!token) {
      setModules([]);
      setIsModulesLoading(false);
      return;
    }

    try {
      setModulesError("");
      setIsModulesLoading(true);
      const fetchedModules = await fetchModules(token);
      setModules(fetchedModules);
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setModulesError(caughtError.message);
      } else {
        setModulesError("Could not load modules.");
      }
    } finally {
      setIsModulesLoading(false);
    }
  }

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="modules-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="modules-main">
        <header className="modules-header">
          <div>
            <p className="modules-eyebrow">Modules</p>
            <h1 className="modules-title">Manage Your Modules</h1>
            <p className="modules-subtitle">Create modules and manage their topic lists.</p>
          </div>
        </header>

        <div className="modules-content">
          <CreateModuleForm token={token} onModuleCreated={loadModulesPageData} />
          <ModulesList
            modules={modules}
            token={token}
            isModulesLoading={isModulesLoading}
            modulesError={modulesError}
            onModuleUpdated={loadModulesPageData}
          />
        </div>
      </main>
    </div>);
}
