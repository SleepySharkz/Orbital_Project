import type {
  FriendSharingIndicator,
  FriendSummary,
} from "../types/friendTypes";

type FriendsListProps = {
  friends: FriendSummary[];
  sharingIndicatorsByFriendId: Record<number, FriendSharingIndicator>;
  actionFriendId: number | null;
  actionSharingRequestId: number | null;
  onCancelPendingShare: (requestId: number) => void;
  onRemoveFriend: (friendUserId: number) => void;
  onViewPendingShares: () => void;
};

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString(undefined, { dateStyle: "medium" });
}

const emptyIndicator: FriendSharingIndicator = {
  incomingRequestCount: 0,
  incomingTcCount: 0,
  outgoingRequestCount: 0,
  outgoingTcCount: 0,
  outgoingRequestId: null,
  acceptedSharedTcCount: 0,
};

export function FriendsList({
  friends,
  sharingIndicatorsByFriendId,
  actionFriendId,
  actionSharingRequestId,
  onCancelPendingShare,
  onRemoveFriend,
  onViewPendingShares,
}: FriendsListProps) {
  return (
    <section className="friends-panel">
      <div className="friends-section-heading friends-section-heading-inline">
        <div>
          <p className="friends-label">Friends</p>
          <h2>Your network</h2>
        </div>
        <span className="friends-count">{friends.length}</span>
      </div>

      {friends.length === 0 ? (
        <p className="friends-empty">No friends yet.</p>
      ) : (
        <div className="friends-person-list">
          {friends.map((friend) => {
            const indicator =
              sharingIndicatorsByFriendId[friend.userId] ?? emptyIndicator;
            const outgoingRequestId = indicator.outgoingRequestId;
            const canCancelSingleOutgoing =
              indicator.outgoingRequestCount === 1 &&
              outgoingRequestId !== null;
            const hasMultipleOutgoing = indicator.outgoingRequestCount > 1;
            const isRemoving = actionFriendId === friend.userId;
            const isCancelling =
              outgoingRequestId !== null &&
              actionSharingRequestId === outgoingRequestId;

            return (
              <article
                className="friends-person-row friends-person-row-with-actions"
                key={friend.userId}
              >
                <div className="friends-avatar" aria-hidden="true">
                  {friend.username.charAt(0).toUpperCase()}
                </div>
                <div className="friends-person-copy friends-person-copy-expanded">
                  <h3>{friend.username}</h3>
                  <p>{friend.email}</p>
                  <span>Friends since {formatDate(friend.friendsSince)}</span>
                  <FriendSharingPills indicator={indicator} />
                </div>
                <div className="friends-person-actions">
                  {canCancelSingleOutgoing && (
                    <button
                      className="friends-secondary-button"
                      type="button"
                      disabled={isCancelling}
                      onClick={() => onCancelPendingShare(outgoingRequestId)}
                    >
                      {isCancelling ? "Cancelling..." : "Cancel pending share"}
                    </button>
                  )}
                  {hasMultipleOutgoing && (
                    <button
                      className="friends-secondary-button"
                      type="button"
                      onClick={onViewPendingShares}
                    >
                      View pending shares
                    </button>
                  )}
                  <button
                    className="friends-danger-button"
                    type="button"
                    disabled={isRemoving}
                    onClick={() => onRemoveFriend(friend.userId)}
                  >
                    {isRemoving ? "Removing..." : "Remove friend"}
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}

function FriendSharingPills({
  indicator,
}: {
  indicator: FriendSharingIndicator;
}) {
  const hasIndicators =
    indicator.incomingRequestCount > 0 ||
    indicator.outgoingRequestCount > 0 ||
    indicator.acceptedSharedTcCount > 0;

  if (!hasIndicators) {
    return null;
  }

  return (
    <div className="friends-sharing-pills" aria-label="TC sharing status">
      {indicator.incomingRequestCount > 0 && (
        <span className="friends-sharing-pill">
          {indicator.incomingTcCount} incoming{" "}
          {indicator.incomingTcCount === 1 ? "TC" : "TCs"}
        </span>
      )}
      {indicator.outgoingRequestCount > 0 && (
        <span className="friends-sharing-pill">
          {indicator.outgoingTcCount}{" "}
          {indicator.outgoingTcCount === 1 ? "TC" : "TCs"} pending
        </span>
      )}
      {indicator.acceptedSharedTcCount > 0 && (
        <span className="friends-sharing-pill">
          {indicator.acceptedSharedTcCount} shared{" "}
          {indicator.acceptedSharedTcCount === 1 ? "TC" : "TCs"}
        </span>
      )}
    </div>
  );
}
