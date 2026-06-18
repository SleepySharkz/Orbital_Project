import type { TfcContentResponse } from "../api/tfcApi";

type TFCDetailHeaderProps = {
  tfc: TfcContentResponse;
};

export function TFCDetailHeader({ tfc }: TFCDetailHeaderProps) {
  return (
    <header className="tfc-detail-header">
      <h1 className="tfc-detail-title">{tfc.topic}</h1>

      {tfc.isStale && (
        <p className="tfc-stale-warning">
          This topic no longer exists in the module topic list. This cheatsheet is kept for
          historical review and will not receive new entries.
        </p>
      )}
    </header>
  );
}
