import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import {
  fetchCFCById,
  updateCFCEntryContent,
  updateCFCSummary,
  type CFCContent,
  type CFCResponse,
} from "../api/cfcApi";
import { CFCDetailHeader } from "../components/CFCDetailHeader";
import { CFCEntryList } from "../components/CFCEntryList";
import "../styles/cfcStyles.css";

export function CFCDetailPage() {
  const navigate = useNavigate();
  const { cfcId } = useParams();
  const { user, token, logout } = useAuth();
  const parsedCFCId = useMemo(() => Number(cfcId), [cfcId]);
  const isValidCFCId = Number.isInteger(parsedCFCId) && parsedCFCId > 0;

  const [cfc, setCFC] = useState<CFCResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadCFCDetail() {
      if (!token) {
        setCFC(null);
        setIsLoading(false);
        return;
      }

      if (!isValidCFCId) {
        setCFC(null);
        setError("Invalid CFC id.");
        setIsLoading(false);
        return;
      }

      try {
        setError("");
        setIsLoading(true);
        const fetchedCFC = await fetchCFCById(parsedCFCId, token);
        setCFC(fetchedCFC);
      } catch (caughtError) {
        setCFC(null);
        setError(
          caughtError instanceof Error ? caughtError.message : "Could not load saved flashcard.",
        );
      } finally {
        setIsLoading(false);
      }
    }

    void loadCFCDetail();
  }, [isValidCFCId, parsedCFCId, token]);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  async function handleSummarySave(summary: string) {
    if (!token || !cfc) {
      return;
    }

    const updatedCFC = await updateCFCSummary(cfc.id, { summary }, token);
    setCFC(updatedCFC);
  }

  async function handleEntryContentSave(entryId: number, content: CFCContent) {
    if (!token || !cfc) {
      return;
    }

    const updatedEntry = await updateCFCEntryContent(cfc.id, entryId, content, token);

    setCFC((currentCFC) => {
      if (!currentCFC) {
        return currentCFC;
      }

      return {
        ...currentCFC,
        entries: currentCFC.entries.map((entry) =>
          entry.id === updatedEntry.id ? updatedEntry : entry,
        ),
      };
    });
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="cfc-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="cfc-main">
        {isLoading && (
          <section className="cfc-panel">
            <p className="cfc-helper-copy">Loading saved flashcard...</p>
          </section>
        )}

        {!isLoading && error && (
          <section className="cfc-panel">
            <p className="cfc-banner cfc-banner-error">{error}</p>
          </section>
        )}

        {!isLoading && !error && cfc && (
          <div className="cfc-detail-content">
            <CFCDetailHeader cfc={cfc} onSummarySave={handleSummarySave} />
            <CFCEntryList
              entries={cfc.entries}
              onEntryContentSave={handleEntryContentSave}
            />
          </div>
        )}
      </main>
    </div>
  );
}
