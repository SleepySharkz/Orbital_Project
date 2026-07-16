import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/context/useAuth";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import { fetchMarketplaceListingDetail } from "../api/marketplaceApi";
import { MarketplaceEntryPreview } from "../components/MarketplaceEntryPreview";
import "../styles/marketplaceStyles.css";
import type { MarketplaceListingDetail } from "../types/marketplaceTypes";

export function MarketplaceListingDetailPage() {
  const { listingId } = useParams();
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [listing, setListing] = useState<MarketplaceListingDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token || !listingId) {
      Promise.resolve().then(() => {
        setIsLoading(false);
      });
      return undefined;
    }

    const numericListingId = Number(listingId);

    if (!Number.isInteger(numericListingId) || numericListingId <= 0) {
      Promise.resolve().then(() => {
        setError("Marketplace listing not found.");
        setIsLoading(false);
      });
      return undefined;
    }

    let didCancel = false;

    Promise.resolve()
      .then(() => {
        if (didCancel) {
          return null;
        }

        setError("");
        setIsLoading(true);
        return fetchMarketplaceListingDetail(numericListingId, token);
      })
      .then((nextListing) => {
        if (!didCancel && nextListing) {
          setListing(nextListing);
        }
      })
      .catch((caughtError) => {
        if (!didCancel) {
          setError(
            toErrorMessage(
              caughtError,
              "Marketplace listing could not be loaded.",
            ),
          );
        }
      })
      .finally(() => {
        if (!didCancel) {
          setIsLoading(false);
        }
      });

    return () => {
      didCancel = true;
    };
  }, [listingId, token]);

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="modules-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="modules-main marketplace-main">
        <Link className="marketplace-back-link" to="/marketplace">
          Back to marketplace
        </Link>

        {isLoading ? (
          <section className="marketplace-empty-panel">
            <p>Loading marketplace listing...</p>
          </section>
        ) : error || !listing ? (
          <section className="marketplace-empty-panel">
            <h1>Listing unavailable</h1>
            <p>
              {error ||
                "This listing may have been removed, unlisted, or made unavailable."}
            </p>
          </section>
        ) : (
          <div className="marketplace-detail-layout">
            <section className="marketplace-detail-main-panel">
              <div className="marketplace-detail-heading">
                <p className="modules-eyebrow">Listing preview</p>
                <h1>{listing.publicTitle}</h1>
                <p>
                  {listing.description ||
                    "This publisher did not add a description."}
                </p>
              </div>

              <div className="marketplace-detail-meta">
                <span>{listing.courseCode}</span>
                <span>{listing.schoolSem}</span>
                <span>{listing.topic}</span>
                {listing.institution && <span>{listing.institution}</span>}
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

              <div className="marketplace-import-note">
                Importing will create an independent copy in your own workspace.
                Imported content will not update when the publisher changes this
                listing later.
              </div>

              <section className="marketplace-entry-section">
                <div className="marketplace-section-heading">
                  <h2>Published entries</h2>
                  <span>
                    {listing.entryCount}{" "}
                    {listing.entryCount === 1 ? "entry" : "entries"}
                  </span>
                </div>

                <div className="marketplace-entry-list">
                  {listing.entries.map((entry) => (
                    <MarketplaceEntryPreview entry={entry} key={entry.id} />
                  ))}
                </div>
              </section>
            </section>

            <aside className="marketplace-detail-side-panel">
              <p className="marketplace-side-label">Publisher</p>
              <h2>{listing.publisherDisplayName}</h2>
              <dl>
                <div>
                  <dt>Upvotes</dt>
                  <dd>{listing.upvoteCount}</dd>
                </div>
                <div>
                  <dt>Imports</dt>
                  <dd>{listing.importCount}</dd>
                </div>
                <div>
                  <dt>Published</dt>
                  <dd>{formatDate(listing.publishedAt)}</dd>
                </div>
                <div>
                  <dt>Updated</dt>
                  <dd>{formatDate(listing.updatedAt)}</dd>
                </div>
              </dl>
              <button className="marketplace-primary-button" type="button" disabled>
                Import coming soon
              </button>
            </aside>
          </div>
        )}
      </main>
    </div>
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

function toErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}
