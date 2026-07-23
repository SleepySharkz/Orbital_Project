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
  status: "READY" | "REFRESHING" | "REFRESH_FAILED";
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

type MindmapInsightStatus =
  | "GENERATING"
  | "GENERATION_FAILED"
  | "READY"
  | "NO_USEFUL_LINK"
  | "REFRESHING"
  | "REFRESH_FAILED";

type MindmapInsightPoint = {
  heading: string;
  explanation: string;
  sourceTopicAReferences: string | null;
  sourceTopicBReferences: string | null;
  displayOrder: number;
};

type MindmapInsightDetail = {
  insightId: number;
  moduleId: number;
  sourceTcId: number;
  targetTcId: number;
  topicA: string;
  topicB: string;
  status: MindmapInsightStatus;
  title: string | null;
  summary: string | null;
  points: MindmapInsightPoint[];
  rejectionReason: string | null;
  createdAt: string;
  updatedAt: string;
  generatedAt: string | null;
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

function canonicalPairKey(tcIds: number[]) {
  return [...tcIds].sort((first, second) => first - second).join(":");
}

function detailToReadyEdge(detail: MindmapInsightDetail): MindmapEdge | null {
  if (detail.status !== "READY" || !detail.title || !detail.summary) {
    return null;
  }

  return {
    insightId: detail.insightId,
    sourceTcId: detail.sourceTcId,
    targetTcId: detail.targetTcId,
    status: "READY",
    title: detail.title,
    summary: detail.summary,
    updatedAt: detail.updatedAt,
    isRefreshing: false,
  };
}

export {
  canonicalPairKey,
  detailToReadyEdge,
  edgeMatchesSelection,
  updateMindmapSelection,
};

export type {
  MindmapEdge,
  MindmapInsightDetail,
  MindmapInsightPoint,
  MindmapInsightStatus,
  MindmapNode,
  MindmapResponse,
  NodePosition,
};
