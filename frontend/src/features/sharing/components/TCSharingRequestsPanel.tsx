import type { TCSharingRequestSummary } from "../types/tcSharingTypes";

type TCSharingRequestsPanelProps = {
  incomingRequests: TCSharingRequestSummary[];
  outgoingRequests: TCSharingRequestSummary[];
  detailRequestId: number | null;
  cancellingRequestId: number | null;
  onViewDetail: (requestId: number) => Promise<void>;
  onCancelOutgoingRequest: (requestId: number) => Promise<void>;
};

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

function formatTopics(topics: string[]) {
  if (topics.length === 0) {
    return "No topics";
  }

  if (topics.length <= 3) {
    return topics.join(", ");
  }

  return `${topics.slice(0, 3).join(", ")} +${topics.length - 3} more`;
}

export function TCSharingRequestsPanel({
  incomingRequests,
  outgoingRequests,
  detailRequestId,
  cancellingRequestId,
  onViewDetail,
  onCancelOutgoingRequest,
}: TCSharingRequestsPanelProps) {
  const totalCount = incomingRequests.length + outgoingRequests.length;

  return (
    <section className="friends-panel tc-sharing-panel">
      <div className="friends-section-heading friends-section-heading-inline">
        <div>
          <p className="friends-label">TC sharing</p>
          <h2>Private sharing requests</h2>
        </div>
        <span className="friends-count">{totalCount}</span>
      </div>

      <div className="tc-sharing-request-grid">
        <TCSharingRequestList
          title="Incoming"
          emptyMessage="No pending incoming TC sharing requests."
          requests={incomingRequests}
          detailRequestId={detailRequestId}
          cancellingRequestId={cancellingRequestId}
          direction="incoming"
          onViewDetail={onViewDetail}
          onCancelOutgoingRequest={onCancelOutgoingRequest}
        />
        <TCSharingRequestList
          title="Outgoing"
          emptyMessage="No pending outgoing TC sharing requests."
          requests={outgoingRequests}
          detailRequestId={detailRequestId}
          cancellingRequestId={cancellingRequestId}
          direction="outgoing"
          onViewDetail={onViewDetail}
          onCancelOutgoingRequest={onCancelOutgoingRequest}
        />
      </div>
    </section>
  );
}

type TCSharingRequestListProps = {
  title: string;
  emptyMessage: string;
  requests: TCSharingRequestSummary[];
  detailRequestId: number | null;
  cancellingRequestId: number | null;
  direction: "incoming" | "outgoing";
  onViewDetail: (requestId: number) => Promise<void>;
  onCancelOutgoingRequest: (requestId: number) => Promise<void>;
};

function TCSharingRequestList({
  title,
  emptyMessage,
  requests,
  detailRequestId,
  cancellingRequestId,
  direction,
  onViewDetail,
  onCancelOutgoingRequest,
}: TCSharingRequestListProps) {
  return (
    <div className="tc-sharing-request-column">
      <div className="tc-sharing-column-header">
        <h3>{title}</h3>
        <span>{requests.length}</span>
      </div>

      {requests.length === 0 ? (
        <p className="friends-empty">{emptyMessage}</p>
      ) : (
        <div className="friends-person-list">
          {requests.map((request) => {
            const displayName =
              direction === "incoming"
                ? request.senderUsername
                : request.recipientUsername;
            const displayEmail =
              direction === "incoming"
                ? request.senderEmail
                : request.recipientEmail;
            const isLoading = detailRequestId === request.id;
            const isCancelling = cancellingRequestId === request.id;

            return (
              <article className="friends-request-card" key={request.id}>
                <div className="friends-person-row friends-person-row-plain">
                  <div className="friends-avatar" aria-hidden="true">
                    {displayName.charAt(0).toUpperCase()}
                  </div>
                  <div className="friends-person-copy">
                    <h3>{displayName}</h3>
                    <p>{displayEmail}</p>
                    <span>Sent {formatDateTime(request.createdAt)}</span>
                  </div>
                </div>
                <p className="tc-sharing-topic-line">
                  {request.itemCount}{" "}
                  {request.itemCount === 1 ? "TC" : "TCs"}:{" "}
                  {formatTopics(request.topics)}
                </p>
                <div className="friends-request-actions">
                  <button
                    className="friends-secondary-button"
                    type="button"
                    disabled={isLoading || isCancelling}
                    onClick={() => void onViewDetail(request.id)}
                  >
                    {isLoading ? "Loading..." : "View"}
                  </button>
                  {direction === "outgoing" && (
                    <button
                      className="friends-danger-button"
                      type="button"
                      disabled={isCancelling}
                      onClick={() => void onCancelOutgoingRequest(request.id)}
                    >
                      {isCancelling ? "Cancelling..." : "Cancel"}
                    </button>
                  )}
                </div>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}
