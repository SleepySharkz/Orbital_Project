import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/useAuth";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import { fetchMarketplaceListings } from "../api/marketplaceApi";
import { MarketplaceListingCard } from "../components/MarketplaceListingCard";
import { MarketplacePagination } from "../components/MarketplacePagination";
import { MarketplaceSearchBar } from "../components/MarketplaceSearchBar";
import "../styles/marketplaceStyles.css";
import type {
  MarketplaceBrowseParams,
  MarketplaceListingPage,
  MarketplaceSort,
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

export function MarketplaceBrowsePage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
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
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

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

  if (!user || !token) {
    return null;
  }

  return (
    <div className="modules-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="modules-main marketplace-main">
        <header className="marketplace-header">
          <div>
            <p className="modules-eyebrow">Marketplace</p>
            <h1>Public Marketplace</h1>
            <p>
              Browse public topic sheet snapshots shared by other MindMesh users.
            </p>
          </div>
        </header>

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
              Try a broader search, remove a filter, or check again after more
              topic sheets have been published.
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
      </main>
    </div>
  );
}

function toErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}
