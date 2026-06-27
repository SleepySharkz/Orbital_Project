import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import {
  fetchSharedTCById,
  fetchSharedTCs,
} from "../api/tcSharingApi";
import type {
  SharedTCDetail,
  SharedTCSummary,
} from "../types/tcSharingTypes";
import "../../modules/styles/modulesStyles.css";
import "../../tc/styles/tcStyles.css";
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

export function SharedTCListPage() {
  const navigate = useNavigate();
  const { sharedTcId } = useParams();
  const { user, token, logout } = useAuth();
  const parsedSharedTcId = useMemo(() => {
    if (!sharedTcId) {
      return null;
    }

    const value = Number(sharedTcId);
    return Number.isInteger(value) && value > 0 ? value : null;
  }, [sharedTcId]);

  const [sharedTcs, setSharedTcs] = useState<SharedTCSummary[]>([]);
  const [selectedSharedTc, setSelectedSharedTc] =
    useState<SharedTCDetail | null>(null);
  const [isListLoading, setIsListLoading] = useState(true);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [listError, setListError] = useState("");
  const [detailError, setDetailError] = useState("");

  useEffect(() => {
    async function loadSharedTcs() {
      if (!token) {
        setSharedTcs([]);
        setIsListLoading(false);
        return;
      }

      try {
        setListError("");
        setIsListLoading(true);
        setSharedTcs(await fetchSharedTCs(token));
      } catch (caughtError) {
        setSharedTcs([]);
        setListError(
          caughtError instanceof Error
            ? caughtError.message
            : "Could not load shared TCs.",
        );
      } finally {
        setIsListLoading(false);
      }
    }

    void loadSharedTcs();
  }, [token]);

  useEffect(() => {
    async function loadSelectedSharedTc() {
      if (!token) {
        setSelectedSharedTc(null);
        setIsDetailLoading(false);
        return;
      }

      if (parsedSharedTcId === null) {
        setSelectedSharedTc(null);
        setDetailError(sharedTcId ? "Invalid shared TC id." : "");
        setIsDetailLoading(false);
        return;
      }

      try {
        setDetailError("");
        setIsDetailLoading(true);
        setSelectedSharedTc(await fetchSharedTCById(parsedSharedTcId, token));
      } catch (caughtError) {
        setSelectedSharedTc(null);
        setDetailError(
          caughtError instanceof Error
            ? caughtError.message
            : "Could not load shared TC.",
        );
      } finally {
        setIsDetailLoading(false);
      }
    }

    void loadSelectedSharedTc();
  }, [parsedSharedTcId, sharedTcId, token]);

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        navigate("/shared-tcs");
      }
    }

    if (isDetailLoading || detailError || selectedSharedTc) {
      window.addEventListener("keydown", handleKeyDown);
    }

    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [detailError, isDetailLoading, navigate, selectedSharedTc]);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="tc-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="tc-main">
        <header className="tc-header">
          <h1 className="tc-title">Shared TCs</h1>
          <p className="tc-subtitle">
            Accepted topic sheets shared privately with you.
          </p>
        </header>

        <section className="tc-section">
          {isListLoading && (
            <div className="tc-panel">
              <p className="tc-helper-copy">Loading shared TCs...</p>
            </div>
          )}

          {!isListLoading && listError && (
            <div className="tc-panel">
              <p className="tc-banner tc-banner-error">{listError}</p>
            </div>
          )}

          {!isListLoading && !listError && sharedTcs.length === 0 && (
            <div className="tc-panel">
              <p className="tc-helper-copy">
                No shared TCs yet. Accept a compatible sharing request first.
              </p>
            </div>
          )}

          {!isListLoading && !listError && sharedTcs.length > 0 && (
            <div className="tc-card-list">
              {sharedTcs.map((sharedTc) => (
                <button
                  className={
                    parsedSharedTcId === sharedTc.id
                      ? "tc-card tc-card-selected"
                      : "tc-card"
                  }
                  key={sharedTc.id}
                  type="button"
                  onClick={() => navigate(`/shared-tcs/${sharedTc.id}`)}
                >
                  <span className="tc-card-main">
                    <span className="tc-card-kicker">
                      {sharedTc.courseCode} - {sharedTc.schoolSem}
                    </span>
                    <span className="tc-card-title">{sharedTc.topic}</span>
                    <span className="tc-card-meta">
                      Shared by {sharedTc.sharedByUsername} -{" "}
                      {sharedTc.entryCount}{" "}
                      {sharedTc.entryCount === 1 ? "entry" : "entries"} -
                      Accepted {formatDateTime(sharedTc.acceptedAt)}
                    </span>
                  </span>
                  <span className="tc-open-link">Open</span>
                </button>
              ))}
            </div>
          )}
        </section>

        {(isDetailLoading || detailError || selectedSharedTc) && (
          <div
            aria-hidden="true"
            className="tc-overlay-backdrop"
            onClick={() => navigate("/shared-tcs")}
          />
        )}

        {(isDetailLoading || detailError || selectedSharedTc) && (
          <section
            aria-label="Shared TC overlay"
            className="tc-overlay"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="tc-overlay-header">
              <button
                className="tc-overlay-close"
                onClick={() => navigate("/shared-tcs")}
                type="button"
              >
                Close
              </button>
            </div>

            {isDetailLoading && (
              <div className="tc-panel">
                <p className="tc-helper-copy">Loading shared TC...</p>
              </div>
            )}

            {!isDetailLoading && detailError && (
              <div className="tc-panel">
                <p className="tc-banner tc-banner-error">{detailError}</p>
              </div>
            )}

            {!isDetailLoading && !detailError && selectedSharedTc && (
              <div className="tc-detail-content">
                <header className="tc-detail-header">
                  <p className="tc-eyebrow">
                    Shared by {selectedSharedTc.sharedByUsername}
                  </p>
                  <h1 className="tc-detail-title">
                    {selectedSharedTc.topic}
                  </h1>
                  <p className="tc-subtitle">
                    {selectedSharedTc.courseCode} -{" "}
                    {selectedSharedTc.schoolSem} - Accepted{" "}
                    {formatDateTime(selectedSharedTc.acceptedAt)}
                  </p>
                </header>

                <section className="tc-sheet">
                  {selectedSharedTc.entries.map((entry) => (
                    <article className="tc-sheet-entry" key={entry.id}>
                      <div className="tc-entry-header">
                        <h2 className="tc-entry-question">
                          {entry.flashcardQuestion}
                        </h2>
                      </div>

                      <div className="tc-note-block">
                        {entry.flashcardNoteContent
                          .split("\n")
                          .map((line, lineIndex) => (
                            <p
                              className="tc-note-line"
                              key={`${entry.id}-${lineIndex}`}
                            >
                              {line}
                            </p>
                          ))}
                      </div>

                      <details className="tc-source-details">
                        <summary className="tc-source-summary">
                          Show original source material
                        </summary>

                        <div className="tc-source-grid">
                          <div className="tc-source-card">
                            <p className="tc-source-label">
                              Original Question
                            </p>
                            <p className="tc-source-copy">
                              {entry.questionText &&
                              entry.questionText.trim().length > 0
                                ? entry.questionText
                                : "No original question text was provided for this entry."}
                            </p>
                          </div>

                          <div className="tc-source-card">
                            <p className="tc-source-label">
                              Original Rough Note
                            </p>
                            <p className="tc-source-copy">
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
