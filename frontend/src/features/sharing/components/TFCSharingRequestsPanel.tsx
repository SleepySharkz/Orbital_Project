import type { TFCSharingRequestSummary } from "../types/tfcSharingTypes";

type TFCSharingRequestsPanelProps = {
  incomingRequests: TFCSharingRequestSummary[];
  outgoingRequests: TFCSharingRequestSummary[];
  detailRequestId: number | null;
  onViewDetail: (requestId: number) => Promise<void>;
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

export function TFCSharingRequestsPanel({
  incomingRequests,
  outgoingRequests,
  detailRequestId,
  onViewDetail,
}: TFCSharingRequestsPanelProps) {
  const totalCount = incomingRequests.length + outgoingRequests.length;

  return (
    <section className="friends-panel tfc-sharing-panel">
      <div className="friends-section-heading friends-section-heading-inline">
        <div>
          <p className="friends-label">TFC sharing</p>
          <h2>Private sharing requests</h2>
        </div>
        <span className="friends-count">{totalCount}</span>
      </div>

      <div className="tfc-sharing-request-grid">
        <TFCSharingRequestList
          title="Incoming"
          emptyMessage="No incoming TFC sharing requests."
          requests={incomingRequests}
          detailRequestId={detailRequestId}
          direction="incoming"
          onViewDetail={onViewDetail}
        />
        <TFCSharingRequestList
          title="Outgoing"
          emptyMessage="No outgoing TFC sharing requests."
          requests={outgoingRequests}
          detailRequestId={detailRequestId}
          direction="outgoing"
          onViewDetail={onViewDetail}
        />
      </div>
    </section>
  );
}

type TFCSharingRequestListProps = {
  title: string;
  emptyMessage: string;
  requests: TFCSharingRequestSummary[];
  detailRequestId: number | null;
  direction: "incoming" | "outgoing";
  onViewDetail: (requestId: number) => Promise<void>;
};

function TFCSharingRequestList({
  title,
  emptyMessage,
  requests,
  detailRequestId,
  direction,
  onViewDetail,
}: TFCSharingRequestListProps) {
  return (
    <div className="tfc-sharing-request-column">
      <div className="tfc-sharing-column-header">
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
                <p className="tfc-sharing-topic-line">
                  {request.itemCount}{" "}
                  {request.itemCount === 1 ? "TFC" : "TFCs"}:{" "}
                  {formatTopics(request.topics)}
                </p>
                <div className="friends-request-actions">
                  <button
                    className="friends-secondary-button"
                    type="button"
                    disabled={isLoading}
                    onClick={() => void onViewDetail(request.id)}
                  >
                    {isLoading ? "Loading..." : "View"}
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}
