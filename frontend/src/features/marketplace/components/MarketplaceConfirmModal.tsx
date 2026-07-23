type MarketplaceConfirmModalProps = {
  title: string;
  description: string;
  confirmLabel: string;
  isWorking: boolean;
  isDanger?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
};

export function MarketplaceConfirmModal({
  title,
  description,
  confirmLabel,
  isWorking,
  isDanger = false,
  onConfirm,
  onCancel,
}: MarketplaceConfirmModalProps) {
  return (
    <>
      <div
        aria-hidden="true"
        className="marketplace-modal-backdrop"
        onClick={isWorking ? undefined : onCancel}
      />
      <section
        aria-label={title}
        className="marketplace-confirm-modal"
        onClick={(event) => event.stopPropagation()}
      >
        <div>
          <h2>{title}</h2>
          <p>{description}</p>
        </div>

        <div className="marketplace-confirm-actions">
          <button
            className="marketplace-secondary-button"
            type="button"
            disabled={isWorking}
            onClick={onCancel}
          >
            Cancel
          </button>
          <button
            className={isDanger ? "marketplace-danger-button" : "marketplace-primary-button"}
            type="button"
            disabled={isWorking}
            onClick={onConfirm}
          >
            {isWorking ? "Working..." : confirmLabel}
          </button>
        </div>
      </section>
    </>
  );
}
