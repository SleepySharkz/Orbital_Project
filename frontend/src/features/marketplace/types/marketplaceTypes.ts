export type PublisherVisibility = "DISPLAY_NAME" | "ANONYMOUS";

export type MarketplaceListingSummary = {
  id: number;
  publicTitle: string;
  descriptionPreview: string | null;
  courseCode: string;
  schoolSem: string;
  topic: string;
  tags: string[];
  publisherVisibility: PublisherVisibility;
  publisherDisplayName: string;
  entryCount: number;
  upvoteCount: number;
  importCount: number;
  publishedAt: string;
};

export type MarketplaceListingPage = {
  items: MarketplaceListingSummary[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
};

export type MarketplaceListingEntry = {
  id: number;
  flashcardQuestion: string;
  flashcardNoteContent: string;
  displayOrder: number;
};

export type MarketplaceListingDetail = {
  id: number;
  publicTitle: string;
  description: string | null;
  courseCode: string;
  schoolSem: string;
  topic: string;
  tags: string[];
  institution: string | null;
  publisherVisibility: PublisherVisibility;
  publisherDisplayName: string;
  entryCount: number;
  upvoteCount: number;
  importCount: number;
  publishedAt: string;
  updatedAt: string;
  entries: MarketplaceListingEntry[];
};

export type MarketplaceBrowseParams = {
  q?: string;
  module?: string;
  topic?: string;
  tag?: string;
  sort?: MarketplaceSort;
  page?: number;
  size?: number;
};

export type MarketplaceSort = "newest" | "mostUpvoted" | "mostImported";
