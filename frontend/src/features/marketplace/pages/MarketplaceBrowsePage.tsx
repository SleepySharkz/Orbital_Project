import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/useAuth";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import {
  fetchMarketplaceListings,
  fetchMyMarketplaceListings,
  fetchMyMarketplaceListingDetail,
  republishMyMarketplaceListing,
  unlistMyMarketplaceListing,
  updateMyMarketplaceListingMetadata,
} from "../api/marketplaceApi";
import { MarketplaceConfirmModal } from "../components/MarketplaceConfirmModal";
import { MarketplaceListingCard } from "../components/MarketplaceListingCard";
import { MarketplaceListingEditModal } from "../components/MarketplaceListingEditModal";
import { MyMarketplaceListingsPanel } from "../components/MyMarketplaceListingsPanel";
import { MarketplacePagination } from "../components/MarketplacePagination";
import { MarketplaceSearchBar } from "../components/MarketplaceSearchBar";
import "../styles/marketplaceStyles.css";
import type {
  MarketplaceBrowseParams,
  MarketplaceListingManagementDetail,
  MarketplaceListingManagementPage,
  MarketplaceListingManagementSummary,
  MarketplaceListingPage,
  MarketplaceSort,
  UpdateMarketplaceListingMetadataRequest,
} from "../types/marketplaceTypes";

const DEFAULT_PAGE_SIZE = 12;

const emptyPage: MarketplaceListingPage = {
  items: [],
  page: 0,
  size: DEFAULT_PAGE_SIZE,
  totalItems: 0,
  totalPages: 0,
  hasNext: false,
};

const emptyManagementPage: MarketplaceListingManagementPage = {
  items: [],
  page: 0,
  size: DEFAULT_PAGE_SIZE,
  totalItems: 0,
  totalPages: 0,
  hasNext: false,
};

type MarketplaceTab = "browse" | "publish";
type ConfirmAction =
  | { type: "unlist"; listing: MarketplaceListingManagementSummary }
  | { type: "republish"; listing: MarketplaceListingManagementSummary };

