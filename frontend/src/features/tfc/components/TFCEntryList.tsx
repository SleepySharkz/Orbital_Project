import type { TfcEntryView } from "../api/tfcApi";

type TFCEntryListProps = {
  entries: TfcEntryView[];
};

export function TFCEntryList({ entries }: TFCEntryListProps) {
  return (
    <section className="tfc-sheet">
      {entries.map((entry) => (
        <article className="tfc-sheet-entry" key={entry.entryId}>
          <div className="tfc-entry-header">
            <h2 className="tfc-entry-question">{entry.flashcardQuestion}</h2>
          </div>

          <div className="tfc-note-block">
            {entry.flashcardNoteContent.split("\n").map((line, lineIndex) => (
              <p className="tfc-note-line" key={`${entry.entryId}-${lineIndex}`}>
                {line}
              </p>
            ))}
          </div>

          <details className="tfc-source-details">
            <summary className="tfc-source-summary">Show original source material</summary>

            <div className="tfc-source-grid">
              <div className="tfc-source-card">
                <p className="tfc-source-label">Original Question</p>
                <p className="tfc-source-copy">
                  {entry.questionText && entry.questionText.trim().length > 0
                    ? entry.questionText
                    : "No original question text was provided for this entry."}
                </p>
              </div>

              <div className="tfc-source-card">
                <p className="tfc-source-label">Original Rough Note</p>
                <p className="tfc-source-copy">{entry.roughNote}</p>
              </div>
            </div>
          </details>
        </article>
      ))}
    </section>
  );
}
