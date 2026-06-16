import type { TfcContentResponse } from "../api/tfcApi";

type TFCDetailHeaderProps = {
  tfc: TfcContentResponse;
};

export function TFCDetailHeader({ tfc }: TFCDetailHeaderProps) {
  return (
    <header className="tfc-detail-header">
      <h1 className="tfc-detail-title">{tfc.topic}</h1>
    </header>
  );
}
