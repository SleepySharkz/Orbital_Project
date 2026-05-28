import type { CFCSummary } from "../../cfc/api/cfcApi";
import { CFCSummaryCard } from "./CFCSummaryCard";

type CFCSummaryListProps = {
  cfcs: CFCSummary[];
  isLoading: boolean;
  error: string;
};

export function CFCSummaryList({ cfcs, isLoading, error }: CFCSummaryListProps) {
  return (
    <section className="modules-panel modules-detail-cfcs-panel">
      <p className="modules-label">Saved CFCs</p>

      {isLoading && <p className="modules-body-copy">Loading saved flashcards...</p>}

      {!isLoading && error && (
        <p className="modules-error-message">{error}</p>
      )}

      {!isLoading && !error && cfcs.length === 0 && (
        <p className="modules-body-copy">No saved CFCs for this module yet.</p>
      )}

      {!isLoading && !error && cfcs.length > 0 && (
        <div className="modules-cfc-list">
          {cfcs.map((cfc) => (
            <CFCSummaryCard cfc={cfc} key={cfc.id} />
          ))}
        </div>
      )}
    </section>
  );
}
