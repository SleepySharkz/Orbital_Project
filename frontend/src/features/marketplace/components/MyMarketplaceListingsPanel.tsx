import { Link } from "react-router-dom";
import type { MarketplaceListingManagementSummary } from "../types/marketplaceTypes";

type MyMarketplaceListingsPanelProps = {
  listings: MarketplaceListingManagementSummary[];
  isLoading: boolean;
  error: string;
  actionListingId: number | null;
  onGoToPublish: () => void;
  onEdit: (listingId: number) => void;
  onRepublish: (listing: MarketplaceListingManagementSummary) => void;
  onUnlist: (listing: MarketplaceListingManagementSummary) => void;
};

export function MyMarketplaceListingsPanel({
  listings,
  isLoading,
  error,
  actionListingId,
  onGoToPublish,
  onEdit,
  onRepublish,
  onUnlist,
}: MyMarketplaceListingsPanelProps) {
  return (
    <section className="marketplace-publishing-panel">
      <div className="marketplace-publishing-intro">
        <div>
          <p className="modules-eyebrow">Publishing</p>
          <h2>Your marketplace listings</h2>
          <p>
            Publish from the sharing page, then manage visibility and updates
            here.
          </p>
        </div>
        <button
          className="marketplace-primary-button"
          type="button"
          onClick={onGoToPublish}
        >
          Publish a TC
        </button>
      </div>

      {error && <p className="marketplace-banner marketplace-banner-error">{error}</p>}

      {isLoading ? (
        <p className="marketplace-muted">Loading your listings...</p>
      ) : listings.length === 0 ? (
        <p className="marketplace-muted">
          You have not published any marketplace listings yet.
        </p>
      ) : (
        <div className="marketplace-management-list">
          {listings.map((listing) => (
            <article className="marketplace-management-row" key={listing.id}>
              <div>
                <h3>{listing.publicTitle}</h3>
                <p>
                  {listing.courseCode} - {listing.schoolSem} - {listing.topic}
                </p>
              </div>
              <span className={`marketplace-status ${listing.status.toLowerCase()}`}>
                {formatStatus(listing.status)}
              </span>
              <span>{listing.entryCount} entries</span>
              <span>{listing.upvoteCount} upvotes</span>
              <span>{listing.importCount} imports</span>
              <div className="marketplace-management-actions">
                <Link className="marketplace-secondary-button" to={`/marketplace/${listing.id}`}>
                  View
                </Link>
                <button
                  className="marketplace-secondary-button"
                  type="button"
                  disabled={actionListingId === listing.id}
                  onClick={() => onEdit(listing.id)}
                >
                  Edit
                </button>
                {listing.status !== "REMOVED" && (
                  <button
                    className="marketplace-secondary-button"
                    type="button"
                    disabled={actionListingId === listing.id}
                    onClick={() => onRepublish(listing)}
                  >
                    Update
                  </button>
                )}
                {listing.status === "PUBLISHED" && (
                  <button
                    className="marketplace-danger-button"
                    type="button"
                    disabled={actionListingId === listing.id}
                    onClick={() => onUnlist(listing)}
                  >
                    Unlist
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

function formatStatus(status: string) {
  return status
    .toLowerCase()
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
}
