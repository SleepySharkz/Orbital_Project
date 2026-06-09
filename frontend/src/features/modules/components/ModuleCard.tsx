import { useState, type FormEvent } from "react";
import {
  fetchModuleTopics,
  updateModuleRequest,
  type ModuleSummary,
  type ModuleTopicsResponse,
} from "../api/moduleApi";

type ModuleCardProps = {
  module: ModuleSummary;
  token: string;
  onModuleUpdated: () => Promise<void>;
};

export function ModuleCard({ module, token, onModuleUpdated }: ModuleCardProps) {
  const [expanded, setExpanded] = useState(false);
  const [topicsData, setTopicsData] = useState<ModuleTopicsResponse | null>(null);
  const [topicsError, setTopicsError] = useState("");
  const [isTopicsLoading, setIsTopicsLoading] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [editCourseCode, setEditCourseCode] = useState(module.courseCode);
  const [editSchoolSem, setEditSchoolSem] = useState(module.schoolSem);
  const [editTopics, setEditTopics] = useState<string[]>([""]);
  const [editError, setEditError] = useState("");
  const [editSuccess, setEditSuccess] = useState("");
  const [isSaving, setIsSaving] = useState(false);

  async function loadTopics() {
    if (topicsData) {
      return topicsData;
    }

    setTopicsError("");
    setIsTopicsLoading(true);

    try {
      const moduleTopics = await fetchModuleTopics(module.id, token);
      setTopicsData(moduleTopics);
      return moduleTopics;
    } finally {
      setIsTopicsLoading(false);
    }
  }

  async function handleToggleTopics() {
    if (isEditing) {
      return;
    }

    if (expanded) {
      setExpanded(false);
      setTopicsError("");
      return;
    }

    setExpanded(true);
    setTopicsError("");

    try {
      await loadTopics();
    } catch (caughtError) {
      setExpanded(false);

      if (caughtError instanceof Error) {
        setTopicsError(caughtError.message);
      } else {
        setTopicsError("Could not load module topics.");
      }
    }
  }

  async function handleStartEditing() {
    setEditError("");
    setEditSuccess("");

    try {
      const moduleTopics = await loadTopics();
      setEditCourseCode(module.courseCode);
      setEditSchoolSem(module.schoolSem);
      setEditTopics(moduleTopics.topics.length > 0 ? moduleTopics.topics : [""]);
      setExpanded(true);
      setIsEditing(true);
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setEditError(caughtError.message);
      } else {
        setEditError("Could not load module for editing.");
      }
    }
  }

  function handleCancelEditing() {
    setIsEditing(false);
    setEditError("");
  }

  function handleEditTopicChange(index: number, value: string) {
    const nextTopics = [...editTopics];
    nextTopics[index] = value;
    setEditTopics(nextTopics);
  }

  function handleAddEditTopicField() {
    setEditTopics([...editTopics, ""]);
  }

  function handleRemoveEditTopicField(index: number) {
    if (editTopics.length === 1) {
      return;
    }

    setEditTopics(editTopics.filter((_, currentIndex) => currentIndex !== index));
  }

  async function handleSaveModule(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setEditError("");
    setEditSuccess("");

    const trimmedCourseCode = editCourseCode.trim();
    const trimmedSchoolSem = editSchoolSem.trim();
    const trimmedTopics = editTopics.map((topic) => topic.trim());

    if (!trimmedCourseCode) {
      setEditError("Course code is required.");
      return;
    }

    if (!trimmedSchoolSem) {
      setEditError("School semester is required.");
      return;
    }

    if (trimmedTopics.some((topic) => !topic)) {
      setEditError("Every topic field must be filled.");
      return;
    }

    try {
      setIsSaving(true);
      const updatedModule = await updateModuleRequest(
        module.id,
        {
          courseCode: trimmedCourseCode,
          schoolSem: trimmedSchoolSem,
          topics: trimmedTopics,
        },
        token,
      );

      setTopicsData({
        moduleId: updatedModule.id,
        courseCode: updatedModule.courseCode,
        topics: updatedModule.topics,
      });
      setEditCourseCode(updatedModule.courseCode);
      setEditSchoolSem(updatedModule.schoolSem);
      setEditTopics(updatedModule.topics);
      setIsEditing(false);
      setExpanded(true);
      setEditSuccess("Module updated successfully.");
      await onModuleUpdated();
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setEditError(caughtError.message);
      } else {
        setEditError("Could not update module.");
      }
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <article className={expanded ? "modules-card modules-card-expanded" : "modules-card"}>
      <div className="modules-card-header">
        <button
          className="modules-card-toggle"
          type="button"
          aria-expanded={expanded}
          disabled={isEditing}
          onClick={() => void handleToggleTopics()}
        >
          <div className="modules-card-copy">
            <p className="modules-card-code">{module.courseCode}</p>
            <p className="modules-card-sem">{module.schoolSem}</p>
          </div>

          <span className="modules-card-indicator" aria-hidden="true">
            {expanded ? "-" : "+"}
          </span>
        </button>

        <button
          className={isEditing ? "modules-cancel-button" : "modules-edit-button"}
          type="button"
          onClick={isEditing ? handleCancelEditing : () => void handleStartEditing()}
        >
          {isEditing ? "Cancel" : "Edit"}
        </button>
      </div>

      {expanded && (
        <div className="modules-topics-dropdown">
          {isEditing ? (
            <form className="modules-form modules-edit-form" onSubmit={handleSaveModule}>
              <div className="modules-form-grid">
                <div className="modules-field-group">
                  <label className="modules-field-label" htmlFor={`courseCode-${module.id}`}>
                    Course Code
                  </label>
                  <input
                    className="modules-input"
                    id={`courseCode-${module.id}`}
                    type="text"
                    value={editCourseCode}
                    onChange={(event) => setEditCourseCode(event.target.value)}
                  />
                </div>

                <div className="modules-field-group">
                  <label className="modules-field-label" htmlFor={`schoolSem-${module.id}`}>
                    School Semester
                  </label>
                  <input
                    className="modules-input"
                    id={`schoolSem-${module.id}`}
                    type="text"
                    value={editSchoolSem}
                    onChange={(event) => setEditSchoolSem(event.target.value)}
                  />
                </div>
              </div>

              <div className="modules-field-group">
                <div className="modules-topic-header">
                  <label className="modules-field-label">Topics</label>
                  <button
                    className="modules-add-topic-button"
                    type="button"
                    onClick={handleAddEditTopicField}
                  >
                    Add Topic
                  </button>
                </div>

                <div className="modules-topic-list">
                  {editTopics.map((topic, index) => (
                    <div className="modules-topic-row" key={index}>
                      <input
                        className="modules-input"
                        type="text"
                        value={topic}
                        onChange={(event) => handleEditTopicChange(index, event.target.value)}
                      />
                      <button
                        className="modules-remove-topic-button"
                        type="button"
                        onClick={() => handleRemoveEditTopicField(index)}
                      >
                        Remove
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              <button
                className="modules-save-button"
                type="submit"
                disabled={isSaving}
              >
                {isSaving ? "Saving..." : "Save Changes"}
              </button>

              {editError && (
                <p className="modules-error-message">{editError}</p>
              )}
            </form>
          ) : isTopicsLoading ? (
            <p className="modules-topics-message">Loading topics...</p>
          ) : topicsData && (
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

      {!isEditing && editError && (
        <p className="modules-error-message">{editError}</p>
      )}

      {editSuccess && (
        <p className="modules-success-message modules-card-message">{editSuccess}</p>
      )}

      {topicsError && (
        <p className="modules-error-message">{topicsError}</p>
      )}
    </article>
  );
}
