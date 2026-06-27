import type { TcSummary } from "../api/tcApi";

type TCSummaryCardProps = {
  tc: TcSummary;
  isSelected: boolean;
  onOpen: (tcId: number) => void;
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

export function TCSummaryCard({ tc, isSelected, onOpen, currentUsername }: TCSummaryCardProps) {
  const ownerLabel =
    tc.ownerUsername.trim().toLowerCase() === currentUsername.trim().toLowerCase()
      ? "You"
      : tc.ownerUsername;

  return (
    <button
      className={isSelected ? "tc-card tc-card-selected" : "tc-card"}
      type="button"
      onClick={() => onOpen(tc.id)}
    >
      <div className="tc-card-main">
        <div className="tc-card-kicker">
          <span>{tc.courseCode}</span>
          <span>{tc.schoolSem}</span>
        </div>

        <h2 className="tc-card-title">{tc.topic}</h2>

        <div className="tc-card-meta">
          <span>{tc.entryCount} linked {tc.entryCount === 1 ? "entry" : "entries"}</span>
          <span>Owner: {ownerLabel}</span>
          <span>Updated {formatUpdatedAt(tc.updatedAt)}</span>
        </div>

        {tc.isStale && (
          <p className="tc-stale-badge">Historical topic</p>
        )}
      </div>

      <span className="tc-open-link">
        {isSelected ? "Viewing Cheatsheet" : "Open Cheatsheet"}
      </span>
    </button>
  );
}
