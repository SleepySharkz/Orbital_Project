import type { CFCCollectionViewMode } from "./cfcCollectionTypes";

type CFCCollectionControlsProps = {
  viewMode: CFCCollectionViewMode;
  onViewModeChange: (viewMode: CFCCollectionViewMode) => void;
};

const viewModes: Array<{ label: string; value: CFCCollectionViewMode }> = [
  { label: "Date Created", value: "date" },
  { label: "Modules", value: "module" },
  { label: "Topics", value: "topic" },
];

export function CFCCollectionControls({
  viewMode,
  onViewModeChange,
}: CFCCollectionControlsProps) {
  return (
    <div className="cfc-collection-controls" aria-label="CFC view mode">
      {viewModes.map((mode) => (
        <button
          className={
            mode.value === viewMode
              ? "cfc-view-mode-button cfc-view-mode-button-active"
              : "cfc-view-mode-button"
          }
          key={mode.value}
          type="button"
          onClick={() => onViewModeChange(mode.value)}
        >
          {mode.label}
        </button>
      ))}
    </div>
  );
}
