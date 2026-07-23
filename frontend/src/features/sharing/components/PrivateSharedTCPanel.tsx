import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  fetchSharedTCById,
  fetchSharedTCs,
} from "../api/tcSharingApi";
import type {
  SharedTCDetail,
  SharedTCSummary,
} from "../types/tcSharingTypes";
import { ReceivedTCDetailOverlay } from "./ReceivedTCDetailOverlay";

type PrivateSharedTCPanelProps = {
  token: string;
};

function formatDateTime(value: string | null) {
  if (!value) {
    return "Not available";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleString(undefined, {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function PrivateSharedTCPanel({ token }: PrivateSharedTCPanelProps) {
  const navigate = useNavigate();
  const { sharedTcId } = useParams();
  const parsedSharedTcId = useMemo(() => {
    if (!sharedTcId) {
      return null;
    }

    const value = Number(sharedTcId);
    return Number.isInteger(value) && value > 0 ? value : null;
  }, [sharedTcId]);

  const [sharedTcs, setSharedTcs] = useState<SharedTCSummary[]>([]);
  const [selectedSharedTc, setSelectedSharedTc] =
    useState<SharedTCDetail | null>(null);
  const [isListLoading, setIsListLoading] = useState(true);
  const [isDetailLoading, setIsDetailLoading] = useState(false);
  const [listError, setListError] = useState("");
  const [detailError, setDetailError] = useState("");

  useEffect(() => {
    async function loadSharedTcs() {
      try {
        setListError("");
        setIsListLoading(true);
        setSharedTcs(await fetchSharedTCs(token));
      } catch (caughtError) {
        setSharedTcs([]);
        setListError(
          caughtError instanceof Error
            ? caughtError.message
            : "Could not load shared TCs.",
        );
      } finally {
        setIsListLoading(false);
      }
    }

    void loadSharedTcs();
  }, [token]);

  useEffect(() => {
    async function loadSelectedSharedTc() {
      if (parsedSharedTcId === null) {
        setSelectedSharedTc(null);
        setDetailError(sharedTcId ? "Invalid shared TC id." : "");
        setIsDetailLoading(false);
        return;
      }

      try {
        setDetailError("");
        setIsDetailLoading(true);
        setSelectedSharedTc(await fetchSharedTCById(parsedSharedTcId, token));
      } catch (caughtError) {
        setSelectedSharedTc(null);
        setDetailError(
          caughtError instanceof Error
            ? caughtError.message
            : "Could not load shared TC.",
        );
      } finally {
        setIsDetailLoading(false);
      }
    }

    void loadSelectedSharedTc();
  }, [parsedSharedTcId, sharedTcId, token]);

  const isDetailOpen = isDetailLoading || Boolean(detailError) || Boolean(selectedSharedTc);

  return (
    <>
      <section className="tc-section">
        {isListLoading && (
          <div className="tc-panel">
            <p className="tc-helper-copy">Loading shared TCs...</p>
          </div>
        )}

        {!isListLoading && listError && (
          <div className="tc-panel">
            <p className="tc-banner tc-banner-error">{listError}</p>
          </div>
        )}

        {!isListLoading && !listError && sharedTcs.length === 0 && (
          <div className="tc-panel">
            <p className="tc-helper-copy">
              No shared TCs yet. Accept a compatible sharing request first.
            </p>
          </div>
        )}

        {!isListLoading && !listError && sharedTcs.length > 0 && (
          <div className="tc-card-list">
            {sharedTcs.map((sharedTc) => (
              <button
                className={
                  parsedSharedTcId === sharedTc.id
                    ? "tc-card tc-card-selected"
                    : "tc-card"
                }
                key={sharedTc.id}
                type="button"
                onClick={() => navigate(`/shared-tcs/${sharedTc.id}`)}
              >
                <div className="tc-card-main">
                  <div className="tc-card-kicker">
                    <span>{sharedTc.courseCode}</span>
                    <span>{sharedTc.schoolSem}</span>
                  </div>
                  <h2 className="tc-card-title">{sharedTc.topic}</h2>
                  <div className="tc-card-meta">
                    <span>Shared by {sharedTc.sharedByUsername}</span>
                    <span>
                      {sharedTc.entryCount}{" "}
                      {sharedTc.entryCount === 1 ? "entry" : "entries"}
                    </span>
                    <span>Accepted {formatDateTime(sharedTc.acceptedAt)}</span>
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
        title={selectedSharedTc?.topic}
        subtitle={
          selectedSharedTc
            ? `${selectedSharedTc.courseCode} - ${selectedSharedTc.schoolSem} - Accepted ${formatDateTime(selectedSharedTc.acceptedAt)}`
            : undefined
        }
        entries={selectedSharedTc?.entries}
        showSourceMaterial
        loadingMessage="Loading shared TC..."
        ariaLabel="Shared TC overlay"
        onClose={() => navigate("/shared-tcs")}
      />
    </>
  );
}
