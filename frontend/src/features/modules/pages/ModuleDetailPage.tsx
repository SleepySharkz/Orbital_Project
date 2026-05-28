import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { fetchCFCsForModule, type CFCSummary } from "../../cfc/api/cfcApi";
import { fetchModuleById, type ModuleResponse } from "../api/moduleApi";
import { CFCSummaryList } from "../components/CFCSummaryList";
import { ModuleDetailHeader } from "../components/ModuleDetailHeader";
import { ModuleTopicsPanel } from "../components/ModuleTopicsPanel";
import { ModulesSidebar } from "../components/ModulesSidebar";
import "../styles/modulesStyles.css";

export function ModuleDetailPage() {
  const navigate = useNavigate();
  const { moduleId } = useParams();
  const { user, token, logout } = useAuth();
  const parsedModuleId = useMemo(() => Number(moduleId), [moduleId]);
  const isValidModuleId = Number.isInteger(parsedModuleId) && parsedModuleId > 0;

  const [module, setModule] = useState<ModuleResponse | null>(null);
  const [cfcs, setCfcs] = useState<CFCSummary[]>([]);
  const [isModuleLoading, setIsModuleLoading] = useState(true);
  const [isCFCsLoading, setIsCFCsLoading] = useState(true);
  const [moduleError, setModuleError] = useState("");
  const [cfcsError, setCfcsError] = useState("");

  useEffect(() => {
    async function loadModuleDetail() {
      if (!token) {
        setModule(null);
        setCfcs([]);
        setIsModuleLoading(false);
        setIsCFCsLoading(false);
        return;
      }

      if (!isValidModuleId) {
        setModule(null);
        setCfcs([]);
        setModuleError("Invalid module id.");
        setCfcsError("");
        setIsModuleLoading(false);
        setIsCFCsLoading(false);
        return;
      }

      setModuleError("");
      setCfcsError("");
      setIsModuleLoading(true);
      setIsCFCsLoading(true);

      try {
        const fetchedModule = await fetchModuleById(parsedModuleId, token);
        setModule(fetchedModule);
      } catch (caughtError) {
        setModule(null);
        setModuleError(
          caughtError instanceof Error ? caughtError.message : "Could not load module.",
        );
      } finally {
        setIsModuleLoading(false);
      }

      try {
        const fetchedCFCs = await fetchCFCsForModule(parsedModuleId, token);
        setCfcs(fetchedCFCs);
      } catch (caughtError) {
        setCfcs([]);
        setCfcsError(
          caughtError instanceof Error ? caughtError.message : "Could not load saved flashcards.",
        );
      } finally {
        setIsCFCsLoading(false);
      }
    }

    void loadModuleDetail();
  }, [isValidModuleId, parsedModuleId, token]);

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
        <header className="modules-header modules-detail-page-header">
          <div>
            <p className="modules-eyebrow">Module Detail</p>
            <h1 className="modules-title">
              {module ? module.courseCode : "Module Overview"}
            </h1>
            <p className="modules-subtitle">
              Review saved topics and open generated coursework flashcards for this module.
            </p>
          </div>

          <Link className="modules-secondary-link" to="/modules">
            Back to Modules
          </Link>
        </header>

        {isModuleLoading && (
          <section className="modules-panel">
            <p className="modules-body-copy">Loading module...</p>
          </section>
        )}

        {!isModuleLoading && moduleError && (
          <section className="modules-panel">
            <p className="modules-error-message">{moduleError}</p>
          </section>
        )}

        {!isModuleLoading && !moduleError && module && (
          <div className="modules-detail-content">
            <ModuleDetailHeader module={module} />

            <div className="modules-detail-grid">
              <ModuleTopicsPanel topics={module.topics} />
              <CFCSummaryList cfcs={cfcs} isLoading={isCFCsLoading} error={cfcsError} />
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
