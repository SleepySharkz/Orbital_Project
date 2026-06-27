import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { fetchModules } from "../../modules/api/moduleApi";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import { fetchTCById, fetchTCsForModule, type TcContentResponse, type TcSummary } from "../api/tcApi";
import { TCDetailHeader } from "../components/TCDetailHeader";
import { TCEntryList } from "../components/TCEntryList";
import { TCSummaryList } from "../components/TCSummaryList";
import "../styles/tcStyles.css";

function compareUpdatedAtDescending(left: TcSummary, right: TcSummary) {
  return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime();
}

export function TCListPage() {
  const navigate = useNavigate();
  const { tcId } = useParams();
  const { user, token, logout } = useAuth();
  const parsedTcId = useMemo(() => {
    if (!tcId) {
      return null;
    }

    const value = Number(tcId);
    return Number.isInteger(value) && value > 0 ? value : null;
  }, [tcId]);

  const [tcs, setTcs] = useState<TcSummary[]>([]);
  const [selectedTc, setSelectedTc] = useState<TcContentResponse | null>(null);
  const [isListLoading, setIsListLoading] = useState(true);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [listError, setListError] = useState("");
  const [detailError, setDetailError] = useState("");

  useEffect(() => {
    async function loadTopicSheets() {
      if (!token) {
        setTcs([]);
        setIsListLoading(false);
        return;
      }

      try {
        setListError("");
        setIsListLoading(true);

        const modules = await fetchModules(token);
        const tcGroups = await Promise.all(
          modules.map((module) => fetchTCsForModule(module.id, token)),
        );

        setTcs(tcGroups.flat().sort(compareUpdatedAtDescending));
      } catch (caughtError) {
        setTcs([]);
        setListError(caughtError instanceof Error ? caughtError.message : "Could not load topic sheets.");
      } finally {
        setIsListLoading(false);
      }
    }

    void loadTopicSheets();
  }, [token]);

  useEffect(() => {
    async function loadSelectedTopicSheet() {
      if (!token) {
        setSelectedTc(null);
        setIsDetailLoading(false);
        return;
      }

      if (parsedTcId === null) {
        setSelectedTc(null);
        setDetailError(tcId ? "Invalid topic sheet id." : "");
        setIsDetailLoading(false);
        return;
      }

      try {
        setDetailError("");
        setIsDetailLoading(true);
        const fetchedTc = await fetchTCById(parsedTcId, token);
        setSelectedTc(fetchedTc);
      } catch (caughtError) {
        setSelectedTc(null);
        setDetailError(caughtError instanceof Error ? caughtError.message : "Could not load topic sheet.");
      } finally {
        setIsDetailLoading(false);
      }
    }

    void loadSelectedTopicSheet();
  }, [parsedTcId, tcId, token]);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  function handleOpenTc(nextTcId: number) {
    navigate(`/topic-sheets/${nextTcId}`);
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="tc-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="tc-main">
        <header className="tc-header">
          <h1 className="tc-title">Topical Cheatsheets</h1>
          <p className="tc-subtitle">
            Select a cheatsheet to open it.
          </p>
        </header>

        <div className="tc-workspace">
          <aside className="tc-list-column">
            <TCSummaryList
              tcs={tcs}
              isLoading={isListLoading}
              error={listError}
              selectedTcId={parsedTcId}
              onOpenTc={handleOpenTc}
              currentUsername={user.username}
            />
          </aside>
        </div>

        {(isDetailLoading || detailError || selectedTc) && (
          <div
            aria-hidden="true"
            className="tc-overlay-backdrop"
            onClick={() => navigate("/topic-sheets")}
          />
        )}

        {(isDetailLoading || detailError || selectedTc) && (
          <section
            aria-label="Cheatsheet overlay"
            className="tc-overlay"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="tc-overlay-header">
              <button
                className="tc-overlay-close"
                onClick={() => navigate("/topic-sheets")}
                type="button"
              >
                Close
              </button>
            </div>

            {isDetailLoading && (
              <div className="tc-panel">
                <p className="tc-helper-copy">Loading topic sheet...</p>
              </div>
            )}

            {!isDetailLoading && detailError && (
              <div className="tc-panel">
                <p className="tc-banner tc-banner-error">{detailError}</p>
              </div>
            )}

            {!isDetailLoading && !detailError && selectedTc && (
              <div className="tc-detail-content">
                <TCDetailHeader tc={selectedTc} />
                <TCEntryList entries={selectedTc.entries} />
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}
