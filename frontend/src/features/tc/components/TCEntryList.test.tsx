import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it } from "vitest";
import type { TcEntryView } from "../api/tcApi";
import { TCEntryList } from "./TCEntryList";

describe("TCEntryList", () => {
  it("shows private-share source metadata for a merged entry", async () => {
    const user = userEvent.setup();
    const entry: TcEntryView = {
      entryId: 1,
      topic: "Trees",
      flashcardQuestion: "What is a tree?",
      flashcardNoteContent: "A connected acyclic graph.",
      questionText: "Define a tree.",
      roughNote: "Connected, no cycles",
      createdAt: "2026-07-20T10:15:00",
      origin: "MERGED_SHARED",
      sourceOwnerUsername: "alice",
      sourceTypeAtShare: "TUTORIAL",
      sourceTitleAtShare: "Tutorial 1",
      sourceSharedTcId: 31,
      sourceSharedEntryId: 401,
      sourceEntryCreatedAt: "2026-07-18T10:00:00",
      mergedAt: "2026-07-20T10:15:00Z",
    };

    render(<TCEntryList entries={[entry]} />);
    await user.click(screen.getByText("Show original source material"));

    expect(screen.getByText("Shared by alice", { exact: false })).toBeInTheDocument();
    expect(screen.getByText("Tutorial - Tutorial 1", { exact: false })).toBeInTheDocument();
  });
});
