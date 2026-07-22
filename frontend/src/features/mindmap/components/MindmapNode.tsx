import type {
  MindmapNode as MindmapNodeData,
  NodePosition,
} from "../types/mindmapTypes";

type MindmapNodeProps = {
  node: MindmapNodeData;
  position: NodePosition;
  selected: boolean;
  onToggle: (tcId: number) => void;
};

export function MindmapNode({
  node,
  position,
  selected,
  onToggle,
}: MindmapNodeProps) {
  return (
    <button
      aria-label={`${selected ? "Deselect" : "Select"} ${node.topic}`}
      aria-pressed={selected}
      className={
        selected ? "mindmap-node mindmap-node-selected" : "mindmap-node"
      }
      style={{ left: `${position.x}%`, top: `${position.y}%` }}
      type="button"
      onClick={() => onToggle(node.tcId)}
    >
      <strong>{node.topic}</strong>
      <span>
        {node.entryCount} {node.entryCount === 1 ? "entry" : "entries"}
      </span>
    </button>
  );
}
