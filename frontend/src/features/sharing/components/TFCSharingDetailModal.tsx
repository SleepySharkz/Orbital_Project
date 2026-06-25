import { useEffect } from "react";
import type {
  TFCSharingCompatibilityStatus,
  TFCSharingRequestDetail,
} from "../types/tfcSharingTypes";

type TFCSharingDetailModalProps = {
  request: TFCSharingRequestDetail;
  viewerUserId: number;
  isResponding: boolean;
  onAccept: (requestId: number) => Promise<void>;
  onDecline: (requestId: number) => Promise<void>;
  onClose: () => void;
};

function formatDateTime(value: string | null) {
  if (!value) {
    return "Not available";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

function compatibilityLabel(status: TFCSharingCompatibilityStatus | null) {
  switch (status) {
    case "READY":
      return "Ready";
    case "MISSING_MODULE":
      return "Missing module";
    case "MISSING_TOPIC":
      return "Missing topic";
    default:
      return "";
  }
}

export function TFCSharingDetailModal({
  request,
  viewerUserId,
  isResponding,
  onAccept,
  onDecline,
  onClose,
}: TFCSharingDetailModalProps) {
  const viewerIsRecipient = request.recipientUserId === viewerUserId;

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === "Escape") {
        onClose();
      }
    }

    window.addEventListener("keydown", handleKeyDown);

    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [onClose]);

  return (
    <div
      className="friends-modal-backdrop"
      role="presentation"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <section
        aria-labelledby="tfc-sharing-detail-title"
        aria-modal="true"
        className="friends-modal tfc-sharing-detail-modal"
        role="dialog"
      >
        <header className="friends-modal-header">
          <div>
            <p className="friends-label">TFC sharing request</p>
            <h2 id="tfc-sharing-detail-title">
              {request.senderUsername} to {request.recipientUsername}
            </h2>
            <p>Sent {formatDateTime(request.createdAt)}</p>
          </div>
          <button
            className="friends-icon-button"
            type="button"
            aria-label="Close sharing request detail"
            onClick={onClose}
          >
            x
          </button>
        </header>

        <div className="tfc-sharing-detail-summary">
          <span className="friends-status-pill">{request.status}</span>
          <span>
            {request.items.length}{" "}
            {request.items.length === 1 ? "topic sheet" : "topic sheets"}
          </span>
          {viewerIsRecipient && (
            <span className="friends-status-pill">
              {request.canAccept ? "Ready to accept" : "Blocked"}
            </span>
          )}
        </div>

        {viewerIsRecipient && request.blockingReasons.length > 0 && (
          <div className="tfc-sharing-blockers">
            <p className="friends-label">Blockers</p>
            <ul>
              {request.blockingReasons.map((reason) => (
                <li key={reason}>{reason}</li>
              ))}
            </ul>
          </div>
        )}

        {viewerIsRecipient && request.status === "PENDING" && (
          <div className="tfc-sharing-accept-row">
            <button
              className="friends-secondary-button"
              type="button"
              disabled={isResponding}
              onClick={() => void onDecline(request.id)}
            >
              {isResponding ? "Resolving..." : "Decline"}
            </button>
            <button
              className="friends-primary-button"
              type="button"
              disabled={isResponding || !request.canAccept}
              onClick={() => void onAccept(request.id)}
            >
              {isResponding ? "Resolving..." : "Accept all"}
            </button>
          </div>
        )}

        <div className="tfc-sharing-detail-list">
          {request.items.map((item) => (
            <article className="tfc-sharing-detail-card" key={item.id}>
              <header className="tfc-sharing-detail-card-header">
                <div>
                  <h3>{item.topic}</h3>
                  <p>
                    {item.courseCode} - {item.schoolSem} - {item.entryCount}{" "}
                    {item.entryCount === 1 ? "entry" : "entries"}
                  </p>
                </div>
                <div className="tfc-sharing-status-stack">
                  {item.sourceWasStaleAtSendTime && (
                    <span className="friends-status-pill">Stale at send</span>
                  )}
                  {viewerIsRecipient && item.compatibilityStatus && (
                    <span className="friends-status-pill">
                      {compatibilityLabel(item.compatibilityStatus)}
                    </span>
                  )}
                </div>
              </header>

              {viewerIsRecipient && item.blockingReason && (
                <p className="tfc-sharing-item-blocker">
                  {item.blockingReason}
                </p>
              )}

              {item.entries.length === 0 ? (
                <p className="friends-empty">No entries were captured.</p>
              ) : (
                <div className="tfc-sharing-entry-list">
                  {item.entries.map((entry) => (
                    <article className="tfc-sharing-entry-card" key={entry.id}>
                      <p className="friends-label">Flashcard</p>
                      <h4>{entry.flashcardQuestion}</h4>
                      <p>{entry.flashcardNoteContent}</p>
                      {entry.questionText && (
                        <>
                          <p className="friends-label">Source question</p>
                          <p>{entry.questionText}</p>
                        </>
                      )}
                      <p className="friends-label">Rough note</p>
                      <p>{entry.roughNote}</p>
                    </article>
                  ))}
                </div>
              )}
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
