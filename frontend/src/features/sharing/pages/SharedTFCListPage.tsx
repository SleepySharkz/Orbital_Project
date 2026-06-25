import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import {
  fetchSharedTFCById,
  fetchSharedTFCs,
} from "../api/tfcSharingApi";
import type {
  SharedTFCDetail,
  SharedTFCSummary,
} from "../types/tfcSharingTypes";
import "../../modules/styles/modulesStyles.css";
import "../../tfc/styles/tfcStyles.css";
import "../styles/sharingStyles.css";

function formatDateTime(value: string | null) {
  if (!value) {
    return "Not available";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function SharedTFCListPage() {
  const navigate = useNavigate();
  const { sharedTfcId } = useParams();
  const { user, token, logout } = useAuth();
  const parsedSharedTfcId = useMemo(() => {
    if (!sharedTfcId) {
      return null;
    }

    const value = Number(sharedTfcId);
    return Number.isInteger(value) && value > 0 ? value : null;
  }, [sharedTfcId]);

  const [sharedTfcs, setSharedTfcs] = useState<SharedTFCSummary[]>([]);
  const [selectedSharedTfc, setSelectedSharedTfc] =
    useState<SharedTFCDetail | null>(null);
  const [isListLoading, setIsListLoading] = useState(true);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [listError, setListError] = useState("");
  const [detailError, setDetailError] = useState("");

  useEffect(() => {
    async function loadSharedTfcs() {
      if (!token) {
        setSharedTfcs([]);
        setIsListLoading(false);
        return;
      }

      try {
        setListError("");
        setIsListLoading(true);
        setSharedTfcs(await fetchSharedTFCs(token));
      } catch (caughtError) {
        setSharedTfcs([]);
        setListError(
          caughtError instanceof Error
            ? caughtError.message
            : "Could not load shared TFCs.",
        );
      } finally {
        setIsListLoading(false);
      }
    }

    void loadSharedTfcs();
  }, [token]);

  useEffect(() => {
    async function loadSelectedSharedTfc() {
      if (!token) {
        setSelectedSharedTfc(null);
        setIsDetailLoading(false);
        return;
      }

      if (parsedSharedTfcId === null) {
        setSelectedSharedTfc(null);
        setDetailError(sharedTfcId ? "Invalid shared TFC id." : "");
        setIsDetailLoading(false);
        return;
      }

      try {
        setDetailError("");
        setIsDetailLoading(true);
        setSelectedSharedTfc(await fetchSharedTFCById(parsedSharedTfcId, token));
      } catch (caughtError) {
        setSelectedSharedTfc(null);
        setDetailError(
          caughtError instanceof Error
            ? caughtError.message
            : "Could not load shared TFC.",
        );
      } finally {
        setIsDetailLoading(false);
      }
    }

    void loadSelectedSharedTfc();
  }, [parsedSharedTfcId, sharedTfcId, token]);

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        navigate("/shared-tfcs");
      }
    }

    if (isDetailLoading || detailError || selectedSharedTfc) {
      window.addEventListener("keydown", handleKeyDown);
    }

    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [detailError, isDetailLoading, navigate, selectedSharedTfc]);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="tfc-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="tfc-main">
        <header className="tfc-header">
          <h1 className="tfc-title">Shared TFCs</h1>
          <p className="tfc-subtitle">
            Accepted topic sheets shared privately with you.
          </p>
        </header>

        <section className="tfc-section">
          {isListLoading && (
            <div className="tfc-panel">
              <p className="tfc-helper-copy">Loading shared TFCs...</p>
            </div>
          )}

          {!isListLoading && listError && (
            <div className="tfc-panel">
              <p className="tfc-banner tfc-banner-error">{listError}</p>
            </div>
          )}

          {!isListLoading && !listError && sharedTfcs.length === 0 && (
            <div className="tfc-panel">
              <p className="tfc-helper-copy">
                No shared TFCs yet. Accept a compatible sharing request first.
              </p>
            </div>
          )}

          {!isListLoading && !listError && sharedTfcs.length > 0 && (
            <div className="tfc-card-list">
              {sharedTfcs.map((sharedTfc) => (
                <button
                  className={
                    parsedSharedTfcId === sharedTfc.id
                      ? "tfc-card tfc-card-selected"
                      : "tfc-card"
                  }
                  key={sharedTfc.id}
                  type="button"
                  onClick={() => navigate(`/shared-tfcs/${sharedTfc.id}`)}
                >
                  <span className="tfc-card-main">
                    <span className="tfc-card-kicker">
                      {sharedTfc.courseCode} - {sharedTfc.schoolSem}
                    </span>
                    <span className="tfc-card-title">{sharedTfc.topic}</span>
                    <span className="tfc-card-meta">
                      Shared by {sharedTfc.sharedByUsername} -{" "}
                      {sharedTfc.entryCount}{" "}
                      {sharedTfc.entryCount === 1 ? "entry" : "entries"} -
                      Accepted {formatDateTime(sharedTfc.acceptedAt)}
                    </span>
                  </span>
                  <span className="tfc-open-link">Open</span>
                </button>
              ))}
            </div>
          )}
        </section>

        {(isDetailLoading || detailError || selectedSharedTfc) && (
          <div
            aria-hidden="true"
            className="tfc-overlay-backdrop"
            onClick={() => navigate("/shared-tfcs")}
          />
        )}

        {(isDetailLoading || detailError || selectedSharedTfc) && (
          <section
            aria-label="Shared TFC overlay"
            className="tfc-overlay"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="tfc-overlay-header">
              <button
                className="tfc-overlay-close"
                onClick={() => navigate("/shared-tfcs")}
                type="button"
              >
                Close
              </button>
            </div>

            {isDetailLoading && (
              <div className="tfc-panel">
                <p className="tfc-helper-copy">Loading shared TFC...</p>
              </div>
            )}

            {!isDetailLoading && detailError && (
              <div className="tfc-panel">
                <p className="tfc-banner tfc-banner-error">{detailError}</p>
              </div>
            )}

            {!isDetailLoading && !detailError && selectedSharedTfc && (
              <div className="tfc-detail-content">
                <header className="tfc-detail-header">
                  <p className="tfc-eyebrow">
                    Shared by {selectedSharedTfc.sharedByUsername}
                  </p>
                  <h1 className="tfc-detail-title">
                    {selectedSharedTfc.topic}
                  </h1>
                  <p className="tfc-subtitle">
                    {selectedSharedTfc.courseCode} -{" "}
                    {selectedSharedTfc.schoolSem} - Accepted{" "}
                    {formatDateTime(selectedSharedTfc.acceptedAt)}
                  </p>
                </header>

                <section className="tfc-sheet">
                  {selectedSharedTfc.entries.map((entry) => (
                    <article className="tfc-sheet-entry" key={entry.id}>
                      <div className="tfc-entry-header">
                        <h2 className="tfc-entry-question">
                          {entry.flashcardQuestion}
                        </h2>
                      </div>

                      <div className="tfc-note-block">
                        {entry.flashcardNoteContent
                          .split("\n")
                          .map((line, lineIndex) => (
                            <p
                              className="tfc-note-line"
                              key={`${entry.id}-${lineIndex}`}
                            >
                              {line}
                            </p>
                          ))}
                      </div>

                      <details className="tfc-source-details">
                        <summary className="tfc-source-summary">
                          Show original source material
                        </summary>

                        <div className="tfc-source-grid">
                          <div className="tfc-source-card">
                            <p className="tfc-source-label">
                              Original Question
                            </p>
                            <p className="tfc-source-copy">
                              {entry.questionText &&
                              entry.questionText.trim().length > 0
                                ? entry.questionText
                                : "No original question text was provided for this entry."}
                            </p>
                          </div>

                          <div className="tfc-source-card">
                            <p className="tfc-source-label">
                              Original Rough Note
                            </p>
                            <p className="tfc-source-copy">
                              {entry.roughNote}
                            </p>
                          </div>
                        </div>
                      </details>
                    </article>
                  ))}
                </section>
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}
