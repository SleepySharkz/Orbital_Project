import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { fetchModules, type ModuleSummary } from "../../modules/api/moduleApi";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import { fetchModuleMindmap } from "../api/mindmapApi";
import { InsightSheetPanel } from "../components/InsightSheetPanel";
import { MindmapCanvas } from "../components/MindmapCanvas";
import { ModuleMindmapSelector } from "../components/ModuleMindmapSelector";
import {
  edgeMatchesSelection,
  updateMindmapSelection,
  type MindmapEdge,
  type MindmapResponse,
} from "../types/mindmapTypes";
import "../styles/mindmapStyles.css";

export function MindmapPage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [modules, setModules] = useState<ModuleSummary[]>([]);
  const [selectedModuleId, setSelectedModuleId] = useState<number | "">("");
  const [mindmap, setMindmap] = useState<MindmapResponse | null>(null);
  const [selectedTcIds, setSelectedTcIds] = useState<number[]>([]);
  const [selectedEdge, setSelectedEdge] = useState<MindmapEdge | null>(null);
  const [isModulesLoading, setIsModulesLoading] = useState(true);
  const [isMindmapLoading, setIsMindmapLoading] = useState(false);
  const [modulesError, setModulesError] = useState("");
  const [mindmapError, setMindmapError] = useState("");
  const [selectionNotice, setSelectionNotice] = useState("");

  useEffect(() => {
    let ignore = false;

    async function loadModules() {
      if (!token) {
        if (!ignore) {
          setModules([]);
          setSelectedModuleId("");
          setIsModulesLoading(false);
        }
        return;
      }

      try {
        setModulesError("");
        setIsModulesLoading(true);
        const fetchedModules = await fetchModules(token);

        if (ignore) {
          return;
        }

        setModules(fetchedModules);
        setSelectedModuleId((currentModuleId) => {
          if (
            typeof currentModuleId === "number" &&
            fetchedModules.some((module) => module.id === currentModuleId)
          ) {
            return currentModuleId;
          }

          return fetchedModules[0]?.id ?? "";
        });
      } catch (caughtError) {
        if (!ignore) {
          setModules([]);
          setSelectedModuleId("");
          setModulesError(toErrorMessage(caughtError, "Could not load modules."));
        }
      } finally {
        if (!ignore) {
          setIsModulesLoading(false);
        }
      }
    }

    void loadModules();

    return () => {
      ignore = true;
    };
  }, [token]);

  useEffect(() => {
    let ignore = false;

    async function loadMindmap() {
      if (!token || selectedModuleId === "") {
        if (!ignore) {
          setMindmap(null);
          setIsMindmapLoading(false);
        }
        return;
      }

      try {
        setMindmapError("");
        setIsMindmapLoading(true);
        setSelectedTcIds([]);
        setSelectedEdge(null);
        setSelectionNotice("");
        const response = await fetchModuleMindmap(selectedModuleId, token);

        if (!ignore) {
          setMindmap(response);
        }
      } catch (caughtError) {
        if (!ignore) {
          setMindmap(null);
          setMindmapError(toErrorMessage(caughtError, "Could not load mindmap."));
        }
      } finally {
        if (!ignore) {
          setIsMindmapLoading(false);
        }
      }
    }

    void loadMindmap();

    return () => {
      ignore = true;
    };
  }, [selectedModuleId, token]);

  const nodeTopicsById = useMemo(
    () =>
      new Map<number, string>(
        mindmap?.nodes.map((node) => [node.tcId, node.topic] as const) ?? [],
      ),
    [mindmap],
  );

  const selectedTopics = selectedTcIds
    .map((tcId) => nodeTopicsById.get(tcId))
    .filter((topic): topic is string => Boolean(topic));

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  function handleModuleChange(moduleId: number) {
    setSelectedTcIds([]);
    setSelectedEdge(null);
    setSelectionNotice("");
    setSelectedModuleId(moduleId);
  }

  function handleToggleNode(tcId: number) {
    setSelectedTcIds((currentIds) =>
      updateMindmapSelection(currentIds, tcId),
    );
    setSelectedEdge(null);
    setSelectionNotice("");
  }

  function handleOpenInsight(edge: MindmapEdge) {
    setSelectedTcIds([edge.sourceTcId, edge.targetTcId]);
    setSelectedEdge(edge);
    setSelectionNotice("");
  }

  function handleDiscoverInsight() {
    if (!mindmap || selectedTcIds.length !== 2) {
      return;
    }

    const existingEdge = mindmap.edges.find((edge) =>
      edgeMatchesSelection(edge, selectedTcIds),
    );

    if (existingEdge) {
      handleOpenInsight(existingEdge);
      return;
    }

    setSelectedEdge(null);
    setSelectionNotice("No saved insight exists for this pair.");
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="modules-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="modules-main mindmap-main">
        <header className="mindmap-header">
          <div>
            <p className="modules-eyebrow">Mindmap</p>
            <h1 className="modules-title">Module Mindmap</h1>
            <p className="modules-subtitle">
              Saved connections across your active Topical Cheatsheets.
            </p>
          </div>

          <ModuleMindmapSelector
            disabled={isModulesLoading}
            modules={modules}
            selectedModuleId={selectedModuleId}
            onChange={handleModuleChange}
          />
        </header>

        {modulesError && (
          <p className="mindmap-banner mindmap-banner-error">{modulesError}</p>
        )}

        {isModulesLoading ? (
          <section className="mindmap-state-panel">
            <p>Loading modules...</p>
          </section>
        ) : modules.length === 0 ? (
          <section className="mindmap-state-panel">
            <p>Create a module before opening a mindmap.</p>
          </section>
        ) : (
          <section
            className={
              selectedEdge
                ? "mindmap-workspace mindmap-workspace-with-panel"
                : "mindmap-workspace"
            }
          >
            <div className="mindmap-graph-column">
              <div className="mindmap-toolbar">
                <div className="mindmap-selection-summary">
                  <span>{selectedTcIds.length}/2 selected</span>
                  <div className="mindmap-selected-topics">
                    {selectedTopics.map((topic) => (
                      <span key={topic}>{topic}</span>
                    ))}
                  </div>
                </div>

                <button
                  className="mindmap-discover-button"
                  disabled={selectedTcIds.length !== 2 || isMindmapLoading}
                  type="button"
                  onClick={handleDiscoverInsight}
                >
                  Discover Insight
                </button>
              </div>

              {selectionNotice && (
                <p aria-live="polite" className="mindmap-banner">
                  {selectionNotice}
                </p>
              )}

              {mindmapError && (
                <p className="mindmap-banner mindmap-banner-error">
                  {mindmapError}
                </p>
              )}

              {isMindmapLoading ? (
                <div className="mindmap-canvas mindmap-canvas-empty">
                  <p>Loading mindmap...</p>
                </div>
              ) : mindmap ? (
                <MindmapCanvas
                  edges={mindmap.edges}
                  nodes={mindmap.nodes}
                  selectedInsightId={selectedEdge?.insightId ?? null}
                  selectedTcIds={selectedTcIds}
                  onOpenInsight={handleOpenInsight}
                  onToggleNode={handleToggleNode}
                />
              ) : null}
            </div>

            {selectedEdge && (
              <InsightSheetPanel
                edge={selectedEdge}
                sourceTopic={
                  nodeTopicsById.get(selectedEdge.sourceTcId) ?? "Topic A"
                }
                targetTopic={
                  nodeTopicsById.get(selectedEdge.targetTcId) ?? "Topic B"
                }
                onClose={() => setSelectedEdge(null)}
              />
            )}
          </section>
        )}
      </main>
    </div>
  );
}

function toErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}
