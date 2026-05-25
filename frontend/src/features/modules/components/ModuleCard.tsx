import { useState } from "react";
import {
  fetchModuleTopics,
  type ModuleSummary,
  type ModuleTopicsResponse,
} from "../api/moduleApi";

type ModuleCardProps = {
  module: ModuleSummary;
  token: string;
};

export function ModuleCard({ module, token }: ModuleCardProps) {
  const [expanded, setExpanded] = useState(false);
  const [topicsData, setTopicsData] = useState<ModuleTopicsResponse | null>(null);
  const [topicsError, setTopicsError] = useState("");
  const [isTopicsLoading, setIsTopicsLoading] = useState(false);

  async function handleToggleTopics() {
    if (expanded) {
      setExpanded(false);
      setTopicsError("");
      return;
    }

    setExpanded(true);
    setTopicsError("");

    if (topicsData) {
      return;
    }

    try {
      setIsTopicsLoading(true);
      const moduleTopics = await fetchModuleTopics(module.id, token);
      setTopicsData(moduleTopics);
    } catch (caughtError) {
      setExpanded(false);

      if (caughtError instanceof Error) {
        setTopicsError(caughtError.message);
      } else {
        setTopicsError("Could not load module topics.");
      }
    } finally {
      setIsTopicsLoading(false);
    }
  }

  return (
    <article className="modules-card">
      <div className="modules-card-top">
        <div className="modules-card-copy">
          <p className="modules-card-code">{module.courseCode}</p>
          <p className="modules-card-sem">{module.schoolSem}</p>
        </div>

        <button
          className="modules-topics-toggle"
          type="button"
          onClick={() => void handleToggleTopics()}
        >
          {expanded ? "Hide Topics" : "View Topics"}
        </button>
      </div>

      {expanded && (
        <div className="modules-topics-dropdown">
          {isTopicsLoading && (
            <p className="modules-topics-message">Loading topics...</p>
          )}

          {!isTopicsLoading && topicsData && (
            <ul className="modules-topics-items">
              {topicsData.topics.map((topic) => (
                <li className="modules-topic-pill" key={topic}>
                  {topic}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {topicsError && (
        <p className="modules-error-message">{topicsError}</p>
      )}
    </article>
  );
}
