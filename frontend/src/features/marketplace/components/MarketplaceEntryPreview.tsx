import type { MarketplaceListingEntry } from "../types/marketplaceTypes";

type MarketplaceEntryPreviewProps = {
  entry: MarketplaceListingEntry;
};

export function MarketplaceEntryPreview({ entry }: MarketplaceEntryPreviewProps) {
  return (
    <article className="marketplace-entry-preview">
      <div className="marketplace-entry-index">{entry.displayOrder + 1}</div>
      <div>
        <h3>{entry.flashcardQuestion}</h3>
        <p>{entry.flashcardNoteContent}</p>
      </div>
    </article>
  );
}
