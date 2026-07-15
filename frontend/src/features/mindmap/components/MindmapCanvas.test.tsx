import { useState } from "react";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import type { MindmapEdge, MindmapNode } from "../types/mindmapTypes";
import { updateMindmapSelection } from "../types/mindmapTypes";
import { InsightSheetPanel } from "./InsightSheetPanel";
import { MindmapCanvas } from "./MindmapCanvas";

const nodes: MindmapNode[] = [
  {
    tcId: 1,
    topic: "Trees",
    entryCount: 3,
    updatedAt: "2026-07-15T10:00:00",
  },
  {
    tcId: 2,
    topic: "Graphs",
    entryCount: 2,
    updatedAt: "2026-07-15T10:05:00",
  },
  {
    tcId: 3,
    topic: "Hashing",
    entryCount: 1,
    updatedAt: "2026-07-15T10:10:00",
  },
];

const edge: MindmapEdge = {
  insightId: 20,
  sourceTcId: 1,
  targetTcId: 2,
  status: "READY",
  title: "Trees as structured graphs",
  summary: "Trees are connected acyclic graphs.",
  updatedAt: "2026-07-15T10:20:00Z",
  isRefreshing: false,
};

describe("MindmapCanvas", () => {
  it("renders active nodes and saved insight edges", () => {
    render(
      <MindmapCanvas
        edges={[edge]}
        nodes={nodes}
        selectedInsightId={null}
        selectedTcIds={[]}
        onOpenInsight={() => undefined}
        onToggleNode={() => undefined}
      />,
    );

    expect(
      screen.getByRole("button", { name: "Select Trees" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Select Graphs" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Select Hashing" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", {
        name: "Open insight between Trees and Graphs",
      }),
    ).toBeInTheDocument();
  });

  it("never allows more than two selected nodes", async () => {
    const user = userEvent.setup();
    render(<SelectionHarness />);

    await user.click(screen.getByRole("button", { name: "Select Trees" }));
    await user.click(screen.getByRole("button", { name: "Select Graphs" }));
    await user.click(screen.getByRole("button", { name: "Select Hashing" }));

    expect(screen.getByLabelText("Selected TC ids")).toHaveTextContent("1,2");

    await user.click(screen.getByRole("button", { name: "Deselect Trees" }));
    await user.click(screen.getByRole("button", { name: "Select Hashing" }));

    expect(screen.getByLabelText("Selected TC ids")).toHaveTextContent("2,3");
  });

  it("opens the saved insight panel when an edge is clicked", async () => {
    const user = userEvent.setup();
    render(<EdgeHarness />);

    await user.click(
      screen.getByRole("button", {
        name: "Open insight between Trees and Graphs",
      }),
    );

    expect(
      screen.getByRole("heading", { name: "Trees as structured graphs" }),
    ).toBeInTheDocument();
    expect(
      screen.getByText("Trees are connected acyclic graphs."),
    ).toBeInTheDocument();
  });

  it("renders the empty module state", () => {
    render(
      <MindmapCanvas
        edges={[]}
        nodes={[]}
        selectedInsightId={null}
        selectedTcIds={[]}
        onOpenInsight={() => undefined}
        onToggleNode={() => undefined}
      />,
    );

    expect(
      screen.getByText("No active Topical Cheatsheets in this module."),
    ).toBeInTheDocument();
  });
});

function SelectionHarness() {
  const [selectedTcIds, setSelectedTcIds] = useState<number[]>([]);

  return (
    <>
      <MindmapCanvas
        edges={[edge]}
        nodes={nodes}
        selectedInsightId={null}
        selectedTcIds={selectedTcIds}
        onOpenInsight={() => undefined}
        onToggleNode={(tcId) =>
          setSelectedTcIds((currentIds) =>
            updateMindmapSelection(currentIds, tcId),
          )
        }
      />
      <output aria-label="Selected TC ids">{selectedTcIds.join(",")}</output>
    </>
  );
}

function EdgeHarness() {
  const [openedEdge, setOpenedEdge] = useState<MindmapEdge | null>(null);

  return (
    <>
      <MindmapCanvas
        edges={[edge]}
        nodes={nodes}
        selectedInsightId={openedEdge?.insightId ?? null}
        selectedTcIds={[]}
        onOpenInsight={setOpenedEdge}
        onToggleNode={() => undefined}
      />
      {openedEdge && (
        <InsightSheetPanel
          edge={openedEdge}
          sourceTopic="Trees"
          targetTopic="Graphs"
          onClose={() => setOpenedEdge(null)}
        />
      )}
    </>
  );
}
