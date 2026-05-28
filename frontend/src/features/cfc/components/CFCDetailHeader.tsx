import { Link } from "react-router-dom";
import type { CFCResponse, SourceType } from "../api/cfcApi";

type CFCDetailHeaderProps = {
  cfc: CFCResponse;
};

function formatSourceType(sourceType: SourceType) {
  return sourceType
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function formatDateTime(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function CFCDetailHeader({ cfc }: CFCDetailHeaderProps) {
  return (
    <section className="cfc-detail-header-panel">
      <div className="cfc-detail-header-copy">
        <p className="cfc-eyebrow">Saved CFC</p>
        <h1 className="cfc-detail-title">{cfc.title}</h1>
        <p className="cfc-detail-summary">{cfc.summary}</p>
      </div>

      <dl className="cfc-detail-meta-grid">
        <div>
          <dt>Module</dt>
          <dd>{cfc.courseCode}</dd>
        </div>
        <div>
          <dt>Semester</dt>
          <dd>{cfc.schoolSem}</dd>
        </div>
        <div>
          <dt>Source</dt>
          <dd>{formatSourceType(cfc.sourceType)}</dd>
        </div>
        <div>
          <dt>Source Title</dt>
          <dd>{cfc.sourceTitle}</dd>
        </div>
        <div>
          <dt>Created</dt>
          <dd>{formatDateTime(cfc.createdAt)}</dd>
        </div>
      </dl>

      <Link className="cfc-secondary-link" to="/my-cfcs">
        Back to My CFCs
      </Link>
    </section>
  );
}
