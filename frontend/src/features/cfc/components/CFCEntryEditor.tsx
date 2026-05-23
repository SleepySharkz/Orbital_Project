import type { ChangeEvent } from "react";
import type { EntryDraft } from "./cfcFormTypes";

type CFCEntryEditorProps = {
  activeEntry: EntryDraft;
  activeEntryIndex: number;
  draftEntryCount: number;
  moduleTopics: string[];
  isLoadingModules: boolean;
  isGenerating: boolean;
  modulesCount: number;
  submissionError: string;
  onFilesChanged: (event: ChangeEvent<HTMLInputElement>) => void;
  onEntryChange: (updatedFields: Partial<EntryDraft>) => void;
  onPreviousEntry: () => void;
  onNextEntry: () => void;
  onGenerate: () => void;
};

export function CFCEntryEditor({
  activeEntry,
  activeEntryIndex,
  draftEntryCount,
  moduleTopics,
  isLoadingModules,
  isGenerating,
  modulesCount,
  submissionError,
  onFilesChanged,
  onEntryChange,
  onPreviousEntry,
  onNextEntry,
  onGenerate,
}: CFCEntryEditorProps) {
  return (
    <section className={`cfc-entry-shell ${submissionError ? "cfc-entry-shell-error" : ""}`}>
      <div className="cfc-entry-shell-header">
        <div>
          <p className="cfc-entry-step">Entry {activeEntryIndex + 1}</p>
          <h2 className="cfc-entry-title">Build One Question-Note Pair</h2>
        </div>

        <div className="cfc-entry-counter">{draftEntryCount} draft item(s)</div>
      </div>

      <div className="cfc-entry-grid">
        <section className="cfc-panel cfc-panel-tight">
          <p className="cfc-panel-number">1. Upload Question Material</p>
          <label className="cfc-upload-box" htmlFor="cfc-files">
            <span className="cfc-upload-title">Add up to 2 screenshots</span>
            <span className="cfc-upload-copy">PNG only. These are temporary input files.</span>
            <input
              id="cfc-files"
              className="cfc-upload-input"
              type="file"
              accept="image/png"
              multiple
              onChange={onFilesChanged}
            />
          </label>

          {activeEntry.files.length > 0 ? (
            <ul className="cfc-file-list">
              {activeEntry.files.map((file) => (
                <li key={file.name} className="cfc-file-item">
                  {file.name}
                </li>
              ))}
            </ul>
          ) : (
            <p className="cfc-helper-copy">No screenshots added for this entry yet.</p>
          )}

          <label className="cfc-field-group" htmlFor="cfc-question-text">
            <span className="cfc-field-label">Question Text (Optional if images are present)</span>
            <textarea
              id="cfc-question-text"
              className="cfc-textarea cfc-textarea-question"
              value={activeEntry.questionText}
              onChange={(event) => onEntryChange({ questionText: event.target.value })}
              placeholder="Paste or type the question text here if you want to include it."
            />
          </label>
        </section>

        <section className="cfc-panel cfc-panel-tight">
          <p className="cfc-panel-number">2. Add Your Notes</p>

          <label className="cfc-field-group" htmlFor="cfc-topic">
            <span className="cfc-field-label">Topic</span>
            <select
              id="cfc-topic"
              className="cfc-input"
              value={activeEntry.topic}
              onChange={(event) => onEntryChange({ topic: event.target.value })}
            >
              <option value="">Select a topic</option>
              {moduleTopics.map((topic) => (
                <option key={topic} value={topic}>
                  {topic}
                </option>
              ))}
            </select>
          </label>

          <label className="cfc-field-group" htmlFor="cfc-rough-note">
            <span className="cfc-field-label">Rough Note</span>
            <textarea
              id="cfc-rough-note"
              className="cfc-textarea"
              value={activeEntry.roughNote}
              onChange={(event) => onEntryChange({ roughNote: event.target.value })}
              placeholder="Explain what confused you, what you forgot, or what you want the flashcard to teach back to you."
            />
          </label>

          <div className="cfc-actions">
            <button
              className="cfc-secondary-button"
              type="button"
              onClick={onPreviousEntry}
              disabled={activeEntryIndex === 0}
            >
              Previous
            </button>

            <button
              className="cfc-secondary-button"
              type="button"
              onClick={onNextEntry}
            >
              Next Entry
            </button>

            <button
              className="cfc-primary-button"
              type="button"
              onClick={onGenerate}
              disabled={isGenerating || isLoadingModules || modulesCount === 0}
            >
              {isGenerating ? "Generating..." : "Generate"}
            </button>
          </div>
        </section>
      </div>
    </section>
  );
}
