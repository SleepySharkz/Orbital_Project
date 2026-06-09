import type { CFCContent, CreatedCFCEntry } from "../api/cfcApi";
import { CFCEntryCard } from "./CFCEntryCard";

type CFCEntryListProps = {
  entries: CreatedCFCEntry[];
  onEntryContentSave: (entryId: number, content: CFCContent) => Promise<void>;
};

export function CFCEntryList({ entries, onEntryContentSave }: CFCEntryListProps) {
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
            <CFCEntryCard
              entry={entry}
              key={entry.id}
              onEntryContentSave={onEntryContentSave}
            />
          ))}
        </div>
      )}
    </section>
  );
}
