import type { MindmapInsightDetail } from "../types/mindmapTypes";

type InsightSheetPanelProps = {
  insight: MindmapInsightDetail;
  onClose: () => void;
};

export function InsightSheetPanel({
  insight,
  onClose,
}: InsightSheetPanelProps) {
  return (
    <aside aria-label="Saved insight" className="mindmap-insight-panel">
      <div className="mindmap-insight-panel-header">
        <div>
          <p className="mindmap-label">Insight sheet</p>
          <p className="mindmap-insight-topics">
            {insight.topicA} / {insight.topicB}
          </p>
        </div>
        <button className="mindmap-panel-close" type="button" onClick={onClose}>
          Close
        </button>
      </div>

      {insight.status === "GENERATING" && (
        <div aria-live="polite" className="mindmap-generation-state">
          <strong>Discovering insight</strong>
          <p>Analyzing both Topical Cheatsheets...</p>
        </div>
      )}

      {insight.status === "REFRESHING" && (
        <p className="mindmap-refresh-status">Refreshing</p>
      )}

      {(insight.status === "READY" || insight.status === "REFRESHING") &&
        insight.title &&
        insight.summary && (
          <>
            <div className="mindmap-insight-copy">
              <h2>{insight.title}</h2>
              <p>{insight.summary}</p>
            </div>

            <ol className="mindmap-insight-points">
              {insight.points.map((point) => (
                <li key={`${point.displayOrder}-${point.heading}`}>
                  <h3>{point.heading}</h3>
                  <p>{point.explanation}</p>
                  {(point.sourceTopicAReferences ||
                    point.sourceTopicBReferences) && (
                    <dl className="mindmap-point-sources">
                      {point.sourceTopicAReferences && (
                        <>
                          <dt>{insight.topicA}</dt>
                          <dd>{point.sourceTopicAReferences}</dd>
                        </>
                      )}
                      {point.sourceTopicBReferences && (
                        <>
                          <dt>{insight.topicB}</dt>
                          <dd>{point.sourceTopicBReferences}</dd>
                        </>
                      )}
                    </dl>
                  )}
                </li>
              ))}
            </ol>
          </>
        )}

      {insight.status === "NO_USEFUL_LINK" && (
        <div className="mindmap-result-message mindmap-result-no-link">
          <strong>No useful link found</strong>
          <p>
            {insight.rejectionReason ||
              "The current material does not support a useful relationship."}
          </p>
        </div>
      )}

      {insight.status === "GENERATION_FAILED" && (
        <div className="mindmap-result-message mindmap-result-failed">
          <strong>Insight generation failed</strong>
          <p>
            {insight.rejectionReason ||
              "Please close this panel and try the selected pair again."}
          </p>
        </div>
      )}

      {insight.updatedAt && (
        <p className="mindmap-insight-updated">
          Updated {formatDateTime(insight.updatedAt)}
        </p>
      )}
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
