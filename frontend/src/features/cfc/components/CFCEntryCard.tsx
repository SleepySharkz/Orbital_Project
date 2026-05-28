import type { CreatedCFCEntry } from "../api/cfcApi";

type CFCEntryCardProps = {
  entry: CreatedCFCEntry;
};

function textOrFallback(value: string | null, fallback: string) {
  if (!value || !value.trim()) {
    return fallback;
  }

  return value;
}

export function CFCEntryCard({ entry }: CFCEntryCardProps) {
  return (
    <article className="cfc-detail-entry-card">
      <header className="cfc-detail-entry-header">
        <p className="cfc-entry-step">Entry {entry.requestItemId}</p>
        <h3 className="cfc-detail-entry-topic">{entry.topic}</h3>
      </header>

      <div className="cfc-detail-entry-sections">
        <section className="cfc-detail-entry-section">
          <p className="cfc-detail-section-title">Generated Content</p>

          <dl className="cfc-detail-content-list">
            <div>
              <dt>Learning Point</dt>
              <dd>{entry.content.learningPoint}</dd>
            </div>
            <div>
              <dt>Explanation</dt>
              <dd>{entry.content.explanation}</dd>
            </div>
            <div>
              <dt>Mistake Pattern</dt>
              <dd>{entry.content.mistakePattern}</dd>
            </div>
            <div>
              <dt>Review Prompt</dt>
              <dd>{entry.content.reviewPrompt}</dd>
            </div>
          </dl>
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
