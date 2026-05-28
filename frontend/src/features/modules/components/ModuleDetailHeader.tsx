import type { ModuleResponse } from "../api/moduleApi";

type ModuleDetailHeaderProps = {
  module: ModuleResponse;
};

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

export function ModuleDetailHeader({ module }: ModuleDetailHeaderProps) {
  return (
    <section className="modules-panel modules-detail-header-panel">
      <div>
        <p className="modules-label">Module</p>
        <h2 className="modules-detail-code">{module.courseCode}</h2>
        <p className="modules-detail-semester">{module.schoolSem}</p>
      </div>

      <dl className="modules-detail-meta">
        <div>
          <dt>Created</dt>
          <dd>{formatDateTime(module.createdAt)}</dd>
        </div>
        <div>
          <dt>Updated</dt>
          <dd>{formatDateTime(module.updatedAt)}</dd>
        </div>
      </dl>
    </section>
  );
}
