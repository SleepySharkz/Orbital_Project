import type { TcSummary } from "../api/tcApi";
import { TCSummaryCard } from "./TCSummaryCard";

type TCSummaryListProps = {
  tcs: TcSummary[];
  isLoading: boolean;
  error: string;
  selectedTcId: number | null;
  onOpenTc: (tcId: number) => void;
  currentUsername: string;
};

export function TCSummaryList({
  tcs,
  isLoading,
  error,
  selectedTcId,
  onOpenTc,
  currentUsername,
}: TCSummaryListProps) {
  return (
    <section className="tc-section">
      {isLoading && (
        <div className="tc-panel">
          <p className="tc-helper-copy">Loading topic sheets...</p>
        </div>
      )}

      {!isLoading && error && (
        <div className="tc-panel">
          <p className="tc-banner tc-banner-error">{error}</p>
        </div>
      )}

      {!isLoading && !error && tcs.length === 0 && (
        <div className="tc-panel">
          <p className="tc-helper-copy">No topic sheets available yet. Generate some CFCs first.</p>
        </div>
      )}

      {!isLoading && !error && tcs.length > 0 && (
        <div className="tc-card-list">
          {tcs.map((tc) => (
            <TCSummaryCard
              key={tc.id}
              tc={tc}
              isSelected={selectedTcId === tc.id}
              onOpen={onOpenTc}
              currentUsername={currentUsername}
            />
          ))}
        </div>
      )}
    </section>
  );
}
