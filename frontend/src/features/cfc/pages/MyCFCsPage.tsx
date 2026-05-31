import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import { fetchModules } from "../../modules/api/moduleApi";
import { fetchCFCById, fetchCFCsForModule, type CFCResponse } from "../api/cfcApi";
import { CFCCollectionControls } from "../components/CFCCollectionControls";
import { CFCCollectionList } from "../components/CFCCollectionList";
import type { CFCCollectionItem, CFCCollectionViewMode } from "../components/cfcCollectionTypes";
import "../styles/cfcStyles.css";

function getUniqueTopics(cfc: CFCResponse) {
  return Array.from(
    new Set(
      cfc.entries
        .map((entry) => entry.topic.trim())
        .filter((topic) => topic.length > 0),
    ),
  );
}

export function MyCFCsPage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [items, setItems] = useState<CFCCollectionItem[]>([]);
  const [viewMode, setViewMode] = useState<CFCCollectionViewMode>("date");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadAllCFCs() {
      if (!token) {
        setItems([]);
        setIsLoading(false);
        return;
      }

      try {
        setError("");
        setIsLoading(true);

        const modules = await fetchModules(token);
        const summaryGroups = await Promise.all(
          modules.map((module) => fetchCFCsForModule(module.id, token)),
        );
        const summaries = summaryGroups.flat();

        const collectionItems = await Promise.all(
          summaries.map(async (summary) => {
            const detail = await fetchCFCById(summary.id, token);

            return {
              summary,
              topics: getUniqueTopics(detail),
              entryCount: detail.entries.length,
            };
          }),
        );

        setItems(collectionItems);
      } catch (caughtError) {
        setItems([]);
        setError(caughtError instanceof Error ? caughtError.message : "Could not load saved CFCs.");
      } finally {
        setIsLoading(false);
      }
    }

    void loadAllCFCs();
  }, [token]);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="cfc-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="cfc-main">
        <header className="cfc-collection-header">
          <div>
            <p className="cfc-eyebrow">My CFCs</p>
            <h1 className="cfc-title">Saved Coursework Flashcards</h1>
            <p className="cfc-subtitle">
              Review every saved CFC across your modules.
            </p>
          </div>

          <CFCCollectionControls viewMode={viewMode} onViewModeChange={setViewMode} />
        </header>

        {isLoading && (
          <section className="cfc-panel">
            <p className="cfc-helper-copy">Loading saved CFCs...</p>
          </section>
        )}

        {!isLoading && error && (
          <section className="cfc-panel">
            <p className="cfc-banner cfc-banner-error">{error}</p>
          </section>
        )}

        {!isLoading && !error && (
          <CFCCollectionList items={items} viewMode={viewMode} />
        )}
      </main>
    </div>
  );
}
