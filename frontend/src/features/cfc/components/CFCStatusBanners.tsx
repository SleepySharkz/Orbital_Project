type CFCStatusBannersProps = {
  loadError: string;
  submissionError: string;
  submissionSuccess: string;
};

export function CFCStatusBanners({
  loadError,
  submissionError,
  submissionSuccess,
}: CFCStatusBannersProps) {
  return (
    <>
      {loadError ? <p className="cfc-banner cfc-banner-error">{loadError}</p> : null}
      {submissionError ? <p className="cfc-banner cfc-banner-error">{submissionError}</p> : null}
      {submissionSuccess ? <p className="cfc-banner cfc-banner-success">{submissionSuccess}</p> : null}
    </>
  );
}
