import { Link } from "react-router-dom";
import type { SourceType } from "../api/cfcApi";
import type { CFCCollectionItem } from "./cfcCollectionTypes";

type CFCCollectionCardProps = {
  item: CFCCollectionItem;
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

export function CFCCollectionCard({ item }: CFCCollectionCardProps) {
  const { summary, topics, entryCount } = item;

  return (
    <article className="cfc-collection-card">
      <div className="cfc-collection-card-main">
        <div className="cfc-collection-kicker">
          <span>{summary.courseCode}</span>
          <span>{formatSourceType(summary.sourceType)}</span>
          <span>{formatDate(summary.createdAt)}</span>
        </div>

        <h3 className="cfc-collection-card-title">{summary.title}</h3>
        <p className="cfc-collection-card-source">{summary.sourceTitle}</p>
        <p className="cfc-collection-card-summary">{summary.summary}</p>

        <div className="cfc-collection-card-meta">
          <span>{summary.schoolSem}</span>
          <span>{entryCount} saved item(s)</span>
        </div>

        <ul className="cfc-collection-topic-list" aria-label="CFC topics">
          {topics.map((topic) => (
            <li className="cfc-collection-topic-pill" key={topic}>
              {topic}
            </li>
          ))}
        </ul>
      </div>

      <Link className="cfc-collection-open-link" to={`/my-cfcs/${summary.id}`}>
        Open CFC
      </Link>
    </article>
  );
}
