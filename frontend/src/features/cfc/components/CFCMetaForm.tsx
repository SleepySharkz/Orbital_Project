import type { ModuleSummary } from "../../modules/api/moduleApi";
import type { SourceType } from "../api/cfcApi";

const SOURCE_TYPE_OPTIONS: Array<{ label: string; value: SourceType }> = [
  { label: "Assignment", value: "ASSIGNMENT" },
  { label: "Tutorial", value: "TUTORIAL" },
  { label: "Practice Paper", value: "PRACTICE_PAPER" },
];

type CFCMetaFormProps = {
  modules: ModuleSummary[];
  isLoadingModules: boolean;
  selectedModuleId: number | null;
  sourceType: SourceType;
  sourceTitle: string;
  onModuleChange: (moduleId: number) => void;
  onSourceTypeChange: (sourceType: SourceType) => void;
  onSourceTitleChange: (sourceTitle: string) => void;
  resetDraftState: () => void;
};

export function CFCMetaForm({
  modules,
  isLoadingModules,
  selectedModuleId,
  sourceType,
  sourceTitle,
  onModuleChange,
  onSourceTypeChange,
  onSourceTitleChange,
  resetDraftState,
}: CFCMetaFormProps) {
  return (
    <div className="cfc-entry-meta">
      <div className="cfc-meta-card">
        <label className="cfc-field-label" htmlFor="cfc-module">
          Module
        </label>
        <select
          id="cfc-module"
          className="cfc-input"
          value={selectedModuleId ?? ""}
          onChange={(event) => {
            onModuleChange(Number(event.target.value));
            resetDraftState();
          }}
          disabled={isLoadingModules || modules.length === 0}
        >
          {modules.map((module) => (
            <option key={module.id} value={module.id}>
              {module.courseCode} - {module.schoolSem}
            </option>
          ))}
        </select>
      </div>

      <div className="cfc-meta-card">
        <label className="cfc-field-label" htmlFor="cfc-source-type">
          Source Type
        </label>
        <select
          id="cfc-source-type"
          className="cfc-input"
          value={sourceType}
          onChange={(event) => onSourceTypeChange(event.target.value as SourceType)}
        >
          {SOURCE_TYPE_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      <div className="cfc-meta-card cfc-meta-card-wide">
        <label className="cfc-field-label" htmlFor="cfc-source-title">
          Source Title
        </label>
        <input
          id="cfc-source-title"
          className="cfc-input"
          type="text"
          value={sourceTitle}
          onChange={(event) => onSourceTitleChange(event.target.value)}
          placeholder="Tutorial 5, Assignment 2, Practice Paper 1..."
        />
      </div>
    </div>
  );
}
