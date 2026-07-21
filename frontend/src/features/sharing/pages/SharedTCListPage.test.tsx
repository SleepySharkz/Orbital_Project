import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  fetchSharedTCById,
  fetchSharedTCs,
  mergeSharedTC,
} from "../api/tcSharingApi";
import type {
  SharedTCDetail,
  SharedTCSummary,
} from "../types/tcSharingTypes";
import { SharedTCListPage } from "./SharedTCListPage";

vi.mock("../../auth/context/useAuth", () => ({
  useAuth: () => ({
    user: { id: 7, username: "bob", email: "bob@example.com" },
    token: "token",
    logout: vi.fn(),
  }),
}));

vi.mock("../../modules/components/ModulesSidebar", () => ({
  ModulesSidebar: () => <aside aria-label="Sidebar" />,
}));

vi.mock("../api/tcSharingApi", () => ({
  fetchSharedTCById: vi.fn(),
  fetchSharedTCs: vi.fn(),
  mergeSharedTC: vi.fn(),
}));

const summary: SharedTCSummary = {
  id: 31,
  moduleId: 8,
  courseCode: "CS2040S",
  schoolSem: "Year1Sem2",
  topic: "Trees",
  entryCount: 1,
  sharedByUserId: 4,
  sharedByUsername: "alice",
  acceptedAt: "2026-07-20T10:00:00Z",
};

const detail: SharedTCDetail = {
  ...summary,
  status: "ACTIVE",
  matchingOwnedTcId: 17,
  canMerge: true,
  mergeBlockingReason: null,
  mergedIntoTcId: null,
  mergedAt: null,
  entries: [{
    id: 401,
    sourceEntryId: 801,
    flashcardQuestion: "What is a tree?",
    flashcardNoteContent: "A connected acyclic graph.",
    questionText: "Define a tree.",
    roughNote: "Connected, no cycles",
    sourceType: "TUTORIAL",
    sourceTitle: "Tutorial 1",
    sourceEntryCreatedAt: "2026-07-18T10:00:00",
  }],
};

describe("SharedTCListPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.mocked(fetchSharedTCs).mockResolvedValue([summary]);
    vi.mocked(fetchSharedTCById).mockResolvedValue(detail);
  });

  it("merges an eligible shared TC and removes it from the active list", async () => {
    const user = userEvent.setup();
    vi.mocked(mergeSharedTC).mockResolvedValue({
      ownedTcId: 17,
      sharedTcId: 31,
      mergedEntryCount: 1,
      status: "MERGED",
      mergedAt: "2026-07-20T10:15:00Z",
    });
    renderPage();

    await user.click(await screen.findByRole("button", { name: "Merge into my TC" }));

    expect(mergeSharedTC).toHaveBeenCalledWith(31, "token");
    expect(await screen.findByText("Shared TC merged")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Open merged TC" })).toBeInTheDocument();
    expect(screen.getByText("No shared TCs yet. Accept a compatible sharing request first."))
      .toBeInTheDocument();
  });

  it("hides merge when no matching owned TC exists", async () => {
    vi.mocked(fetchSharedTCById).mockResolvedValue({
      ...detail,
      matchingOwnedTcId: null,
      canMerge: false,
      mergeBlockingReason: "Create your own TC for this module and topic before merging.",
    });
    renderPage();

    expect(await screen.findByText(
      "Create your own TC for this module and topic before merging.",
    )).toBeInTheDocument();
    await waitFor(() => {
      expect(screen.queryByRole("button", { name: "Merge into my TC" }))
        .not.toBeInTheDocument();
    });
  });
});

function renderPage() {
  render(
    <MemoryRouter initialEntries={["/shared-tcs/31"]}>
      <Routes>
        <Route path="/shared-tcs" element={<SharedTCListPage />} />
        <Route path="/shared-tcs/:sharedTcId" element={<SharedTCListPage />} />
        <Route path="/topic-sheets/:tcId" element={<p>Owned TC</p>} />
      </Routes>
    </MemoryRouter>,
  );
}
