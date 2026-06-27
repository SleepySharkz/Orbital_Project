import type { FriendRequest } from "../types/friendTypes";

type IncomingRequestsProps = {
  requests: FriendRequest[];
  actionRequestId: number | null;
  onAccept: (requestId: number) => Promise<void>;
  onDecline: (requestId: number) => Promise<void>;
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

export function IncomingRequests({
  requests,
  actionRequestId,
  onAccept,
  onDecline,
}: IncomingRequestsProps) {
  return (
    <section className="friends-panel">
      <div className="friends-section-heading friends-section-heading-inline">
        <div>
          <p className="friends-label">Incoming requests</p>
          <h2>Waiting for you</h2>
        </div>
        <span className="friends-count">{requests.length}</span>
      </div>

      {requests.length === 0 ? (
        <p className="friends-empty">No incoming friend requests.</p>
      ) : (
        <div className="friends-person-list">
          {requests.map((request) => {
            const isUpdating = actionRequestId === request.id;

            return (
              <article className="friends-request-card" key={request.id}>
                <div className="friends-person-row friends-person-row-plain">
                  <div className="friends-avatar" aria-hidden="true">
                    {request.senderUsername.charAt(0).toUpperCase()}
                  </div>
                  <div className="friends-person-copy">
                    <h3>{request.senderUsername}</h3>
                    <p>{request.senderEmail}</p>
                    <span>Sent {formatDateTime(request.createdAt)}</span>
                  </div>
                </div>

                <div className="friends-request-actions">
                  <button
                    className="friends-primary-button"
                    type="button"
                    disabled={isUpdating}
                    onClick={() => void onAccept(request.id)}
                  >
                    {isUpdating ? "Updating..." : "Accept"}
                  </button>
                  <button
                    className="friends-danger-button"
                    type="button"
                    disabled={isUpdating}
                    onClick={() => void onDecline(request.id)}
                  >
                    Decline
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
