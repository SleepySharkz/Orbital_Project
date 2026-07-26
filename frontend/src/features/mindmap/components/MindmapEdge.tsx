import type { KeyboardEvent } from "react";
import type {
  MindmapEdge as MindmapEdgeData,
  NodePosition,
} from "../types/mindmapTypes";

type MindmapEdgeProps = {
  edge: MindmapEdgeData;
  sourcePosition: NodePosition;
  targetPosition: NodePosition;
  sourceTopic: string;
  targetTopic: string;
  selected: boolean;
  onOpen: (edge: MindmapEdgeData) => void;
};

export function MindmapEdge({
  edge,
  sourcePosition,
  targetPosition,
  sourceTopic,
  targetTopic,
  selected,
  onOpen,
}: MindmapEdgeProps) {
  function handleKeyDown(event: KeyboardEvent<SVGLineElement>) {
    if (event.key === "Enter" || event.key === " ") {
      event.preventDefault();
      onOpen(edge);
    }
  }

  const className = [
    "mindmap-edge-line",
    edge.isRefreshing ? "mindmap-edge-line-refreshing" : "",
    selected ? "mindmap-edge-line-selected" : "",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <g>
      <line
        aria-hidden="true"
        className="mindmap-edge-hit-area"
        x1={sourcePosition.x}
        x2={targetPosition.x}
        y1={sourcePosition.y}
        y2={targetPosition.y}
        onClick={() => onOpen(edge)}
      />
      <line
        aria-label={`Open insight between ${sourceTopic} and ${targetTopic}`}
        className={className}
        role="button"
        tabIndex={0}
        vectorEffect="non-scaling-stroke"
        x1={sourcePosition.x}
        x2={targetPosition.x}
        y1={sourcePosition.y}
        y2={targetPosition.y}
        onClick={() => onOpen(edge)}
        onKeyDown={handleKeyDown}
      />
    </g>
  );
}
