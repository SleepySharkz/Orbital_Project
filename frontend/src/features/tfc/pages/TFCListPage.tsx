import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { fetchModules } from "../../modules/api/moduleApi";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import { fetchTFCById, fetchTFCsForModule, type TfcContentResponse, type TfcSummary } from "../api/tfcApi";
import { TFCDetailHeader } from "../components/TFCDetailHeader";
import { TFCEntryList } from "../components/TFCEntryList";
import { TFCSummaryList } from "../components/TFCSummaryList";
import "../styles/tfcStyles.css";

function compareUpdatedAtDescending(left: TfcSummary, right: TfcSummary) {
  return new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime();
}

export function TFCListPage() {
  const navigate = useNavigate();
  const { tfcId } = useParams();
  const { user, token, logout } = useAuth();
  const parsedTfcId = useMemo(() => {
    if (!tfcId) {
      return null;
    }

    const value = Number(tfcId);
    return Number.isInteger(value) && value > 0 ? value : null;
  }, [tfcId]);

  const [tfcs, setTfcs] = useState<TfcSummary[]>([]);
  const [selectedTfc, setSelectedTfc] = useState<TfcContentResponse | null>(null);
  const [isListLoading, setIsListLoading] = useState(true);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [listError, setListError] = useState("");
  const [detailError, setDetailError] = useState("");

  useEffect(() => {
    async function loadTopicSheets() {
      if (!token) {
        setTfcs([]);
        setIsListLoading(false);
        return;
      }

      try {
        setListError("");
        setIsListLoading(true);

        const modules = await fetchModules(token);
        const tfcGroups = await Promise.all(
          modules.map((module) => fetchTFCsForModule(module.id, token)),
        );

        setTfcs(tfcGroups.flat().sort(compareUpdatedAtDescending));
      } catch (caughtError) {
        setTfcs([]);
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
        setSelectedTfc(null);
        setIsDetailLoading(false);
        return;
      }

      if (parsedTfcId === null) {
        setSelectedTfc(null);
        setDetailError(tfcId ? "Invalid topic sheet id." : "");
        setIsDetailLoading(false);
        return;
      }

      try {
        setDetailError("");
        setIsDetailLoading(true);
        const fetchedTfc = await fetchTFCById(parsedTfcId, token);
        setSelectedTfc(fetchedTfc);
      } catch (caughtError) {
        setSelectedTfc(null);
        setDetailError(caughtError instanceof Error ? caughtError.message : "Could not load topic sheet.");
      } finally {
        setIsDetailLoading(false);
      }
    }

    void loadSelectedTopicSheet();
  }, [parsedTfcId, tfcId, token]);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  function handleOpenTfc(nextTfcId: number) {
    navigate(`/topic-sheets/${nextTfcId}`);
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="tfc-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="tfc-main">
        <header className="tfc-header">
          <p className="tfc-eyebrow">Topic Sheets</p>
          <h1 className="tfc-title">Topical Flashcards</h1>
          <p className="tfc-subtitle">
            Browse topic-level study sheets generated from linked coursework flashcard entries.
          </p>
        </header>

        <div className="tfc-workspace">
          <aside className="tfc-list-column">
            <div className="tfc-list-column-header">
              <p className="tfc-list-label">All Topic Sheets</p>
              <p className="tfc-list-copy">
                Keep a quick reference list on the left and open the full sheet on the right.
              </p>
            </div>

            <TFCSummaryList
              tfcs={tfcs}
              isLoading={isListLoading}
              error={listError}
              selectedTfcId={parsedTfcId}
              onOpenTfc={handleOpenTfc}
            />
          </aside>

          <section className="tfc-detail-column">
            {isDetailLoading && (
              <div className="tfc-panel">
                <p className="tfc-helper-copy">Loading topic sheet...</p>
              </div>
            )}

            {!isDetailLoading && detailError && (
              <div className="tfc-panel">
                <p className="tfc-banner tfc-banner-error">{detailError}</p>
              </div>
            )}

            {!isDetailLoading && !detailError && !selectedTfc && (
              <div className="tfc-empty-state">
                <p className="tfc-eyebrow">Select A Sheet</p>
                <h2 className="tfc-empty-title">Open a topic from the left column.</h2>
                <p className="tfc-subtitle">
                  The right side becomes your study surface, with linked entries presented as one
                  continuous topic notebook.
                </p>
              </div>
            )}

            {!isDetailLoading && !detailError && selectedTfc && (
              <div className="tfc-detail-content">
                <TFCDetailHeader tfc={selectedTfc} />
                <TFCEntryList entries={selectedTfc.entries} />
              </div>
            )}
          </section>
        </div>
      </main>
    </div>
  );
}
