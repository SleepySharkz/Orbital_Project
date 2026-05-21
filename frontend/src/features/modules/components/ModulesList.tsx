import type { ModuleSummary } from "../api/moduleApi";
import { ModuleCard } from "./ModuleCard";

type ModulesListProps = {
  modules: ModuleSummary[];
  token: string;
  isModulesLoading: boolean;
  modulesError: string;
};

export function ModulesList({
  modules,
  token,
  isModulesLoading,
  modulesError,
}: ModulesListProps) {
  return (
    <section className="modules-panel">
      <p className="modules-label">My Modules</p>

      {isModulesLoading && <p className="modules-body-copy">Loading modules...</p>}

      {!isModulesLoading && modulesError && (
        <p className="modules-error-message">{modulesError}</p>
      )}

      {!isModulesLoading && !modulesError && modules.length === 0 && (
        <p className="modules-body-copy">No modules added yet.</p>
      )}

      {!isModulesLoading && !modulesError && modules.length > 0 && (
        <div className="modules-list">
          {modules.map((module) => (
            <ModuleCard key={module.id} module={module} token={token} />
          ))}
        </div>
      )}
    </section>
  );
}
