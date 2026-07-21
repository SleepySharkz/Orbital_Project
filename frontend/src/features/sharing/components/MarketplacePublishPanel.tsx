import type { TcSummary } from "../../tc/api/tcApi";
import type { PublisherVisibility } from "../../marketplace/types/marketplaceTypes";

type MarketplacePublishPanelProps = {
  tcs: TcSummary[];
  selectedTcId: number | "";
  publicTitle: string;
  description: string;
  tagsText: string;
  institution: string;
  publisherVisibility: PublisherVisibility;
  isSubmitting: boolean;
  onSelectedTcIdChange: (tcId: number | "") => void;
  onPublicTitleChange: (publicTitle: string) => void;
  onDescriptionChange: (description: string) => void;
  onTagsTextChange: (tagsText: string) => void;
  onInstitutionChange: (institution: string) => void;
  onPublisherVisibilityChange: (publisherVisibility: PublisherVisibility) => void;
  onPublish: () => void;
};

export function MarketplacePublishPanel({
  tcs,
  selectedTcId,
  publicTitle,
  description,
  tagsText,
  institution,
  publisherVisibility,
  isSubmitting,
  onSelectedTcIdChange,
  onPublicTitleChange,
  onDescriptionChange,
  onTagsTextChange,
  onInstitutionChange,
  onPublisherVisibilityChange,
  onPublish,
}: MarketplacePublishPanelProps) {
  const selectedTc =
    typeof selectedTcId === "number"
      ? tcs.find((tc) => tc.id === selectedTcId) ?? null
      : null;

  return (
    <section className="friends-panel sharing-compose-panel marketplace-publish-panel">
      <div className="friends-section-heading">
        <div>
          <p className="friends-label">Public marketplace</p>
          <h2>Publish a TC snapshot</h2>
        </div>
        <p>
          Publishing creates a public copy. Later edits to your TC do not change
          the listing until you update it.
        </p>
      </div>

      <div className="sharing-field">
        <label className="sharing-field-label" htmlFor="marketplace-tc">
          Topic sheet
        </label>
        <select
          className="friends-input"
          id="marketplace-tc"
          value={selectedTcId}
          disabled={isSubmitting}
          onChange={(event) =>
            onSelectedTcIdChange(
              event.target.value ? Number(event.target.value) : "",
            )
          }
        >
          <option value="">Select a TC to publish</option>
          {tcs.map((tc) => (
            <option value={tc.id} key={tc.id}>
              {tc.topic} - {tc.courseCode} - {tc.entryCount} entries
            </option>
          ))}
        </select>
      </div>

      {selectedTc && (
        <p className="sharing-publish-source">
          {selectedTc.courseCode} - {selectedTc.schoolSem} - {selectedTc.topic}
        </p>
      )}

      <div className="sharing-publish-grid">
        <label className="sharing-field">
          <span className="sharing-field-label">Public title</span>
          <input
            className="friends-input"
            value={publicTitle}
            maxLength={120}
            disabled={isSubmitting}
            onChange={(event) => onPublicTitleChange(event.target.value)}
            placeholder="e.g. Complete Data Structures Guide"
          />
        </label>

        <label className="sharing-field">
          <span className="sharing-field-label">Publisher</span>
          <select
            className="friends-input"
            value={publisherVisibility}
            disabled={isSubmitting}
            onChange={(event) =>
              onPublisherVisibilityChange(event.target.value as PublisherVisibility)
            }
          >
            <option value="DISPLAY_NAME">Show my name</option>
            <option value="ANONYMOUS">Anonymous</option>
          </select>
        </label>
      </div>

      <label className="sharing-field">
        <span className="sharing-field-label">Description</span>
        <textarea
          className="friends-input sharing-textarea"
          value={description}
          maxLength={1000}
          disabled={isSubmitting}
          onChange={(event) => onDescriptionChange(event.target.value)}
          placeholder="Briefly describe what this TC helps with."
        />
      </label>

      <div className="sharing-publish-grid">
        <label className="sharing-field">
          <span className="sharing-field-label">Tags</span>
          <input
            className="friends-input"
            value={tagsText}
            disabled={isSubmitting}
            onChange={(event) => onTagsTextChange(event.target.value)}
            placeholder="revision, finals, practice"
          />
        </label>

        <label className="sharing-field">
          <span className="sharing-field-label">Institution</span>
          <input
            className="friends-input"
            value={institution}
            maxLength={120}
            disabled={isSubmitting}
            onChange={(event) => onInstitutionChange(event.target.value)}
            placeholder="Optional"
          />
        </label>
      </div>

      <div className="sharing-compose-actions">
        <button
          className="friends-primary-button"
          type="button"
          disabled={isSubmitting || !selectedTcId || publicTitle.trim().length < 3}
          onClick={onPublish}
        >
          {isSubmitting ? "Publishing..." : "Publish to marketplace"}
        </button>
      </div>
    </section>
  );
}
