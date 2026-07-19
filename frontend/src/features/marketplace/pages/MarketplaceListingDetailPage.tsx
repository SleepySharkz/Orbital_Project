import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../../auth/context/useAuth";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import "../../tc/styles/tcStyles.css";
import {
  fetchMarketplaceListingDetail,
  addMarketplaceListingUpvote,
  removeMarketplaceListingUpvote,
  reportMarketplaceListing,
} from "../api/marketplaceApi";
import { MarketplaceReportModal } from "../components/MarketplaceReportModal";
import "../styles/marketplaceStyles.css";
import type {
  CreateMarketplaceReportRequest,
  MarketplaceListingDetail,
} from "../types/marketplaceTypes";

export function MarketplaceListingDetailPage() {
  const { listingId } = useParams();
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [listing, setListing] = useState<MarketplaceListingDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [isUpdatingUpvote, setIsUpdatingUpvote] = useState(false);
  const [upvoteError, setUpvoteError] = useState("");
  const [isReportModalOpen, setIsReportModalOpen] = useState(false);
  const [isSubmittingReport, setIsSubmittingReport] = useState(false);
  const [reportError, setReportError] = useState("");

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

  async function handleToggleUpvote() {
    if (!listing || !token || isUpdatingUpvote) {
      // Prevent ghost input
      return;
    }

    setIsUpdatingUpvote(true); // Block other racing upvotes
    setUpvoteError(""); // Reset

    try {
      const response = listing.hasCurrentUserUpvoted
        ? await removeMarketplaceListingUpvote(listing.id, token)
        : await addMarketplaceListingUpvote(listing.id, token);

      setListing((currentListing) => {
        // Protect against race condition / stale response scenario
        if (!currentListing || currentListing.id !== response.listingId) { // Check if its still the same listing
          return currentListing;
        }

        return {
          ...currentListing,
          upvoteCount: response.upvoteCount,
          hasCurrentUserUpvoted: response.hasCurrentUserUpvoted,
        };
      });
    } catch (caughtError) {
      setUpvoteError(
        toErrorMessage(caughtError, "Could not update your upvote."),
      );
    } finally {
      setIsUpdatingUpvote(false);
    }
  }

  async function handleSubmitReport(request: CreateMarketplaceReportRequest) {
    if (!listing || !token || isSubmittingReport || listing.hasCurrentUserReported) {
      return;
    }

    setIsSubmittingReport(true);
    setReportError("");

    try {
      const response = await reportMarketplaceListing(listing.id, request, token);

      setListing((currentListing) => {
        if (!currentListing || currentListing.id !== response.listingId) {
          return currentListing;
        }

        return {
          ...currentListing,
          hasCurrentUserReported: true,
        };
      });
      setIsReportModalOpen(false);
    } catch (caughtError) {
      setReportError(
        toErrorMessage(caughtError, "Could not report this listing."),
      );
    } finally {
      setIsSubmittingReport(false);
    }
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
                  <h2>Cheatsheet preview</h2>
                  <span>
                    {listing.entryCount}{" "}
                    {listing.entryCount === 1 ? "entry" : "entries"}
                  </span>
                </div>

                <div className="marketplace-sheet">
                  {listing.entries.map((entry) => (
                    <article className="marketplace-sheet-entry" key={entry.id}>
                      <div className="tc-entry-header">
                        <h2 className="tc-entry-question">
                          {entry.flashcardQuestion}
                        </h2>
                      </div>

                      <div className="tc-note-block">
                        {entry.flashcardNoteContent
                          .split("\n")
                          .map((line, lineIndex) => (
                            <p
                              className="tc-note-line"
                              key={`${entry.id}-${lineIndex}`}
                            >
                              {line}
                            </p>
                          ))}
                      </div>
                    </article>
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
              <button
                className={`marketplace-primary-button marketplace-upvote-action${
                  listing.hasCurrentUserUpvoted ? " upvoted" : ""
                }`}
                type="button"
                aria-pressed={listing.hasCurrentUserUpvoted}
                disabled={isUpdatingUpvote}
                onClick={handleToggleUpvote}
              >
                {listing.hasCurrentUserUpvoted ? "Upvoted" : "Upvote"}
              </button>
              {upvoteError && (
                <p className="marketplace-banner marketplace-banner-error" role="alert">
                  {upvoteError}
                </p>
              )}
              <button
                className={`marketplace-primary-button marketplace-report-action${
                  listing.hasCurrentUserReported ? " reported" : ""
                }`}
                type="button"
                disabled={listing.hasCurrentUserReported || isSubmittingReport}
                onClick={() => {
                  setReportError("");
                  setIsReportModalOpen(true);
                }}
              >
                {listing.hasCurrentUserReported ? "Reported" : "Report"}
              </button>
              <button className="marketplace-primary-button" type="button" disabled>
                Import coming soon
              </button>
            </aside>
          </div>
        )}

        {isReportModalOpen && listing && (
          <MarketplaceReportModal
            isSubmitting={isSubmittingReport}
            error={reportError}
            onSubmit={(request) => void handleSubmitReport(request)}
            onCancel={() => {
              if (!isSubmittingReport) {
                setReportError("");
                setIsReportModalOpen(false);
              }
            }}
          />
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
