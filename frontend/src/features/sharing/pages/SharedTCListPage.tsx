import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../../auth/context/useAuth";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import { MarketplaceImportsPanel } from "../components/MarketplaceImportsPanel";
import { PrivateSharedTCPanel } from "../components/PrivateSharedTCPanel";
import "../../friends/styles/friendsStyles.css";
import "../../modules/styles/modulesStyles.css";
import "../../tc/styles/tcStyles.css";
import "../styles/sharingStyles.css";

type SharedContentTab = "private" | "marketplace";

export function SharedTCListPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user, token, logout } = useAuth();
  const activeTab: SharedContentTab =
    searchParams.get("tab") === "marketplace" ? "marketplace" : "private";

  async function handleLogout() {
    await logout();
    navigate("/login");
  }

  if (!user || !token) {
    return null;
  }

  return (
    <div className="tc-page">
      <ModulesSidebar user={user} onLogout={handleLogout} />

      <main className="tc-main">
        <header className="tc-header">
          <h1 className="tc-title">Your received TCs</h1>
          <p className="tc-subtitle">
            Review topic sheets shared by friends and saved from the public
            marketplace.
          </p>
        </header>

        <div
          className="sharing-tabs"
          role="tablist"
          aria-label="Received TC categories"
        >
          <button
            className={activeTab === "private" ? "active" : ""}
            type="button"
            role="tab"
            aria-selected={activeTab === "private"}
            onClick={() => navigate("/shared-tcs")}
          >
            Privately Shared
          </button>

          <button
            className={activeTab === "marketplace" ? "active" : ""}
            type="button"
            role="tab"
            aria-selected={activeTab === "marketplace"}
            onClick={() => navigate("/shared-tcs?tab=marketplace")}
          >
            Marketplace Imports
          </button>
        </div>

        {activeTab === "private" ? (
          <PrivateSharedTCPanel token={token} />
        ) : (
          <MarketplaceImportsPanel token={token} />
        )}
      </main>
    </div>
  );
}
