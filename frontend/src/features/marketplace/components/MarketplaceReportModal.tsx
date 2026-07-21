import { useState } from "react";
import type {
  CreateMarketplaceReportRequest,
  MarketplaceReportReason,
} from "../types/marketplaceTypes";

type MarketplaceReportModalProps = {
  isSubmitting: boolean;
  error: string;
  onSubmit: (request: CreateMarketplaceReportRequest) => void;
  onCancel: () => void;
};

export function MarketplaceReportModal({
  isSubmitting,
  error,
  onSubmit,
  onCancel,
}: MarketplaceReportModalProps) {
  const [reason, setReason] = useState<MarketplaceReportReason>("INACCURATE_CONTENT");
  const [details, setDetails] = useState("");

  function handleSubmit() {
    const normalizedDetails = details.trim();
    onSubmit({
      reason,
      details: normalizedDetails || undefined,
    });
  }

  return (
    <>
      <div
        aria-hidden="true"
        className="marketplace-modal-backdrop"
        onClick={isSubmitting ? undefined : onCancel}
      />
      <section
        aria-label="Report marketplace listing"
        className="marketplace-confirm-modal marketplace-report-modal"
        onClick={(event) => event.stopPropagation()}
      >
        <div>
          <p className="modules-eyebrow">Report listing</p>
          <h2>Why are you reporting this?</h2>
          <p>Select the closest reason and add any useful details.</p>
        </div>

        <label className="marketplace-edit-field">
          <span>Reason</span>
          <select
            className="marketplace-input"
            value={reason}
            disabled={isSubmitting}
            onChange={(event) => setReason(event.target.value as MarketplaceReportReason)}
          >
            <option value="INACCURATE_CONTENT">Inaccurate content</option>
            <option value="SPAM">Spam</option>
            <option value="OFFENSIVE_CONTENT">Offensive content</option>
            <option value="PERSONAL_INFORMATION">Personal information</option>
            <option value="RESTRICTED_ASSESSMENT_MATERIAL">Restricted assessment material</option>
            <option value="COPYRIGHTED_MATERIAL">Copyrighted material</option>
            <option value="OTHER">Other</option>
          </select>
        </label>

        <label className="marketplace-edit-field">
          <span>Message (optional)</span>
          <textarea
            className="marketplace-input marketplace-report-textarea"
            value={details}
            maxLength={1000}
            disabled={isSubmitting}
            placeholder="Tell us what is wrong with this listing."
            onChange={(event) => setDetails(event.target.value)}
          />
        </label>

        {error && (
          <p className="marketplace-banner marketplace-banner-error" role="alert">
            {error}
          </p>
        )}

        <div className="marketplace-confirm-actions">
          <button
            className="marketplace-secondary-button"
            type="button"
            disabled={isSubmitting}
            onClick={onCancel}
          >
            Cancel
          </button>
          <button
            className="marketplace-danger-button"
            type="button"
            disabled={isSubmitting}
            onClick={handleSubmit}
          >
            {isSubmitting ? "Sending..." : "Send report"}
          </button>
        </div>
      </section>
    </>
  );
}
