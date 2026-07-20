import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/useAuth";
import { fetchModules, type ModuleSummary } from "../../modules/api/moduleApi";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import {
  discoverInsight,
  fetchInsightDetail,
  fetchModuleMindmap,
  pollInsightUntilSettled,
} from "../api/mindmapApi";
import { InsightSheetPanel } from "../components/InsightSheetPanel";
import { MindmapCanvas } from "../components/MindmapCanvas";
import { ModuleMindmapSelector } from "../components/ModuleMindmapSelector";
import {
  canonicalPairKey,
  detailToReadyEdge,
  edgeMatchesSelection,
  updateMindmapSelection,
  type MindmapEdge,
  type MindmapInsightDetail,
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
  const [selectedInsight, setSelectedInsight] =
    useState<MindmapInsightDetail | null>(null);
  const [discoveringPairKey, setDiscoveringPairKey] = useState<string | null>(
    null,
  );
  const [isModulesLoading, setIsModulesLoading] = useState(true);
  const [isMindmapLoading, setIsMindmapLoading] = useState(false);
  const [modulesError, setModulesError] = useState("");
  const [mindmapError, setMindmapError] = useState("");
  const [selectionNotice, setSelectionNotice] = useState("");
  const activeOperationRef = useRef(0);
  const inFlightPairsRef = useRef(new Set<string>());

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
    activeOperationRef.current += 1;

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
        setSelectedInsight(null);
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
  const selectedPairKey =
    selectedTcIds.length === 2 ? canonicalPairKey(selectedTcIds) : null;
  const isSelectedPairDiscovering =
    selectedPairKey !== null && discoveringPairKey === selectedPairKey;

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  function handleModuleChange(moduleId: number) {
    activeOperationRef.current += 1;
    setSelectedTcIds([]);
    setSelectedInsight(null);
    setSelectionNotice("");
    setSelectedModuleId(moduleId);
  }

  function handleToggleNode(tcId: number) {
    activeOperationRef.current += 1;
    setSelectedTcIds((currentIds) => updateMindmapSelection(currentIds, tcId));
    setSelectedInsight(null);
    setSelectionNotice("");
  }

  async function handleOpenInsight(edge: MindmapEdge) {
    if (!token) {
      return;
    }

    const operationId = ++activeOperationRef.current;
    setSelectedTcIds([edge.sourceTcId, edge.targetTcId]);
    setSelectedInsight(null);
    setSelectionNotice("Loading saved insight...");

    try {
      const detail = await fetchInsightDetail(edge.insightId, token);
      if (operationId !== activeOperationRef.current) {
        return;
      }
      setSelectedInsight(detail);
      setSelectionNotice("");
    } catch (caughtError) {
      if (operationId === activeOperationRef.current) {
        setSelectionNotice(
          toErrorMessage(caughtError, "Could not load saved insight."),
        );
      }
    }
  }

  async function handleDiscoverInsight() {
    if (
      !mindmap ||
      !token ||
      selectedModuleId === "" ||
      selectedTcIds.length !== 2
    ) {
      return;
    }

    const selectedIds = [...selectedTcIds];
    const existingEdge = mindmap.edges.find((edge) =>
      edgeMatchesSelection(edge, selectedIds),
    );

    if (existingEdge) {
      await handleOpenInsight(existingEdge);
      return;
    }

    const pairKey = canonicalPairKey(selectedIds);
    if (inFlightPairsRef.current.has(pairKey)) {
      return;
    }

    inFlightPairsRef.current.add(pairKey);
    const operationId = ++activeOperationRef.current;
    setDiscoveringPairKey(pairKey);
    setSelectedInsight(null);
    setSelectionNotice("");

    try {
      let detail = await discoverInsight(selectedModuleId, selectedIds, token);
      if (operationId !== activeOperationRef.current) {
        return;
      }

      setSelectedInsight(detail);
      if (detail.status === "GENERATING") {
        detail = await pollInsightUntilSettled(detail.insightId, token);
      }

      if (operationId !== activeOperationRef.current) {
        return;
      }
      applyDiscoveryResult(detail);
    } catch (caughtError) {
      if (operationId === activeOperationRef.current) {
        setSelectionNotice(
          toErrorMessage(caughtError, "Could not discover insight."),
        );
      }
    } finally {
      inFlightPairsRef.current.delete(pairKey);
      setDiscoveringPairKey((currentPairKey) =>
        currentPairKey === pairKey ? null : currentPairKey,
      );
    }
  }

  function applyDiscoveryResult(detail: MindmapInsightDetail) {
    setSelectedInsight(detail);
    const readyEdge = detailToReadyEdge(detail);

    if (readyEdge) {
      setMindmap((currentMindmap) => {
        if (!currentMindmap) {
          return currentMindmap;
        }
        return {
          ...currentMindmap,
          edges: [
            ...currentMindmap.edges.filter(
              (edge) => edge.insightId !== readyEdge.insightId,
            ),
            readyEdge,
          ],
        };
      });
      setSelectionNotice("");
      return;
    }

    if (detail.status === "NO_USEFUL_LINK") {
      setSelectionNotice("No useful link was found for this pair.");
      return;
    }

    if (detail.status === "GENERATION_FAILED") {
      setSelectionNotice("Insight generation failed. Please try again.");
    }
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
              Discover and revisit connections across your active Topical
              Cheatsheets.
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
              selectedInsight
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
                  disabled={
                    selectedTcIds.length !== 2 ||
                    isMindmapLoading ||
                    isSelectedPairDiscovering
                  }
                  type="button"
                  onClick={() => void handleDiscoverInsight()}
                >
                  {isSelectedPairDiscovering
                    ? "Discovering..."
                    : "Discover Insight"}
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
                  selectedInsightId={selectedInsight?.insightId ?? null}
                  selectedTcIds={selectedTcIds}
                  onOpenInsight={(edge) => void handleOpenInsight(edge)}
                  onToggleNode={handleToggleNode}
                />
              ) : null}
            </div>

            {selectedInsight && (
              <InsightSheetPanel
                insight={selectedInsight}
                onClose={() => setSelectedInsight(null)}
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
