import type { TfcEntryView } from "../api/tfcApi";

type TFCEntryListProps = {
  entries: TfcEntryView[];
};

function formatCreatedAt(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function TFCEntryList({ entries }: TFCEntryListProps) {
  return (
    <section className="tfc-entry-list">
      {entries.map((entry, index) => (
        <article className="tfc-entry-card" key={entry.entryId}>
          <div className="tfc-entry-header">
            <div>
              <p className="tfc-entry-kicker">Entry {index + 1}</p>
              <h2 className="tfc-entry-question">{entry.flashcardQuestion}</h2>
            </div>

            <p className="tfc-entry-created-at">{formatCreatedAt(entry.createdAt)}</p>
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
