import { useMemo } from "react";
import type {
  MindmapEdge as MindmapEdgeData,
  MindmapNode as MindmapNodeData,
  NodePosition,
} from "../types/mindmapTypes";
import { MindmapEdge } from "./MindmapEdge";
import { MindmapNode } from "./MindmapNode";

type MindmapCanvasProps = {
  nodes: MindmapNodeData[];
  edges: MindmapEdgeData[];
  selectedTcIds: number[];
  selectedInsightId: number | null;
  onToggleNode: (tcId: number) => void;
  onOpenInsight: (edge: MindmapEdgeData) => void;
};

export function MindmapCanvas({
  nodes,
  edges,
  selectedTcIds,
  selectedInsightId,
  onToggleNode,
  onOpenInsight,
}: MindmapCanvasProps) {
  const sortedNodes = useMemo(
    () =>
      [...nodes].sort(
        (first, second) =>
          first.topic.localeCompare(second.topic) || first.tcId - second.tcId,
      ),
    [nodes],
  );

  const positions = useMemo(() => buildNodePositions(sortedNodes), [sortedNodes]);
  const nodesById = useMemo(
    () => new Map(sortedNodes.map((node) => [node.tcId, node])),
    [sortedNodes],
  );
  const canvasMinHeight =
    sortedNodes.length > 8
      ? Math.max(600, Math.ceil(sortedNodes.length / 4) * 120)
      : 600;

  if (sortedNodes.length === 0) {
    return (
      <div className="mindmap-canvas mindmap-canvas-empty">
        <p>No active Topical Cheatsheets in this module.</p>
      </div>
    );
  }

  return (
    <div
      className="mindmap-canvas"
      data-testid="mindmap-canvas"
      style={{ minHeight: `${canvasMinHeight}px` }}
    >
      <svg
        aria-label="Saved insight connections"
        className="mindmap-edge-layer"
        preserveAspectRatio="none"
        viewBox="0 0 100 100"
      >
        {edges.map((edge) => {
          const sourcePosition = positions.get(edge.sourceTcId);
          const targetPosition = positions.get(edge.targetTcId);
          const sourceNode = nodesById.get(edge.sourceTcId);
          const targetNode = nodesById.get(edge.targetTcId);

          if (!sourcePosition || !targetPosition || !sourceNode || !targetNode) {
            return null;
          }

          return (
            <MindmapEdge
              edge={edge}
              key={edge.insightId}
              selected={selectedInsightId === edge.insightId}
              sourcePosition={sourcePosition}
              sourceTopic={sourceNode.topic}
              targetPosition={targetPosition}
              targetTopic={targetNode.topic}
              onOpen={onOpenInsight}
            />
          );
        })}
      </svg>

      {sortedNodes.map((node) => (
        <MindmapNode
          key={node.tcId}
          node={node}
          position={positions.get(node.tcId) as NodePosition}
          selected={selectedTcIds.includes(node.tcId)}
          onToggle={onToggleNode}
        />
      ))}
    </div>
  );
}

function buildNodePositions(nodes: MindmapNodeData[]) {
  const positions = new Map<number, NodePosition>();

  if (nodes.length === 1) {
    positions.set(nodes[0].tcId, { x: 50, y: 50 });
    return positions;
  }

  if (nodes.length > 8) {
    const columnCount = Math.min(4, nodes.length);
    const rowCount = Math.ceil(nodes.length / columnCount);

    nodes.forEach((node, index) => {
      const column = index % columnCount;
      const row = Math.floor(index / columnCount);
      positions.set(node.tcId, {
        x: ((column + 0.5) / columnCount) * 100,
        y: ((row + 0.5) / rowCount) * 100,
      });
    });

    return positions;
  }

  const horizontalRadius = nodes.length <= 4 ? 34 : 36;
  const verticalRadius = nodes.length <= 4 ? 30 : 36;

  nodes.forEach((node, index) => {
    const angle = -Math.PI / 2 + (index * Math.PI * 2) / nodes.length;
    positions.set(node.tcId, {
      x: 50 + Math.cos(angle) * horizontalRadius,
      y: 50 + Math.sin(angle) * verticalRadius,
    });
  });

  return positions;
}
