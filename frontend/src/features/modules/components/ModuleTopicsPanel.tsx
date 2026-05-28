type ModuleTopicsPanelProps = {
  topics: string[];
};

export function ModuleTopicsPanel({ topics }: ModuleTopicsPanelProps) {
  return (
    <section className="modules-panel modules-detail-topics-panel">
      <p className="modules-label">Topics</p>

      {topics.length === 0 ? (
        <p className="modules-body-copy">No topics saved for this module.</p>
      ) : (
        <ul className="modules-topics-items">
          {topics.map((topic) => (
            <li className="modules-topic-pill" key={topic}>
              {topic}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
