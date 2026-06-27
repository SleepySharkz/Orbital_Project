import type { TcContentResponse } from "../api/tcApi";

type TCDetailHeaderProps = {
  tc: TcContentResponse;
};

export function TCDetailHeader({ tc }: TCDetailHeaderProps) {
  return (
    <header className="tc-detail-header">
      <h1 className="tc-detail-title">{tc.topic}</h1>

      {tc.isStale && (
        <p className="tc-stale-warning">
          This topic no longer exists in the module topic list. This cheatsheet is kept for
          historical review and will not receive new entries.
        </p>
      )}
    </header>
  );
}
