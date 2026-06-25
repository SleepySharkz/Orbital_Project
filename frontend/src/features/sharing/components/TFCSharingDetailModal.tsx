import type { TFCSharingRequestDetail } from "../types/tfcSharingTypes";

type TFCSharingDetailModalProps = {
  request: TFCSharingRequestDetail;
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

export function TFCSharingDetailModal({
  request,
  onClose,
}: TFCSharingDetailModalProps) {
  return (
    <div className="friends-modal-backdrop" role="presentation">
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
        </div>

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
                {item.sourceWasStaleAtSendTime && (
                  <span className="friends-status-pill">Stale at send</span>
                )}
              </header>

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
