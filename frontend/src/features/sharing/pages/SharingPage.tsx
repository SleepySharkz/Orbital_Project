import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { fetchFriends } from "../../friends/api/friendsApi";
import type { FriendSummary } from "../../friends/types/friendTypes";
import { fetchModules } from "../../modules/api/moduleApi";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import "../../friends/styles/friendsStyles.css";
import { fetchTFCsForModule, type TfcSummary } from "../../tfc/api/tfcApi";
import {
  fetchIncomingTFCSharingRequests,
  fetchOutgoingTFCSharingRequests,
  fetchTFCSharingRequestDetail,
  sendTFCSharingRequest,
} from "../api/tfcSharingApi";
import { TFCSharingDetailModal } from "../components/TFCSharingDetailModal";
import { TFCSharingRequestsPanel } from "../components/TFCSharingRequestsPanel";
import type {
  TFCSharingRequestDetail,
  TFCSharingRequestSummary,
} from "../types/tfcSharingTypes";
import "../styles/sharingStyles.css";

export function SharingPage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [friends, setFriends] = useState<FriendSummary[]>([]);
  const [tfcs, setTFCs] = useState<TfcSummary[]>([]);
  const [incomingRequests, setIncomingRequests] = useState<
    TFCSharingRequestSummary[]
  >([]);
  const [outgoingRequests, setOutgoingRequests] = useState<
    TFCSharingRequestSummary[]
  >([]);
  const [selectedFriendId, setSelectedFriendId] = useState<number | "">("");
  const [selectedTFCIds, setSelectedTFCIds] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [detailRequestId, setDetailRequestId] = useState<number | null>(null);
  const [selectedRequest, setSelectedRequest] =
    useState<TFCSharingRequestDetail | null>(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const selectedFriend = useMemo(
    () =>
      typeof selectedFriendId === "number"
        ? friends.find((friend) => friend.userId === selectedFriendId) ?? null
        : null,
    [friends, selectedFriendId],
  );

  const pendingOutgoingByFriendId = useMemo(
    () =>
      outgoingRequests.reduce<Record<number, number>>((counts, request) => {
        counts[request.recipientUserId] =
          (counts[request.recipientUserId] ?? 0) + 1;
        return counts;
      }, {}),
    [outgoingRequests],
  );

  useEffect(() => {
    async function loadSharingPage() {
      if (!token) {
        setIsLoading(false);
        return;
      }

      try {
        setError("");
        setIsLoading(true);

        const [
          nextFriends,
          nextIncomingRequests,
          nextOutgoingRequests,
          modules,
        ] = await Promise.all([
          fetchFriends(token),
          fetchIncomingTFCSharingRequests(token),
          fetchOutgoingTFCSharingRequests(token),
          fetchModules(token),
        ]);

        const tfcGroups = await Promise.all(
          modules.map((module) => fetchTFCsForModule(module.id, token)),
        );

        setFriends(nextFriends);
        setIncomingRequests(nextIncomingRequests);
        setOutgoingRequests(nextOutgoingRequests);
        setTFCs(
          tfcGroups
            .flat()
            .sort(
              (first, second) =>
                safeTime(second.updatedAt) - safeTime(first.updatedAt),
            ),
        );
      } catch (caughtError) {
        setError(toErrorMessage(caughtError, "Could not load sharing page."));
      } finally {
        setIsLoading(false);
      }
    }

    void loadSharingPage();
  }, [token]);

  function handleToggleTFC(tfcId: number) {
    setSelectedTFCIds((currentTFCIds) =>
      currentTFCIds.includes(tfcId)
        ? currentTFCIds.filter((currentTFCId) => currentTFCId !== tfcId)
        : [...currentTFCIds, tfcId],
    );
  }

  async function handleSendTFCSharingRequest() {
    if (!token || !selectedFriend) {
      setError("Select a friend for TFC sharing.");
      setSuccess("");
      return;
    }

    if (selectedTFCIds.length === 0) {
      setError("Select at least one TFC for sharing.");
      setSuccess("");
      return;
    }

    try {
      setError("");
      setSuccess("");
      setIsSubmitting(true);
      await sendTFCSharingRequest(selectedFriend.userId, selectedTFCIds, token);
      const nextOutgoingRequests = await fetchOutgoingTFCSharingRequests(token);
      setOutgoingRequests(nextOutgoingRequests);
      setSelectedFriendId("");
      setSelectedTFCIds([]);
      setSuccess(`TFC sharing request sent to ${selectedFriend.username}.`);
    } catch (caughtError) {
      setError(
        toErrorMessage(caughtError, "Could not send TFC sharing request."),
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleViewDetail(requestId: number) {
    if (!token) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      setDetailRequestId(requestId);
      const detail = await fetchTFCSharingRequestDetail(requestId, token);
      setSelectedRequest(detail);
    } catch (caughtError) {
      setError(
        toErrorMessage(caughtError, "Could not load TFC sharing request."),
      );
    } finally {
      setDetailRequestId(null);
    }
  }

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="modules-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="modules-main sharing-main">
        <header className="friends-header">
          <div>
            <p className="friends-eyebrow">Sharing</p>
            <h1>TFC sharing</h1>
            <p>
              Select a friend and the topic sheets you want to send privately.
            </p>
          </div>
          <div className="friends-header-stat">
            <strong>{incomingRequests.length + outgoingRequests.length}</strong>
            <span>requests</span>
          </div>
        </header>

        {error && <p className="friends-banner friends-banner-error">{error}</p>}
        {success && (
          <p className="friends-banner friends-banner-success">{success}</p>
        )}

        {isLoading ? (
          <section className="friends-panel">
            <p className="friends-empty">Loading sharing workspace...</p>
          </section>
        ) : (
          <div className="friends-content">
            <section className="friends-panel sharing-compose-panel">
              <div className="friends-section-heading">
                <div>
                  <p className="friends-label">New request</p>
                  <h2>Select TFCs for sharing</h2>
                </div>
                <p>
                  A request captures a snapshot of the selected TFCs and sends
                  it to one friend.
                </p>
              </div>

              <div className="sharing-compose-grid">
                <div className="sharing-field">
                  <label className="sharing-field-label" htmlFor="friend">
                    Friend
                  </label>
                  <select
                    className="friends-input"
                    id="friend"
                    value={selectedFriendId}
                    disabled={isSubmitting}
                    onChange={(event) =>
                      setSelectedFriendId(
                        event.target.value
                          ? Number(event.target.value)
                          : "",
                      )
                    }
                  >
                    <option value="">Select a friend</option>
                    {friends.map((friend) => (
                      <option value={friend.userId} key={friend.userId}>
                        {friend.username}
                        {pendingOutgoingByFriendId[friend.userId]
                          ? " - pending request exists"
                          : ""}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="sharing-selected-count">
                  <strong>{selectedTFCIds.length}</strong>
                  <span>{selectedTFCIds.length === 1 ? "TFC" : "TFCs"} selected</span>
                </div>
              </div>

              {tfcs.length === 0 ? (
                <p className="friends-empty">
                  You do not have any TFCs available for sharing yet.
                </p>
              ) : (
                <div className="sharing-tfc-list">
                  {tfcs.map((tfc) => (
                    <label className="sharing-tfc-row" key={tfc.id}>
                      <input
                        type="checkbox"
                        checked={selectedTFCIds.includes(tfc.id)}
                        disabled={isSubmitting}
                        onChange={() => handleToggleTFC(tfc.id)}
                      />
                      <span className="sharing-tfc-copy">
                        <strong>{tfc.topic}</strong>
                        <span>
                          {tfc.courseCode} - {tfc.schoolSem} - {tfc.entryCount}{" "}
                          {tfc.entryCount === 1 ? "entry" : "entries"}
                        </span>
                        <span>Updated {formatDateTime(tfc.updatedAt)}</span>
                      </span>
                      {tfc.isStale && (
                        <span className="friends-status-pill">Stale</span>
                      )}
                    </label>
                  ))}
                </div>
              )}

              <div className="sharing-compose-actions">
                <button
                  className="friends-primary-button"
                  type="button"
                  disabled={
                    isSubmitting || !selectedFriend || selectedTFCIds.length === 0
                  }
                  onClick={() => void handleSendTFCSharingRequest()}
                >
                  {isSubmitting ? "Sending..." : "Send TFC sharing request"}
                </button>
              </div>
            </section>

            <TFCSharingRequestsPanel
              incomingRequests={incomingRequests}
              outgoingRequests={outgoingRequests}
              detailRequestId={detailRequestId}
              onViewDetail={handleViewDetail}
            />
          </div>
        )}

        {selectedRequest && (
          <TFCSharingDetailModal
            request={selectedRequest}
            onClose={() => setSelectedRequest(null)}
          />
        )}
      </main>
    </div>
  );
}

function formatDateTime(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function safeTime(value: string) {
  const time = new Date(value).getTime();
  return Number.isNaN(time) ? 0 : time;
}

function toErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}
