import { useState } from "react";
import { Link } from "react-router-dom";
import type { CFCResponse, SourceType } from "../api/cfcApi";

type CFCDetailHeaderProps = {
  cfc: CFCResponse;
  onSummarySave: (summary: string) => Promise<void>;
};

function formatSourceType(sourceType: SourceType) {
  return sourceType
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatDateTime(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function CFCDetailHeader({ cfc, onSummarySave }: CFCDetailHeaderProps) {
  const [isEditingSummary, setIsEditingSummary] = useState(false);
  const [summaryDraft, setSummaryDraft] = useState(cfc.summary);
  const [summaryError, setSummaryError] = useState("");
  const [isSavingSummary, setIsSavingSummary] = useState(false);

  function handleCancelSummaryEdit() {
    setSummaryDraft(cfc.summary);
    setSummaryError("");
    setIsEditingSummary(false);
  }

  async function handleSaveSummary() {
    const nextSummary = summaryDraft.trim();

    if (!nextSummary) {
      setSummaryError("Summary is required.");
      return;
    }

    try {
      setIsSavingSummary(true);
      setSummaryError("");
      await onSummarySave(nextSummary);
      setSummaryDraft(nextSummary);
      setIsEditingSummary(false);
    } catch (caughtError) {
      setSummaryError(
        caughtError instanceof Error ? caughtError.message : "Could not update summary.",
      );
    } finally {
      setIsSavingSummary(false);
    }
  }

  return (
    <section className="cfc-detail-header-panel">
      <div className="cfc-detail-header-copy">
        <p className="cfc-eyebrow">Saved CFC</p>
        <h1 className="cfc-detail-title">{cfc.title}</h1>

        {isEditingSummary ? (
          <div className="cfc-edit-form">
            <label className="cfc-edit-field">
              <span className="cfc-edit-label">Summary</span>
              <textarea
                className="cfc-edit-textarea cfc-edit-textarea-summary"
                value={summaryDraft}
                onChange={(event) => setSummaryDraft(event.target.value)}
                disabled={isSavingSummary}
              />
            </label>

            {summaryError && <p className="cfc-edit-error">{summaryError}</p>}

            <div className="cfc-edit-actions">
              <button
                className="cfc-save-button"
                type="button"
                onClick={() => void handleSaveSummary()}
                disabled={isSavingSummary}
              >
                {isSavingSummary ? "Saving..." : "Save Summary"}
              </button>
              <button
                className="cfc-cancel-button"
                type="button"
                onClick={handleCancelSummaryEdit}
                disabled={isSavingSummary}
              >
                Cancel
              </button>
            </div>
          </div>
        ) : (
          <div className="cfc-detail-summary-block">
            <p className="cfc-detail-summary">{cfc.summary}</p>
            <button
              className="cfc-edit-button"
              type="button"
              onClick={() => {
                setSummaryDraft(cfc.summary);
                setSummaryError("");
                setIsEditingSummary(true);
              }}
            >
              Edit Summary
            </button>
          </div>
        )}
      </div>

      <dl className="cfc-detail-meta-grid">
        <div>
          <dt>Module</dt>
          <dd>{cfc.courseCode}</dd>
        </div>
        <div>
          <dt>Semester</dt>
          <dd>{cfc.schoolSem}</dd>
        </div>
        <div>
          <dt>Source</dt>
          <dd>{formatSourceType(cfc.sourceType)}</dd>
        </div>
        <div>
          <dt>Source Title</dt>
          <dd>{cfc.sourceTitle}</dd>
        </div>
        <div>
          <dt>Created</dt>
          <dd>{formatDateTime(cfc.createdAt)}</dd>
        </div>
      </dl>

      <Link className="cfc-secondary-link" to="/my-cfcs">
        Back to My CFCs
      </Link>
    </section>
  );
}
