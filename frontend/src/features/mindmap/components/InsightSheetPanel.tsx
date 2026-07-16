import type { MindmapEdge } from "../types/mindmapTypes";

type InsightSheetPanelProps = {
  edge: MindmapEdge;
  sourceTopic: string;
  targetTopic: string;
  onClose: () => void;
};

export function InsightSheetPanel({
  edge,
  sourceTopic,
  targetTopic,
  onClose,
}: InsightSheetPanelProps) {
  return (
    <aside aria-label="Saved insight" className="mindmap-insight-panel">
      <div className="mindmap-insight-panel-header">
        <div>
          <p className="mindmap-label">Saved insight</p>
          <p className="mindmap-insight-topics">
            {sourceTopic} / {targetTopic}
          </p>
        </div>
        <button className="mindmap-panel-close" type="button" onClick={onClose}>
          Close
        </button>
      </div>

      {edge.isRefreshing && (
        <p className="mindmap-refresh-status">Refreshing</p>
      )}

      <div className="mindmap-insight-copy">
        <h2>{edge.title}</h2>
        <p>{edge.summary}</p>
      </div>

      <p className="mindmap-insight-updated">
        Updated {formatDateTime(edge.updatedAt)}
      </p>
    </aside>
  );
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
