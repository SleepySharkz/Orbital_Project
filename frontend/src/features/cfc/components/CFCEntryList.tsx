import type { CreatedCFCEntry } from "../api/cfcApi";
import { CFCEntryCard } from "./CFCEntryCard";

type CFCEntryListProps = {
  entries: CreatedCFCEntry[];
};

export function CFCEntryList({ entries }: CFCEntryListProps) {
  return (
    <section className="cfc-detail-entries-panel">
      <div className="cfc-detail-entries-header">
        <div>
          <p className="cfc-eyebrow">Entries</p>
          <h2 className="cfc-detail-section-heading">Generated Flashcard Entries</h2>
        </div>
        <p className="cfc-entry-counter">{entries.length} saved item(s)</p>
      </div>

      {entries.length === 0 ? (
        <p className="cfc-helper-copy">No saved entries for this CFC.</p>
      ) : (
        <div className="cfc-detail-entry-list">
          {entries.map((entry) => (
            <CFCEntryCard entry={entry} key={entry.id} />
          ))}
        </div>
      )}
    </section>
  );
}
