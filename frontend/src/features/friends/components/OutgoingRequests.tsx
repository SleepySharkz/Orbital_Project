import type { FriendRequest } from "../types/friendTypes";

type OutgoingRequestsProps = {
  requests: FriendRequest[];
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

export function OutgoingRequests({ requests }: OutgoingRequestsProps) {
  return (
    <section className="friends-panel">
      <div className="friends-section-heading friends-section-heading-inline">
        <div>
          <p className="friends-label">Outgoing requests</p>
          <h2>Pending responses</h2>
        </div>
        <span className="friends-count">{requests.length}</span>
      </div>

      {requests.length === 0 ? (
        <p className="friends-empty">No outgoing friend requests.</p>
      ) : (
        <div className="friends-person-list">
          {requests.map((request) => (
            <article className="friends-person-row" key={request.id}>
              <div className="friends-avatar" aria-hidden="true">
                {request.recipientUsername.charAt(0).toUpperCase()}
              </div>
              <div className="friends-person-copy">
                <h3>{request.recipientUsername}</h3>
                <p>{request.recipientEmail}</p>
                <span>Sent {formatDateTime(request.createdAt)}</span>
              </div>
              <span className="friends-status-pill">Pending</span>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
