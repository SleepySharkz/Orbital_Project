import { Link } from "react-router-dom";
import type { TfcContentResponse } from "../api/tfcApi";

type TFCDetailHeaderProps = {
  tfc: TfcContentResponse;
};

function formatUpdatedAt(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function TFCDetailHeader({ tfc }: TFCDetailHeaderProps) {
  return (
    <header className="tfc-detail-header">
      <div>
        <p className="tfc-eyebrow">Topic Sheet</p>
        <h1 className="tfc-title">{tfc.topic}</h1>
        <p className="tfc-subtitle">
          Review linked learning points across this topic in one continuous study sheet.
        </p>
      </div>

      <div className="tfc-detail-actions">
        <div className="tfc-detail-meta">
          <span>{tfc.courseCode}</span>
          <span>{tfc.schoolSem}</span>
          <span>{tfc.entries.length} entries</span>
          <span>Updated {formatUpdatedAt(tfc.updatedAt)}</span>
        </div>

        <Link className="tfc-secondary-link" to="/topic-sheets">
          Back to Topic Sheets
        </Link>
      </div>
    </header>
  );
}
