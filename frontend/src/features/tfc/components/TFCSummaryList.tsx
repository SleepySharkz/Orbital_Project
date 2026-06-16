import type { TfcSummary } from "../api/tfcApi";
import { TFCSummaryCard } from "./TFCSummaryCard";

type TFCSummaryListProps = {
  tfcs: TfcSummary[];
  isLoading: boolean;
  error: string;
  selectedTfcId: number | null;
  onOpenTfc: (tfcId: number) => void;
};

export function TFCSummaryList({
  tfcs,
  isLoading,
  error,
  selectedTfcId,
  onOpenTfc,
}: TFCSummaryListProps) {
  return (
    <section className="tfc-section">
      {isLoading && (
        <div className="tfc-panel">
          <p className="tfc-helper-copy">Loading topic sheets...</p>
        </div>
      )}

      {!isLoading && error && (
        <div className="tfc-panel">
          <p className="tfc-banner tfc-banner-error">{error}</p>
        </div>
      )}

      {!isLoading && !error && tfcs.length === 0 && (
        <div className="tfc-panel">
          <p className="tfc-helper-copy">No topic sheets available yet. Generate some CFCs first.</p>
        </div>
      )}

      {!isLoading && !error && tfcs.length > 0 && (
        <div className="tfc-card-list">
          {tfcs.map((tfc) => (
            <TFCSummaryCard
              key={tfc.id}
              tfc={tfc}
              isSelected={selectedTfcId === tfc.id}
              onOpen={onOpenTfc}
            />
          ))}
        </div>
      )}
    </section>
  );
}
