import type { TfcSummary } from "../api/tfcApi";

type TFCSummaryCardProps = {
  tfc: TfcSummary;
  isSelected: boolean;
  onOpen: (tfcId: number) => void;
  currentUsername: string;
};

function formatUpdatedAt(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString(undefined, {
    dateStyle: "medium",
  });
}

export function TFCSummaryCard({ tfc, isSelected, onOpen, currentUsername }: TFCSummaryCardProps) {
  const ownerLabel =
    tfc.ownerUsername.trim().toLowerCase() === currentUsername.trim().toLowerCase()
      ? "You"
      : tfc.ownerUsername;

  return (
    <button
      className={isSelected ? "tfc-card tfc-card-selected" : "tfc-card"}
      type="button"
      onClick={() => onOpen(tfc.id)}
    >
      <div className="tfc-card-main">
        <div className="tfc-card-kicker">
          <span>{tfc.courseCode}</span>
          <span>{tfc.schoolSem}</span>
        </div>

        <h2 className="tfc-card-title">{tfc.topic}</h2>

        <div className="tfc-card-meta">
          <span>{tfc.entryCount} linked {tfc.entryCount === 1 ? "entry" : "entries"}</span>
          <span>Owner: {ownerLabel}</span>
          <span>Updated {formatUpdatedAt(tfc.updatedAt)}</span>
        </div>
      </div>

      <span className="tfc-open-link">
        {isSelected ? "Viewing Cheatsheet" : "Open Cheatsheet"}
      </span>
    </button>
  );
}
