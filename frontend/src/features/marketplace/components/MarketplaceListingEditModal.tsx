import { useState } from "react";
import type {
  MarketplaceListingManagementDetail,
  PublisherVisibility,
  UpdateMarketplaceListingMetadataRequest,
} from "../types/marketplaceTypes";

type MarketplaceListingEditModalProps = {
  listing: MarketplaceListingManagementDetail;
  isSaving: boolean;
  error: string;
  onSave: (
    listingId: number,
    request: UpdateMarketplaceListingMetadataRequest,
  ) => void;
  onClose: () => void;
};

export function MarketplaceListingEditModal({
  listing,
  isSaving,
  error,
  onSave,
  onClose,
}: MarketplaceListingEditModalProps) {
  const [publicTitle, setPublicTitle] = useState(listing.publicTitle);
  const [description, setDescription] = useState(listing.description ?? "");
  const [tagsText, setTagsText] = useState(listing.tags.join(", "));
  const [institution, setInstitution] = useState(listing.institution ?? "");
  const [publisherVisibility, setPublisherVisibility] =
    useState<PublisherVisibility>(listing.publisherVisibility);

  function handleSave() {
    onSave(listing.id, {
      publicTitle: publicTitle.trim(),
      description: normalizeOptional(description),
      tags: parseTags(tagsText),
      institution: normalizeOptional(institution),
      publisherVisibility,
    });
  }

  return (
    <>
      <div
        aria-hidden="true"
        className="marketplace-modal-backdrop"
        onClick={onClose}
      />
      <section
        aria-label="Edit marketplace listing"
        className="marketplace-edit-modal"
        onClick={(event) => event.stopPropagation()}
      >
        <div className="marketplace-edit-header">
          <div>
            <h2>{listing.publicTitle}</h2>
          </div>
          <button
            className="marketplace-secondary-button"
            type="button"
            disabled={isSaving}
            onClick={onClose}
          >
            Close
          </button>
        </div>

        <p className="marketplace-muted">
          This changes the public listing details only. It does not update the
          published entries.
        </p>

        {error && <p className="marketplace-banner marketplace-banner-error">{error}</p>}

        <div className="marketplace-edit-grid">
          <label className="marketplace-edit-field">
            <span>Public title</span>
            <input
              className="marketplace-input"
              value={publicTitle}
              maxLength={120}
              disabled={isSaving}
              onChange={(event) => setPublicTitle(event.target.value)}
            />
          </label>

          <label className="marketplace-edit-field">
            <span>Publisher</span>
            <select
              className="marketplace-input"
              value={publisherVisibility}
              disabled={isSaving}
              onChange={(event) =>
                setPublisherVisibility(event.target.value as PublisherVisibility)
              }
            >
              <option value="DISPLAY_NAME">Show my name</option>
              <option value="ANONYMOUS">Anonymous</option>
            </select>
          </label>
        </div>

        <label className="marketplace-edit-field">
          <span>Description</span>
          <textarea
            className="marketplace-input marketplace-edit-textarea"
            value={description}
            maxLength={1000}
            disabled={isSaving}
            onChange={(event) => setDescription(event.target.value)}
          />
        </label>

        <div className="marketplace-edit-grid">
          <label className="marketplace-edit-field">
            <span>Tags</span>
            <input
              className="marketplace-input"
              value={tagsText}
              disabled={isSaving}
              placeholder="revision, finals, practice"
              onChange={(event) => setTagsText(event.target.value)}
            />
          </label>

          <label className="marketplace-edit-field">
            <span>Institution</span>
            <input
              className="marketplace-input"
              value={institution}
              maxLength={120}
              disabled={isSaving}
              onChange={(event) => setInstitution(event.target.value)}
            />
          </label>
        </div>

        <div className="marketplace-edit-actions">
          <button
            className="marketplace-primary-button"
            type="button"
            disabled={isSaving || publicTitle.trim().length < 3}
            onClick={handleSave}
          >
            {isSaving ? "Saving..." : "Save changes"}
          </button>
        </div>
      </section>
    </>
  );
}

function normalizeOptional(value: string) {
  const normalizedValue = value.trim();
  return normalizedValue ? normalizedValue : undefined;
}

function parseTags(value: string) {
  const tags = value
    .split(",")
    .map((tag) => tag.trim())
    .filter(Boolean);

  return tags.length > 0 ? tags : undefined;
}
