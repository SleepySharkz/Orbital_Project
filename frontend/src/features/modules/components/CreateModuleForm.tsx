import { useState } from "react";
import { createModuleRequest } from "../api/moduleApi";

type CreateModuleFormProps = {
  token: string;
  onModuleCreated: () => Promise<void>;
};

export function CreateModuleForm({ token, onModuleCreated }: CreateModuleFormProps) {
  const [courseCode, setCourseCode] = useState("");
  const [schoolSem, setSchoolSem] = useState("");
  const [topics, setTopics] = useState([""]);
  const [createModuleError, setCreateModuleError] = useState("");
  const [createModuleSuccess, setCreateModuleSuccess] = useState("");
  const [isCreatingModule, setIsCreatingModule] = useState(false);

  function handleTopicChange(index: number, value: string) {
    const nextTopics = [...topics];
    nextTopics[index] = value;
    setTopics(nextTopics);
  }

  function handleAddTopicField() {
    setTopics([...topics, ""]);
  }

  function handleRemoveTopicField(index: number) {
    if (topics.length === 1) {
      return;
    }

    const nextTopics = topics.filter((_, currentIndex) => currentIndex !== index);
    setTopics(nextTopics);
  }

  async function handleCreateModule(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setCreateModuleError("");
    setCreateModuleSuccess("");

    try {
      setIsCreatingModule(true);
      await createModuleRequest(
        {
          courseCode,
          schoolSem,
          topics,
        },
        token,
      );

      setCourseCode("");
      setSchoolSem("");
      setTopics([""]);
      setCreateModuleSuccess("Module created successfully.");
      await onModuleCreated();
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setCreateModuleError(caughtError.message);
      } else {
        setCreateModuleError("Could not create module.");
      }
    } finally {
      setIsCreatingModule(false);
    }
  }

  return (
    <section className="modules-panel">
      <p className="modules-label">Add Module</p>

      <form className="modules-form" onSubmit={handleCreateModule}>
        <div className="modules-form-grid">
          <div className="modules-field-group">
            <label className="modules-field-label" htmlFor="courseCode">
              Course Code
            </label>
            <input
              className="modules-input"
              id="courseCode"
              type="text"
              placeholder="CS2040"
              value={courseCode}
              onChange={(event) => setCourseCode(event.target.value)}
            />
          </div>

          <div className="modules-field-group">
            <label className="modules-field-label" htmlFor="schoolSem">
              School Semester
            </label>
            <input
              className="modules-input"
              id="schoolSem"
              type="text"
              placeholder="Year1Sem2"
              value={schoolSem}
              onChange={(event) => setSchoolSem(event.target.value)}
            />
          </div>
        </div>

        <div className="modules-field-group">
          <div className="modules-topic-header">
            <label className="modules-field-label">Topics</label>
            <button
              className="modules-add-topic-button"
              type="button"
              onClick={handleAddTopicField}
            >
              Add Topic
            </button>
          </div>

          <div className="modules-topic-list">
            {topics.map((topic, index) => (
              <div className="modules-topic-row" key={index}>
                <input
                  className="modules-input"
                  type="text"
                  placeholder={`Topic ${index + 1}`}
                  value={topic}
                  onChange={(event) => handleTopicChange(index, event.target.value)}
                />
                <button
                  className="modules-remove-topic-button"
                  type="button"
                  onClick={() => handleRemoveTopicField(index)}
                >
                  Remove
                </button>
              </div>
            ))}
          </div>
        </div>

        <button
          className="modules-submit-button"
          type="submit"
          disabled={isCreatingModule}
        >
          {isCreatingModule ? "Creating Module..." : "Create Module"}
        </button>
      </form>

      {createModuleError && (
        <p className="modules-error-message">{createModuleError}</p>
      )}

      {createModuleSuccess && (
        <p className="modules-success-message">{createModuleSuccess}</p>
      )}
    </section>
  );
}
