import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
  fetchMarketplaceImportDetail,
  fetchMarketplaceImports,
} from "../../marketplace/api/marketplaceApi";
import type {
  MarketplaceImportDetail,
  MarketplaceImportSummary,
} from "../../marketplace/types/marketplaceTypes";
import { ReceivedTCDetailOverlay } from "./ReceivedTCDetailOverlay";

type MarketplaceImportsPanelProps = {
  token: string;
};

function formatDateTime(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function MarketplaceImportsPanel({ token }: MarketplaceImportsPanelProps) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const selectedImportId = useMemo(() => {
    const value = Number(searchParams.get("importId"));
    return Number.isInteger(value) && value > 0 ? value : null;
  }, [searchParams]);

  const [marketplaceImports, setMarketplaceImports] = useState<
    MarketplaceImportSummary[]
  >([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedImport, setSelectedImport] =
    useState<MarketplaceImportDetail | null>(null);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [detailError, setDetailError] = useState("");

  useEffect(() => {
    async function loadMarketplaceImports() {
      try {
        setError("");
        setIsLoading(true);
        setMarketplaceImports(await fetchMarketplaceImports(token));
      } catch (caughtError) {
        setMarketplaceImports([]);
        setError(
          caughtError instanceof Error
            ? caughtError.message
            : "Could not load marketplace imports.",
        );
      } finally {
        setIsLoading(false);
      }
    }

    void loadMarketplaceImports();
  }, [token]);

  useEffect(() => {
    async function loadSelectedImport() {
      if (selectedImportId === null) {
        setSelectedImport(null);
        setDetailError("");
        setIsDetailLoading(false);
        return;
      }

      try {
        setDetailError("");
        setSelectedImport(null);
        setIsDetailLoading(true);
        setSelectedImport(
          await fetchMarketplaceImportDetail(selectedImportId, token),
        );
      } catch (caughtError) {
        setSelectedImport(null);
        setDetailError(
          caughtError instanceof Error
            ? caughtError.message
            : "Could not load this marketplace import.",
        );
      } finally {
        setIsDetailLoading(false);
      }
    }

    void loadSelectedImport();
  }, [selectedImportId, token]);

  const isDetailOpen =
    selectedImportId !== null &&
    (isDetailLoading || Boolean(detailError) || Boolean(selectedImport));

  return (
    <>
      <section className="tc-section">
      {isLoading && (
        <div className="tc-panel">
          <p className="tc-helper-copy">Loading marketplace imports...</p>
        </div>
      )}

      {!isLoading && error && (
        <div className="tc-panel">
          <p className="tc-banner tc-banner-error">{error}</p>
        </div>
      )}

      {!isLoading && !error && marketplaceImports.length === 0 && (
        <div className="tc-panel">
          <p className="tc-helper-copy">
            You have not imported any marketplace listings yet.
          </p>
        </div>
      )}

      {!isLoading && !error && marketplaceImports.length > 0 && (
        <div className="tc-card-list">
          {marketplaceImports.map((marketplaceImport) => (
            <button
              className={
                selectedImportId === marketplaceImport.id
                  ? "tc-card tc-card-selected"
                  : "tc-card"
              }
              key={marketplaceImport.id}
              type="button"
              onClick={() =>
                navigate(
                  `/shared-tcs?tab=marketplace&importId=${marketplaceImport.id}`,
                )
              }
            >
              <div className="tc-card-main">
                <div className="tc-card-kicker">
                  <span>Public Marketplace</span>
                </div>
                <h2 className="tc-card-title">
                  {marketplaceImport.sourceListingTitle}
                </h2>
                <div className="tc-card-meta">
                  <span>{marketplaceImport.courseCode}</span>
                  <span>{marketplaceImport.schoolSem}</span>
                  <span>{marketplaceImport.topic}</span>
                </div>
                <div className="tc-card-meta">
                  <span>
                    Published by {marketplaceImport.sourcePublisherDisplayName}
                  </span>
                  <span>
                    {marketplaceImport.entryCount}{" "}
                    {marketplaceImport.entryCount === 1 ? "entry" : "entries"}
                  </span>
                  <span>
                    Imported {formatDateTime(marketplaceImport.importedAt)}
                  </span>
                </div>
              </div>
              <span className="tc-open-link">Open</span>
            </button>
          ))}
        </div>
      )}
      </section>

      <ReceivedTCDetailOverlay
        isOpen={isDetailOpen}
        isLoading={isDetailLoading}
        error={detailError}
        title={selectedImport?.sourceListingTitle}
        subtitle={
          selectedImport
            ? `${selectedImport.courseCode} - ${selectedImport.schoolSem} - ${selectedImport.topic} - Published by ${selectedImport.sourcePublisherDisplayName} - Imported ${formatDateTime(selectedImport.importedAt)}`
            : undefined
        }
        entries={selectedImport?.entries}
        loadingMessage="Loading marketplace import..."
        ariaLabel="Marketplace import overlay"
        onClose={() => navigate("/shared-tcs?tab=marketplace")}
      />
    </>
  );
}
