import type { ModuleSummary } from "../../modules/api/moduleApi";

type ModuleMindmapSelectorProps = {
  modules: ModuleSummary[];
  selectedModuleId: number | "";
  disabled: boolean;
  onChange: (moduleId: number) => void;
};

export function ModuleMindmapSelector({
  modules,
  selectedModuleId,
  disabled,
  onChange,
}: ModuleMindmapSelectorProps) {
  return (
    <label className="mindmap-module-field">
      <span>Module</span>
      <select
        aria-label="Module"
        className="mindmap-module-select"
        disabled={disabled || modules.length === 0}
        value={selectedModuleId}
        onChange={(event) => onChange(Number(event.target.value))}
      >
        {modules.length === 0 ? (
          <option value="">No modules</option>
        ) : (
          modules.map((module) => (
            <option key={module.id} value={module.id}>
              {module.courseCode} - {module.schoolSem}
            </option>
          ))
        )}
      </select>
    </label>
  );
}
