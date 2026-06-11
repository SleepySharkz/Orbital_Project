import { useState } from "react";
import type { CFCContent, CreatedCFCEntry } from "../api/cfcApi";

type CFCEntryCardProps = {
  entry: CreatedCFCEntry;
  onEntryContentSave: (entryId: number, content: CFCContent) => Promise<void>;
};

function textOrFallback(value: string | null, fallback: string) {
  if (!value || !value.trim()) {
    return fallback;
  }

  return value;
}

export function CFCEntryCard({ entry, onEntryContentSave }: CFCEntryCardProps) {
  const [isEditing, setIsEditing] = useState(false);
  const [contentDraft, setContentDraft] = useState<CFCContent>({
    flashcardQuestion: entry.flashcardQuestion,
    flashcardNoteContent: entry.flashcardNoteContent,
  });
  const [saveError, setSaveError] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  function updateDraft(field: keyof CFCContent, value: string) {
    setContentDraft((currentDraft) => ({
      ...currentDraft,
      [field]: value,
    }));
  }

  function handleCancelEdit() {
    setContentDraft({
      flashcardQuestion: entry.flashcardQuestion,
      flashcardNoteContent: entry.flashcardNoteContent,
    });
    setSaveError("");
    setIsEditing(false);
  }

  async function handleSaveContent() {
    const nextContent = {
      flashcardQuestion: contentDraft.flashcardQuestion.trim(),
      flashcardNoteContent: contentDraft.flashcardNoteContent.trim(),
    };

    if (!nextContent.flashcardQuestion || !nextContent.flashcardNoteContent) {
      setSaveError("All generated content fields are required.");
      return;
    }

    try {
      setIsSaving(true);
      setSaveError("");
      await onEntryContentSave(entry.id, nextContent);
      setContentDraft(nextContent);
      setIsEditing(false);
    } catch (caughtError) {
      setSaveError(
        caughtError instanceof Error ? caughtError.message : "Could not update entry.",
      );
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <article className="cfc-detail-entry-card">
      <header className="cfc-detail-entry-header">
        <div>
          <p className="cfc-entry-step">Entry {entry.requestItemId}</p>
          <h3 className="cfc-detail-entry-topic">{entry.topic}</h3>
        </div>

        {!isEditing && (
          <button
            className="cfc-edit-button"
            type="button"
            onClick={() => {
              setContentDraft({
                flashcardQuestion: entry.flashcardQuestion,
                flashcardNoteContent: entry.flashcardNoteContent,
              });
              setSaveError("");
              setIsEditing(true);
            }}
          >
            Edit Generated Content
          </button>
        )}
      </header>

      <div className="cfc-detail-entry-sections">
        <section className="cfc-detail-entry-section">
          <p className="cfc-detail-section-title">Generated Content</p>

          {isEditing ? (
            <div className="cfc-edit-form">
              <label className="cfc-edit-field">
                <span className="cfc-edit-label">Flashcard Question</span>
                <textarea
                  className="cfc-edit-textarea"
                  value={contentDraft.flashcardQuestion}
                  onChange={(event) => updateDraft("flashcardQuestion", event.target.value)}
                  disabled={isSaving}
                />
              </label>

              <label className="cfc-edit-field">
                <span className="cfc-edit-label">Flashcard Note Content</span>
                <textarea
                  className="cfc-edit-textarea"
                  value={contentDraft.flashcardNoteContent}
                  onChange={(event) => updateDraft("flashcardNoteContent", event.target.value)}
                  disabled={isSaving}
                />
              </label>

              {saveError && <p className="cfc-edit-error">{saveError}</p>}

              <div className="cfc-edit-actions">
                <button
                  className="cfc-save-button"
                  type="button"
                  onClick={() => void handleSaveContent()}
                  disabled={isSaving}
                >
                  {isSaving ? "Saving..." : "Save Entry"}
                </button>
                <button
                  className="cfc-cancel-button"
                  type="button"
                  onClick={handleCancelEdit}
                  disabled={isSaving}
                >
                  Cancel
                </button>
              </div>
            </div>
          ) : (
            <dl className="cfc-detail-content-list">
              <div>
                <dt>Flashcard Question</dt>
                <dd>{entry.flashcardQuestion}</dd>
              </div>
              <div>
                <dt>Flashcard Note Content</dt>
                <dd>{entry.flashcardNoteContent}</dd>
              </div>
            </dl>
          )}
        </section>

        <section className="cfc-detail-entry-section cfc-detail-source-section">
          <p className="cfc-detail-section-title">Source Material</p>

          <div className="cfc-detail-source-block">
            <p className="cfc-detail-source-label">Question Text</p>
            <p className="cfc-detail-source-copy">
              {textOrFallback(entry.sourceMaterial.questionText, "No saved question text.")}
            </p>
          </div>

          <div className="cfc-detail-source-block">
            <p className="cfc-detail-source-label">Rough Note</p>
            <p className="cfc-detail-source-copy">
              {textOrFallback(entry.sourceMaterial.roughNote, "No rough note saved.")}
            </p>
          </div>
        </section>
      </div>
    </article>
  );
}
