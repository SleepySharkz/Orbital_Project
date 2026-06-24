import type { FriendSummary } from "../types/friendTypes";

type FriendsListProps = {
  friends: FriendSummary[];
};

function formatDate(value: string) {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return date.toLocaleDateString(undefined, { dateStyle: "medium" });
}

export function FriendsList({ friends }: FriendsListProps) {
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
        <p className="friends-empty">
          You have no friends yet. Search by email to get started.
        </p>
      ) : (
        <div className="friends-person-list">
          {friends.map((friend) => (
            <article className="friends-person-row" key={friend.userId}>
              <div className="friends-avatar" aria-hidden="true">
                {friend.username.charAt(0).toUpperCase()}
              </div>
              <div className="friends-person-copy">
                <h3>{friend.username}</h3>
                <p>{friend.email}</p>
              </div>
              <span className="friends-date">
                Friends since {formatDate(friend.friendsSince)}
              </span>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}
