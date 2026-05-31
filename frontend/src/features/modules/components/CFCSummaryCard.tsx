import { Link } from "react-router-dom";
import type { CFCSummary, SourceType } from "../../cfc/api/cfcApi";

type CFCSummaryCardProps = {
  cfc: CFCSummary;
};

function formatSourceType(sourceType: SourceType) {
  return sourceType
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString(undefined, {
    dateStyle: "medium",
  });
}

export function CFCSummaryCard({ cfc }: CFCSummaryCardProps) {
  return (
    <article className="modules-cfc-card">
      <div className="modules-cfc-card-copy">
        <div className="modules-cfc-card-kicker">
          <span>{formatSourceType(cfc.sourceType)}</span>
          <span>{formatDate(cfc.createdAt)}</span>
        </div>
        <h3 className="modules-cfc-title">{cfc.title}</h3>
        <p className="modules-cfc-source">{cfc.sourceTitle}</p>
        <p className="modules-cfc-summary">{cfc.summary}</p>
      </div>

      <Link className="modules-primary-link" to={`/my-cfcs/${cfc.id}`}>
        Open CFC
      </Link>
    </article>
  );
}
