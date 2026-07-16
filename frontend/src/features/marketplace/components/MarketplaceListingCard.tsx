import { Link } from "react-router-dom";
import type { MarketplaceListingSummary } from "../types/marketplaceTypes";

type MarketplaceListingCardProps = {
  listing: MarketplaceListingSummary;
};

export function MarketplaceListingCard({ listing }: MarketplaceListingCardProps) {
  return (
    <article className="marketplace-card">
      <div className="marketplace-card-header">
        <div>
          <h2>{listing.publicTitle}</h2>
          <p>by {listing.publisherDisplayName}</p>
        </div>
        <span className="marketplace-entry-count">
          {listing.entryCount} {listing.entryCount === 1 ? "entry" : "entries"}
        </span>
      </div>

      {listing.descriptionPreview && (
        <p className="marketplace-card-description">
          {listing.descriptionPreview}
        </p>
      )}

      <div className="marketplace-card-meta">
        <span>{listing.courseCode}</span>
        <span>{listing.topic}</span>
      </div>

      {listing.tags.length > 0 && (
        <div className="marketplace-tag-list">
          {listing.tags.map((tag) => (
            <span className="marketplace-tag" key={tag}>
              {tag}
            </span>
          ))}
        </div>
      )}

      <div className="marketplace-card-footer">
        <div className="marketplace-card-stats">
          <span>{listing.upvoteCount} upvotes</span>
          <span>{listing.importCount} imports</span>
          <span>{formatDate(listing.publishedAt)}</span>
        </div>

        <Link className="marketplace-card-link" to={`/marketplace/${listing.id}`}>
          View Details
        </Link>
      </div>
    </article>
  );
}

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString(undefined, {
    dateStyle: "medium",
  });
}
