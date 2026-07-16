type MindmapNode = {
  tcId: number;
  topic: string;
  entryCount: number;
  updatedAt: string;
};

type MindmapEdge = {
  insightId: number;
  sourceTcId: number;
  targetTcId: number;
  status: "READY" | "REFRESHING";
  title: string;
  summary: string;
  updatedAt: string;
  isRefreshing: boolean;
};

type MindmapResponse = {
  moduleId: number;
  courseCode: string;
  schoolSem: string;
  nodes: MindmapNode[];
  edges: MindmapEdge[];
};

type NodePosition = {
  x: number;
  y: number;
};

function updateMindmapSelection(currentIds: number[], tcId: number) {
  if (currentIds.includes(tcId)) {
    return currentIds.filter((currentId) => currentId !== tcId);
  }

  if (currentIds.length >= 2) {
    return currentIds;
  }

  return [...currentIds, tcId];
}

function edgeMatchesSelection(edge: MindmapEdge, selectedIds: number[]) {
  if (selectedIds.length !== 2) {
    return false;
  }

  return (
    selectedIds.includes(edge.sourceTcId) &&
    selectedIds.includes(edge.targetTcId)
  );
}

export { edgeMatchesSelection, updateMindmapSelection };

export type {
  MindmapEdge,
  MindmapNode,
  MindmapResponse,
  NodePosition,
};
