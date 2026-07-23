import { useEffect } from "react";
import type { ReactNode } from "react";

type ReceivedTCDetailEntry = {
  id: number;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  questionText?: string | null;
  roughNote?: string | null;
};

type ReceivedTCDetailOverlayProps = {
  isOpen: boolean;
  isLoading: boolean;
  error: string;
  title?: string;
  subtitle?: string;
  entries?: ReceivedTCDetailEntry[];
  actions?: ReactNode;
  showSourceMaterial?: boolean;
  loadingMessage: string;
  ariaLabel: string;
  onClose: () => void;
};

export function ReceivedTCDetailOverlay({
  isOpen,
  isLoading,
  error,
  title,
  subtitle,
  entries = [],
  actions,
  showSourceMaterial = false,
  loadingMessage,
  ariaLabel,
  onClose,
}: ReceivedTCDetailOverlayProps) {
  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose();
      }
    }

    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) {
    return null;
  }

  return (
    <>
      <div
        aria-hidden="true"
        className="tc-overlay-backdrop"
        onClick={onClose}
      />

      <section
        aria-label={ariaLabel}
        className="tc-overlay"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="tc-overlay-header">
          <button className="tc-overlay-close" onClick={onClose} type="button">
            Close
          </button>
        </div>

        {isLoading && (
          <div className="tc-panel">
            <p className="tc-helper-copy">{loadingMessage}</p>
          </div>
        )}

        {!isLoading && error && (
          <div className="tc-panel">
            <p className="tc-banner tc-banner-error">{error}</p>
          </div>
        )}

        {!isLoading && !error && title && (
          <div className="tc-detail-content">
            <header className="tc-detail-header">
              <h1 className="tc-detail-title">{title}</h1>
              {subtitle && <p className="tc-subtitle">{subtitle}</p>}
            </header>

            {actions}

            <section className="tc-sheet">
              {entries.map((entry) => (
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

                  {showSourceMaterial && (
                    <details className="tc-source-details">
                      <summary className="tc-source-summary">
                        Show original source material
                      </summary>

                      <div className="tc-source-grid">
                        <div className="tc-source-card">
                          <p className="tc-source-label">Original Question</p>
                          <p className="tc-source-copy">
                            {entry.questionText?.trim()
                              ? entry.questionText
                              : "No original question text was provided for this entry."}
                          </p>
                        </div>

                        <div className="tc-source-card">
                          <p className="tc-source-label">Original Rough Note</p>
                          <p className="tc-source-copy">
                            {entry.roughNote?.trim()
                              ? entry.roughNote
                              : "No original rough note was provided for this entry."}
                          </p>
                        </div>
                      </div>
                    </details>
                  )}
                </article>
              ))}
            </section>
          </div>
        )}
      </section>
    </>
  );
}
