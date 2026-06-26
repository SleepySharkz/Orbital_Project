import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { fetchFriends } from "../../friends/api/friendsApi";
import type { FriendSummary } from "../../friends/types/friendTypes";
import { fetchModules } from "../../modules/api/moduleApi";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import "../../friends/styles/friendsStyles.css";
import { fetchTCsForModule, type TcSummary } from "../../tc/api/tcApi";
import {
  acceptTCSharingRequest,
  cancelTCSharingRequest,
  declineTCSharingRequest,
  fetchIncomingTCSharingRequests,
  fetchOutgoingTCSharingRequests,
  fetchTCSharingRequestDetail,
  sendTCSharingRequest,
} from "../api/tcSharingApi";
import { TCSharingDetailModal } from "../components/TCSharingDetailModal";
import { TCSharingRequestsPanel } from "../components/TCSharingRequestsPanel";
import type {
  TCSharingRequestDetail,
  TCSharingRequestSummary,
} from "../types/tcSharingTypes";
import "../styles/sharingStyles.css";

export function SharingPage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [friends, setFriends] = useState<FriendSummary[]>([]);
  const [tcs, setTCs] = useState<TcSummary[]>([]);
  const [incomingRequests, setIncomingRequests] = useState<
    TCSharingRequestSummary[]
  >([]);
  const [outgoingRequests, setOutgoingRequests] = useState<
    TCSharingRequestSummary[]
  >([]);
  const [selectedFriendId, setSelectedFriendId] = useState<number | "">("");
  const [selectedTCIds, setSelectedTCIds] = useState<number[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [respondingRequestId, setRespondingRequestId] = useState<number | null>(
    null,
  );
  const [cancellingRequestId, setCancellingRequestId] = useState<number | null>(
    null,
  );
  const [detailRequestId, setDetailRequestId] = useState<number | null>(null);
  const [selectedRequest, setSelectedRequest] =
    useState<TCSharingRequestDetail | null>(null);
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
          fetchIncomingTCSharingRequests(token),
          fetchOutgoingTCSharingRequests(token),
          fetchModules(token),
        ]);

        const tcGroups = await Promise.all(
          modules.map((module) => fetchTCsForModule(module.id, token)),
        );

        setFriends(nextFriends);
        setIncomingRequests(nextIncomingRequests);
        setOutgoingRequests(nextOutgoingRequests);
        setTCs(
          tcGroups
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

  function handleToggleTC(tcId: number) {
    setSelectedTCIds((currentTCIds) =>
      currentTCIds.includes(tcId)
        ? currentTCIds.filter((currentTCId) => currentTCId !== tcId)
        : [...currentTCIds, tcId],
    );
  }

  async function refreshSharingRequests() {
    if (!token) {
      return;
    }

    const [nextIncomingRequests, nextOutgoingRequests] = await Promise.all([
      fetchIncomingTCSharingRequests(token),
      fetchOutgoingTCSharingRequests(token),
    ]);

    setIncomingRequests(nextIncomingRequests);
    setOutgoingRequests(nextOutgoingRequests);
  }

  async function handleSendTCSharingRequest() {
    if (!token || !selectedFriend) {
      setError("Select a friend for TC sharing.");
      setSuccess("");
      return;
    }

    if (selectedTCIds.length === 0) {
      setError("Select at least one TC for sharing.");
      setSuccess("");
      return;
    }

    try {
      setError("");
      setSuccess("");
      setIsSubmitting(true);
      await sendTCSharingRequest(selectedFriend.userId, selectedTCIds, token);
      const nextOutgoingRequests = await fetchOutgoingTCSharingRequests(token);
      setOutgoingRequests(nextOutgoingRequests);
      setSelectedFriendId("");
      setSelectedTCIds([]);
      setSuccess(`TC sharing request sent to ${selectedFriend.username}.`);
    } catch (caughtError) {
      setError(
        toErrorMessage(caughtError, "Could not send TC sharing request."),
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  async function handleAcceptSharingRequest(requestId: number) {
    if (!token) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      setRespondingRequestId(requestId);
      await acceptTCSharingRequest(requestId, token);
      await refreshSharingRequests();
      setSelectedRequest(null);
      setSuccess(
        "TC sharing request accepted. Shared copies are available under Shared TCs.",
      );
    } catch (caughtError) {
      setError(
        toErrorMessage(caughtError, "Could not accept TC sharing request."),
      );
    } finally {
      setRespondingRequestId(null);
    }
  }

  async function handleDeclineSharingRequest(requestId: number) {
    if (!token) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      setRespondingRequestId(requestId);
      await declineTCSharingRequest(requestId, token);
      await refreshSharingRequests();
      setSelectedRequest(null);
      setSuccess("TC sharing request declined.");
    } catch (caughtError) {
      setError(
        toErrorMessage(caughtError, "Could not decline TC sharing request."),
      );
    } finally {
      setRespondingRequestId(null);
    }
  }

  async function handleCancelOutgoingRequest(requestId: number) {
    if (!token) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      setCancellingRequestId(requestId);
      await cancelTCSharingRequest(requestId, token);
      await refreshSharingRequests();
      setSelectedRequest((currentRequest) =>
        currentRequest?.id === requestId ? null : currentRequest,
      );
      setSuccess("Pending TC sharing request cancelled.");
    } catch (caughtError) {
      setError(
        toErrorMessage(
          caughtError,
          "Could not cancel pending TC sharing request.",
        ),
      );
    } finally {
      setCancellingRequestId(null);
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
      const detail = await fetchTCSharingRequestDetail(requestId, token);
      setSelectedRequest(detail);
    } catch (caughtError) {
      setError(
        toErrorMessage(caughtError, "Could not load TC sharing request."),
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
            <h1>TC sharing</h1>
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
                  <h2>Select TCs for sharing</h2>
                </div>
                <p>
                  A request captures a snapshot of the selected TCs and sends
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
                  <strong>{selectedTCIds.length}</strong>
                  <span>{selectedTCIds.length === 1 ? "TC" : "TCs"} selected</span>
                </div>
              </div>

              {tcs.length === 0 ? (
                <p className="friends-empty">
                  You do not have any TCs available for sharing yet.
                </p>
              ) : (
                <div className="sharing-tc-list">
                  {tcs.map((tc) => (
                    <label className="sharing-tc-row" key={tc.id}>
                      <input
                        type="checkbox"
                        checked={selectedTCIds.includes(tc.id)}
                        disabled={isSubmitting}
                        onChange={() => handleToggleTC(tc.id)}
                      />
                      <span className="sharing-tc-copy">
                        <strong>{tc.topic}</strong>
                        <span>
                          {tc.courseCode} - {tc.schoolSem} - {tc.entryCount}{" "}
                          {tc.entryCount === 1 ? "entry" : "entries"}
                        </span>
                        <span>Updated {formatDateTime(tc.updatedAt)}</span>
                      </span>
                      {tc.isStale && (
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
                    isSubmitting || !selectedFriend || selectedTCIds.length === 0
                  }
                  onClick={() => void handleSendTCSharingRequest()}
                >
                  {isSubmitting ? "Sending..." : "Send TC sharing request"}
                </button>
              </div>
            </section>

            <TCSharingRequestsPanel
              incomingRequests={incomingRequests}
              outgoingRequests={outgoingRequests}
              detailRequestId={detailRequestId}
              cancellingRequestId={cancellingRequestId}
              onViewDetail={handleViewDetail}
              onCancelOutgoingRequest={handleCancelOutgoingRequest}
            />
          </div>
        )}

        {selectedRequest && (
          <TCSharingDetailModal
            request={selectedRequest}
            viewerUserId={getViewerUserId(selectedRequest, user.email)}
            isResponding={respondingRequestId === selectedRequest.id}
            onAccept={handleAcceptSharingRequest}
            onDecline={handleDeclineSharingRequest}
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

function getViewerUserId(request: TCSharingRequestDetail, viewerEmail: string) {
  const normalizedViewerEmail = viewerEmail.toLowerCase();

  if (request.recipientEmail.toLowerCase() === normalizedViewerEmail) {
    return request.recipientUserId;
  }

  return request.senderUserId;
}