export function MarketplaceBrowsePage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [activeTab, setActiveTab] = useState<MarketplaceTab>("browse");
  const [searchText, setSearchText] = useState("");
  const [courseCode, setCourseCode] = useState("");
  const [topic, setTopic] = useState("");
  const [tag, setTag] = useState("");
  const [sort, setSort] = useState<MarketplaceSort>("newest");
  const [browseParams, setBrowseParams] = useState<MarketplaceBrowseParams>({
    sort: "newest",
    page: 0,
    size: DEFAULT_PAGE_SIZE,
  });
  const [listingPage, setListingPage] =
    useState<MarketplaceListingPage>(emptyPage);
  const [myListingsPage, setMyListingsPage] =
    useState<MarketplaceListingManagementPage>(emptyManagementPage);
  const [isLoading, setIsLoading] = useState(true);
  const [isMyListingsLoading, setIsMyListingsLoading] = useState(false);
  const [actionListingId, setActionListingId] = useState<number | null>(null);
  const [editingListing, setEditingListing] =
    useState<MarketplaceListingManagementDetail | null>(null);
  const [confirmAction, setConfirmAction] = useState<ConfirmAction | null>(null);
  const [isSavingEdit, setIsSavingEdit] = useState(false);
  const [error, setError] = useState("");
  const [myListingsError, setMyListingsError] = useState("");
  const [editError, setEditError] = useState("");

  useEffect(() => {
    if (!token) {
      Promise.resolve().then(() => {
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
        return fetchMarketplaceListings(browseParams, token);
      })
      .then((nextPage) => {
        if (!didCancel && nextPage) {
          setListingPage(nextPage);
        }
      })
      .catch((caughtError) => {
        if (!didCancel) {
          setError(toErrorMessage(caughtError, "Could not load marketplace."));
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
  }, [browseParams, token]);

  useEffect(() => {
    if (!token || activeTab !== "publish") {
      return undefined;
    }

    let didCancel = false;

    Promise.resolve()
      .then(() => {
        if (didCancel) {
          return null;
        }

        setMyListingsError("");
        setIsMyListingsLoading(true);
        return fetchMyMarketplaceListings(token);
      })
      .then((nextPage) => {
        if (!didCancel && nextPage) {
          setMyListingsPage(nextPage);
        }
      })
      .catch((caughtError) => {
        if (!didCancel) {
          setMyListingsError(
            toErrorMessage(caughtError, "Could not load your listings."),
          );
        }
      })
      .finally(() => {
        if (!didCancel) {
          setIsMyListingsLoading(false);
        }
      });

    return () => {
      didCancel = true;
    };
  }, [activeTab, token]);

  async function refreshMyListings() {
    if (!token) {
      return;
    }

    const nextPage = await fetchMyMarketplaceListings(token);
    setMyListingsPage(nextPage);
  }

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  function handleSubmitSearch() {
    setBrowseParams({
      q: searchText,
      module: courseCode,
      topic,
      tag,
      sort,
      page: 0,
      size: DEFAULT_PAGE_SIZE,
    });
  }

  function handleClearSearch() {
    setSearchText("");
    setCourseCode("");
    setTopic("");
    setTag("");
    setSort("newest");
    setBrowseParams({
      sort: "newest",
      page: 0,
      size: DEFAULT_PAGE_SIZE,
    });
  }

  function handlePageChange(page: number) {
    setBrowseParams((currentParams) => ({
      ...currentParams,
      page,
    }));
  }

  async function handleEditListing(listingId: number) {
    if (!token) {
      return;
    }

    try {
      setMyListingsError("");
      setEditError("");
      setActionListingId(listingId);
      const detail = await fetchMyMarketplaceListingDetail(listingId, token);
      setEditingListing(detail);
    } catch (caughtError) {
      setMyListingsError(
        toErrorMessage(caughtError, "Could not load this listing for editing."),
      );
    } finally {
      setActionListingId(null);
    }
  }

  async function handleSaveListingMetadata(
      listingId: number,
      request: UpdateMarketplaceListingMetadataRequest) {
    if (!token) {
      return;
    }

    try {
      setEditError("");
      setIsSavingEdit(true);
      const updatedListing = await updateMyMarketplaceListingMetadata(
        listingId,
        request,
        token,
      );
      await refreshMyListings();
      setEditingListing(updatedListing);
      setEditingListing(null);
    } catch (caughtError) {
      setEditError(
        toErrorMessage(caughtError, "Could not update this listing."),
      );
    } finally {
      setIsSavingEdit(false);
    }
  }

  async function handleConfirmAction() {
    if (!confirmAction) {
      return;
    }

    if (confirmAction.type === "unlist") {
      await handleUnlistListing(confirmAction.listing);
      return;
    }

    await handleRepublishListing(confirmAction.listing);
  }

  async function handleUnlistListing(listing: MarketplaceListingManagementSummary) {
    if (!token) {
      return;
    }

    try {
      setMyListingsError("");
      setActionListingId(listing.id);
      await unlistMyMarketplaceListing(listing.id, token);
      await refreshMyListings();
      setConfirmAction(null);
    } catch (caughtError) {
      setMyListingsError(
        toErrorMessage(caughtError, "Could not unlist this listing."),
      );
    } finally {
      setActionListingId(null);
    }
  }

  async function handleRepublishListing(listing: MarketplaceListingManagementSummary) {
    if (!token) {
      return;
    }

    try {
      setMyListingsError("");
      setActionListingId(listing.id);
      await republishMyMarketplaceListing(listing.id, token);
      await refreshMyListings();
      setConfirmAction(null);
    } catch (caughtError) {
      setMyListingsError(
        toErrorMessage(caughtError, "Could not update this listing snapshot."),
      );
    } finally {
      setActionListingId(null);
    }
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="modules-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="modules-main marketplace-main">
        <header className="marketplace-header">
          <div>
            <h1>Marketplace</h1>
            <p>
              Browse public topic sheet snapshots or manage your own published
              listings.
            </p>
          </div>
        </header>

        <div className="marketplace-tabs" role="tablist" aria-label="Marketplace sections">
          <button
            className={activeTab === "browse" ? "active" : ""}
            type="button"
            onClick={() => setActiveTab("browse")}
          >
            Public marketplace
          </button>
          <button
            className={activeTab === "publish" ? "active" : ""}
            type="button"
            onClick={() => setActiveTab("publish")}
          >
            Publishing
          </button>
        </div>

        {activeTab === "browse" ? (
          <>
            <MarketplaceSearchBar
              searchText={searchText}
              courseCode={courseCode}
              topic={topic}
              tag={tag}
              sort={sort}
              disabled={isLoading}
              onSearchTextChange={setSearchText}
              onCourseCodeChange={setCourseCode}
              onTopicChange={setTopic}
              onTagChange={setTag}
              onSortChange={setSort}
              onSubmit={handleSubmitSearch}
              onClear={handleClearSearch}
            />

            {error && <p className="marketplace-banner marketplace-banner-error">{error}</p>}

            {isLoading ? (
              <section className="marketplace-empty-panel">
                <p>Loading public listings...</p>
              </section>
            ) : listingPage.items.length === 0 ? (
              <section className="marketplace-empty-panel">
                <h2>No marketplace listings found</h2>
                <p>
                  Try a broader search, remove a filter, or check again after
                  more topic sheets have been published.
                </p>
              </section>
            ) : (
              <>
                <section className="marketplace-card-grid" aria-label="Marketplace listings">
                  {listingPage.items.map((listing) => (
                    <MarketplaceListingCard listing={listing} key={listing.id} />
                  ))}
                </section>

                <MarketplacePagination
                  page={listingPage.page}
                  totalPages={listingPage.totalPages}
                  totalItems={listingPage.totalItems}
                  hasNext={listingPage.hasNext}
                  disabled={isLoading}
                  onPageChange={handlePageChange}
                />
              </>
            )}
          </>
        ) : (
          <MyMarketplaceListingsPanel
            listings={myListingsPage.items}
            isLoading={isMyListingsLoading}
            error={myListingsError}
            actionListingId={actionListingId}
            onGoToPublish={() => navigate("/sharing?tab=publish")}
            onEdit={(listingId) => void handleEditListing(listingId)}
            onRepublish={(listing) => setConfirmAction({ type: "republish", listing })}
            onUnlist={(listing) => setConfirmAction({ type: "unlist", listing })}
          />
        )}

        {confirmAction && (
          <MarketplaceConfirmModal
            title={
              confirmAction.type === "unlist"
                ? `Unlist "${confirmAction.listing.publicTitle}"?`
                : `Update "${confirmAction.listing.publicTitle}"?`
            }
            description={
              confirmAction.type === "unlist"
                ? "This hides the listing from public browse and public detail, and prevents future imports. The existing snapshot and stats are kept."
                : "This replaces the published entries with a fresh snapshot from the current source TC. Metadata, listing ID, upvotes, and import stats are kept."
            }
            confirmLabel={confirmAction.type === "unlist" ? "Unlist" : "Update"}
            isDanger={confirmAction.type === "unlist"}
            isWorking={actionListingId === confirmAction.listing.id}
            onCancel={() => setConfirmAction(null)}
            onConfirm={() => void handleConfirmAction()}
          />
        )}

        {editingListing && (
          <MarketplaceListingEditModal
            key={editingListing.id}
            listing={editingListing}
            isSaving={isSavingEdit}
            error={editError}
            onSave={(listingId, request) =>
              void handleSaveListingMetadata(listingId, request)
            }
            onClose={() => {
              setEditError("");
              setEditingListing(null);
            }}
          />
        )}
      </main>
    </div>
  );
}

function toErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}
