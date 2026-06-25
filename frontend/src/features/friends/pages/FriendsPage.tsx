import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../auth/context/AuthContext";
import { ModulesSidebar } from "../../modules/components/ModulesSidebar";
import "../../modules/styles/modulesStyles.css";
import {
  acceptFriendRequest,
  declineFriendRequest,
  fetchFriends,
  fetchIncomingFriendRequests,
  fetchOutgoingFriendRequests,
  searchUsersByEmail,
  sendFriendRequest,
} from "../api/friendsApi";
import { FriendSearchPanel } from "../components/FriendSearchPanel";
import { FriendsList } from "../components/FriendsList";
import { IncomingRequests } from "../components/IncomingRequests";
import { OutgoingRequests } from "../components/OutgoingRequests";
import type {
  FriendRequest,
  FriendSummary,
  UserSearchResult,
} from "../types/friendTypes";
import "../styles/friendsStyles.css";

export function FriendsPage() {
  const navigate = useNavigate();
  const { user, token, logout } = useAuth();
  const [friends, setFriends] = useState<FriendSummary[]>([]);
  const [incomingRequests, setIncomingRequests] = useState<FriendRequest[]>([]);
  const [outgoingRequests, setOutgoingRequests] = useState<FriendRequest[]>([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<UserSearchResult[]>([]);
  const [hasSearched, setHasSearched] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [isSearching, setIsSearching] = useState(false);
  const [actionRequestId, setActionRequestId] = useState<number | null>(null);
  const [actionUserId, setActionUserId] = useState<number | null>(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  useEffect(() => {
    async function loadFriendsPage() {
      if (!token) {
        setIsLoading(false);
        return;
      }

      try {
        setError("");
        setIsLoading(true);
        const [nextFriends, nextIncoming, nextOutgoing] = await Promise.all([
          fetchFriends(token),
          fetchIncomingFriendRequests(token),
          fetchOutgoingFriendRequests(token),
        ]);
        setFriends(nextFriends);
        setIncomingRequests(nextIncoming);
        setOutgoingRequests(nextOutgoing);
      } catch (caughtError) {
        setError(toErrorMessage(caughtError, "Could not load friends."));
      } finally {
        setIsLoading(false);
      }
    }

    void loadFriendsPage();
  }, [token]);

  async function refreshSearchResults() {
    const normalizedQuery = searchQuery.trim();

    if (!token || !normalizedQuery || !hasSearched) {
      return;
    }

    const results = await searchUsersByEmail(normalizedQuery, token);
    setSearchResults(results);
  }

  async function handleSearch() {
    const normalizedQuery = searchQuery.trim();

    if (!token) {
      return;
    }

    if (!normalizedQuery) {
      setError("Enter an email address to search.");
      setSuccess("");
      setSearchResults([]);
      setHasSearched(false);
      return;
    }

    try {
      setError("");
      setSuccess("");
      setIsSearching(true);
      const results = await searchUsersByEmail(normalizedQuery, token);
      setSearchResults(results);
      setHasSearched(true);
    } catch (caughtError) {
      setSearchResults([]);
      setHasSearched(true);
      setError(toErrorMessage(caughtError, "Could not search users."));
    } finally {
      setIsSearching(false);
    }
  }

  async function handleSendRequest(recipientUserId: number) {
    if (!token) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      setActionUserId(recipientUserId);
      await sendFriendRequest(recipientUserId, token);
      const nextOutgoing = await fetchOutgoingFriendRequests(token);
      setOutgoingRequests(nextOutgoing);
      await refreshSearchResults();
      setSuccess("Friend request sent.");
    } catch (caughtError) {
      setError(toErrorMessage(caughtError, "Could not send friend request."));
    } finally {
      setActionUserId(null);
    }
  }

  async function handleAcceptRequest(requestId: number) {
    if (!token) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      setActionRequestId(requestId);
      await acceptFriendRequest(requestId, token);
      const [nextFriends, nextIncoming] = await Promise.all([
        fetchFriends(token),
        fetchIncomingFriendRequests(token),
      ]);
      setFriends(nextFriends);
      setIncomingRequests(nextIncoming);
      await refreshSearchResults();
      setSuccess("Friend request accepted.");
    } catch (caughtError) {
      setError(toErrorMessage(caughtError, "Could not accept friend request."));
    } finally {
      setActionRequestId(null);
    }
  }

  async function handleDeclineRequest(requestId: number) {
    if (!token) {
      return;
    }

    try {
      setError("");
      setSuccess("");
      setActionRequestId(requestId);
      await declineFriendRequest(requestId, token);
      const nextIncoming = await fetchIncomingFriendRequests(token);
      setIncomingRequests(nextIncoming);
      await refreshSearchResults();
      setSuccess("Friend request declined.");
    } catch (caughtError) {
      setError(toErrorMessage(caughtError, "Could not decline friend request."));
    } finally {
      setActionRequestId(null);
    }
  }

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

      <main className="modules-main friends-main">
        <header className="friends-header">
          <div>
            <p className="friends-eyebrow">Friends</p>
            <h1>Build your learning network</h1>
            <p>
              Connect with people you trust. Build knowledge through private sharing.
            </p>
          </div>
          <div className="friends-header-stat">
            <strong>{friends.length}</strong>
            <span>{friends.length === 1 ? "friend" : "friends"}</span>
          </div>
        </header>

        {error && <p className="friends-banner friends-banner-error">{error}</p>}
        {success && (
          <p className="friends-banner friends-banner-success">{success}</p>
        )}

        {isLoading ? (
          <section className="friends-panel">
            <p className="friends-empty">Loading your friend network...</p>
          </section>
        ) : (
          <div className="friends-content">
            <FriendSearchPanel
              query={searchQuery}
              results={searchResults}
              hasSearched={hasSearched}
              isSearching={isSearching}
              actionUserId={actionUserId}
              onQueryChange={setSearchQuery}
              onSearch={handleSearch}
              onSendRequest={handleSendRequest}
            />

            <div className="friends-dashboard-grid">
              <FriendsList friends={friends} />
              <IncomingRequests
                requests={incomingRequests}
                actionRequestId={actionRequestId}
                onAccept={handleAcceptRequest}
                onDecline={handleDeclineRequest}
              />
            </div>

            <OutgoingRequests requests={outgoingRequests} />
          </div>
        )}
      </main>
    </div>
  );
}

function toErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}
