import { useEffect, useState } from "react";
import type { ChangeEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { fetchModules, fetchModuleTopics, type ModuleSummary } from "../../modules/api/moduleApi";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import { createCFCRequest, type SourceType } from "../api/cfcApi";
import { CFCEntryEditor } from "../components/CFCEntryEditor";
import { CFCMetaForm } from "../components/CFCMetaForm";
import { CFCPageHeader } from "../components/CFCPageHeader";
import { CFCStatusBanners } from "../components/CFCStatusBanners";
import { createEmptyEntry, type EntryDraft } from "../components/cfcFormTypes";
import "../styles/cfcStyles.css";

export function CFCPage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [modules, setModules] = useState<ModuleSummary[]>([]);
  const [moduleTopics, setModuleTopics] = useState<string[]>([]);
  const [isLoadingModules, setIsLoadingModules] = useState(true);
  const [isGenerating, setIsGenerating] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [submissionError, setSubmissionError] = useState("");
  const [submissionSuccess, setSubmissionSuccess] = useState("");
  const [selectedModuleId, setSelectedModuleId] = useState<number | null>(null);
  const [sourceType, setSourceType] = useState<SourceType>("TUTORIAL");
  const [sourceTitle, setSourceTitle] = useState("");
  const [entries, setEntries] = useState<EntryDraft[]>([createEmptyEntry()]);
  const [activeEntryIndex, setActiveEntryIndex] = useState(0);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  useEffect(() => {
    async function loadModulesForCFC() {
      if (!token) {
        setModules([]);
        setIsLoadingModules(false);
        return;
      }

      try {
        setLoadError("");
        setIsLoadingModules(true);
        const fetchedModules = await fetchModules(token);
        setModules(fetchedModules);

        if (fetchedModules.length > 0) {
          setSelectedModuleId((currentModuleId) => currentModuleId ?? fetchedModules[0].id);
        }
      } catch (caughtError) {
        if (caughtError instanceof Error) {
          setLoadError(caughtError.message);
        } else {
          setLoadError("Could not load modules.");
        }
      } finally {
        setIsLoadingModules(false);
      }
    }

    void loadModulesForCFC();
  }, [token]);

  useEffect(() => {
    async function loadSelectedModuleTopics() {
      if (!token || !selectedModuleId) {
        setModuleTopics([]);
        return;
      }

      try {
        const topicsResponse = await fetchModuleTopics(selectedModuleId, token);
        setModuleTopics(topicsResponse.topics);
      } catch (caughtError) {
        if (caughtError instanceof Error) {
          setLoadError(caughtError.message);
        } else {
          setLoadError("Could not load module topics.");
        }
      }
    }

    void loadSelectedModuleTopics();
  }, [selectedModuleId, token]);

  function updateActiveEntry(updatedFields: Partial<EntryDraft>) {
    setEntries((currentEntries) =>
      currentEntries.map((entry, index) =>
        index === activeEntryIndex ? { ...entry, ...updatedFields } : entry,
      ),
    );
  }

  function handleFilesChanged(event: ChangeEvent<HTMLInputElement>) {
    const nextFiles = Array.from(event.target.files ?? []).slice(0, 2);
    updateActiveEntry({ files: nextFiles });
  }

  function handleNextEntry() {
    setSubmissionError("");
    setSubmissionSuccess("");

    if (activeEntryIndex === entries.length - 1) {
      setEntries((currentEntries) => [...currentEntries, createEmptyEntry()]);
    }

    setActiveEntryIndex((currentIndex) => currentIndex + 1);
  }

  function handlePreviousEntry() {
    setSubmissionError("");
    setSubmissionSuccess("");
    setActiveEntryIndex((currentIndex) => Math.max(0, currentIndex - 1));
  }

  async function handleGenerate() {
    if (!token || !selectedModuleId) {
      setSubmissionError("Select a module before generating.");
      setSubmissionSuccess("");
      return;
    }

    try {
      setIsGenerating(true);
      setSubmissionError("");
      setSubmissionSuccess("");

      const filledEntries = entries
        .map((entry) => ({
          topic: entry.topic.trim(),
          questionText: entry.questionText.trim(),
          roughNote: entry.roughNote.trim(),
          files: entry.files,
        }))
        .filter((entry) => entry.topic || entry.questionText || entry.roughNote || entry.files.length > 0);

      const payload = {
        moduleId: selectedModuleId,
        flashcardHeader: {
          sourceType,
          sourceTitle: sourceTitle.trim(),
        },
        items: filledEntries.map((entry, index) => ({
          itemId: index + 1,
          topic: entry.topic,
          questionText: entry.questionText ? entry.questionText : null,
          imageKeys: entry.files.map((_, fileIndex) => `item_${index + 1}_img_${fileIndex + 1}`),
          roughNote: entry.roughNote,
        })),
      };

      await createCFCRequest(
        payload,
        filledEntries.map((entry) => entry.files),
        token,
      );

      setSubmissionSuccess("Flashcard generated successfully.");
      setSubmissionError("");
    } catch (caughtError) {
      if (caughtError instanceof Error) {
        setSubmissionError(caughtError.message);
      } else {
        setSubmissionError("Could not generate flashcard.");
      }
      setSubmissionSuccess("");
    } finally {
      setIsGenerating(false);
    }
  }

  if (!user || !token) {
    return null;
  }

  const activeEntry = entries[activeEntryIndex];
  const draftEntryCount = entries.filter(
    (entry) => entry.topic || entry.questionText || entry.roughNote || entry.files.length > 0,
  ).length;

  return (
    <div className="cfc-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="cfc-main">
        <CFCPageHeader />

        <CFCStatusBanners
          loadError={loadError}
          submissionError={submissionError}
          submissionSuccess={submissionSuccess}
        />

        <CFCMetaForm
          modules={modules}
          isLoadingModules={isLoadingModules}
          selectedModuleId={selectedModuleId}
          sourceType={sourceType}
          sourceTitle={sourceTitle}
          onModuleChange={setSelectedModuleId}
          onSourceTypeChange={setSourceType}
          onSourceTitleChange={setSourceTitle}
          resetDraftState={() => {
            setEntries([createEmptyEntry()]);
            setActiveEntryIndex(0);
            setSubmissionError("");
            setSubmissionSuccess("");
          }}
        />

        <CFCEntryEditor
          activeEntry={activeEntry}
          activeEntryIndex={activeEntryIndex}
          draftEntryCount={draftEntryCount}
          moduleTopics={moduleTopics}
          isLoadingModules={isLoadingModules}
          isGenerating={isGenerating}
          modulesCount={modules.length}
          submissionError={submissionError}
          onFilesChanged={handleFilesChanged}
          onEntryChange={updateActiveEntry}
          onPreviousEntry={handlePreviousEntry}
          onNextEntry={handleNextEntry}
          onGenerate={() => void handleGenerate()}
        />
      </main>
    </div>
  );
}
