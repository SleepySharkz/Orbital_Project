import type { FormEvent } from "react";
import type { UserSearchResult } from "../types/friendTypes";

type FriendSearchPanelProps = {
  query: string;
  results: UserSearchResult[];
  hasSearched: boolean;
  isSearching: boolean;
  actionUserId: number | null;
  onQueryChange: (query: string) => void;
  onSearch: () => Promise<void>;
  onSendRequest: (recipientUserId: number) => Promise<void>;
};

function relationshipLabel(result: UserSearchResult) {
  if (result.isSelf) {
    return "This is you";
  }

  if (result.isFriend) {
    return "Already friends";
  }

  if (result.incomingRequestPending) {
    return "Sent you a request";
  }

  if (result.outgoingRequestPending) {
    return "Request pending";
  }

  return null;
}

export function FriendSearchPanel({
  query,
  results,
  hasSearched,
  isSearching,
  actionUserId,
  onQueryChange,
  onSearch,
  onSendRequest,
}: FriendSearchPanelProps) {
  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void onSearch();
  }

  return (
    <section className="friends-panel friends-search-panel">
      <div className="friends-section-heading">
        <div>
          <p className="friends-label">Find people</p>
          <h2>Search by email</h2>
        </div>
        <p>Find another MindMesh user and send a friend request.</p>
      </div>

      <form className="friends-search-form" onSubmit={handleSubmit}>
        <label className="friends-sr-only" htmlFor="friend-email-search">
          Search users by email
        </label>
        <input
          id="friend-email-search"
          className="friends-input"
          type="search"
          placeholder="student@example.com"
          value={query}
          onChange={(event) => onQueryChange(event.target.value)}
        />
        <button
          className="friends-primary-button"
          type="submit"
          disabled={isSearching}
        >
          {isSearching ? "Searching..." : "Search"}
        </button>
      </form>

      {hasSearched && !isSearching && results.length === 0 && (
        <p className="friends-empty">No users matched that email.</p>
      )}

      {results.length > 0 && (
        <div className="friends-result-list">
          {results.map((result) => {
            const statusLabel = relationshipLabel(result);
            const canSend = statusLabel === null;
            const isSending = actionUserId === result.id;

            return (
              <article className="friends-person-row" key={result.id}>
                <div className="friends-avatar" aria-hidden="true">
                  {result.username.charAt(0).toUpperCase()}
                </div>
                <div className="friends-person-copy">
                  <h3>{result.username}</h3>
                  <p>{result.email}</p>
                </div>

                {canSend ? (
                  <button
                    className="friends-secondary-button"
                    type="button"
                    disabled={isSending}
                    onClick={() => void onSendRequest(result.id)}
                  >
                    {isSending ? "Sending..." : "Add friend"}
                  </button>
                ) : (
                  <span className="friends-status-pill">{statusLabel}</span>
                )}
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}
