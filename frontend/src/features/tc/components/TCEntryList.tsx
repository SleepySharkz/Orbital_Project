import type { TcEntryView } from "../api/tcApi";

type TCEntryListProps = {
  entries: TcEntryView[];
};

export function TCEntryList({ entries }: TCEntryListProps) {
  return (
    <section className="tc-sheet">
      {entries.map((entry) => (
        <article className="tc-sheet-entry" key={entry.entryId}>
          <div className="tc-entry-header">
            <h2 className="tc-entry-question">{entry.flashcardQuestion}</h2>
          </div>

          <div className="tc-note-block">
            {entry.flashcardNoteContent.split("\n").map((line, lineIndex) => (
              <p className="tc-note-line" key={`${entry.entryId}-${lineIndex}`}>
                {line}
              </p>
            ))}
          </div>

          <details className="tc-source-details">
            <summary className="tc-source-summary">Show original source material</summary>

            <div className="tc-source-grid">
              <div className="tc-source-card">
                <p className="tc-source-label">Original Question</p>
                <p className="tc-source-copy">
                  {entry.questionText && entry.questionText.trim().length > 0
                    ? entry.questionText
                    : "No original question text was provided for this entry."}
                </p>
              </div>

              <div className="tc-source-card">
                <p className="tc-source-label">Original Rough Note</p>
                <p className="tc-source-copy">{entry.roughNote}</p>
              </div>
            </div>
          </details>
        </article>
      ))}
    </section>
  );
}
