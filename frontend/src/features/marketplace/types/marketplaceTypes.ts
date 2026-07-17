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
  hasCurrentUserUpvoted: boolean;
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

export type MarketplaceListingStatus =
  | "PUBLISHED"
  | "UNLISTED"
  | "UNDER_REVIEW"
  | "REMOVED";

export type MarketplaceListingManagementSummary = {
  id: number;
  publicTitle: string;
  courseCode: string;
  schoolSem: string;
  topic: string;
  status: MarketplaceListingStatus;
  entryCount: number;
  upvoteCount: number;
  importCount: number;
  publishedAt: string;
  updatedAt: string;
  unlistedAt: string | null;
};

export type MarketplaceListingManagementPage = {
  items: MarketplaceListingManagementSummary[];
  page: number;
  size: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
};

export type MarketplaceListingManagementDetail =
  MarketplaceListingManagementSummary & {
    description: string | null;
    tags: string[];
    institution: string | null;
    publisherVisibility: PublisherVisibility;
    sourceTcId: number;
    sourceTcStillExists: boolean;
    sourceTcIsStale: boolean;
    entries: MarketplaceListingEntry[];
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
  hasCurrentUserUpvoted: boolean;
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

export type PublishMarketplaceListingRequest = {
  tcId: number;
  publicTitle: string;
  description?: string;
  tags?: string[];
  institution?: string;
  publisherVisibility: PublisherVisibility;
};

export type MarketplaceListingPublishResponse = {
  listingId: number;
  status: MarketplaceListingStatus;
  publicTitle: string;
  entryCount: number;
  publishedAt: string;
};

export type UpdateMarketplaceListingMetadataRequest = {
  publicTitle: string;
  description?: string;
  tags?: string[];
  institution?: string;
  publisherVisibility: PublisherVisibility;
};

export type MarketplaceUpvoteResponse = {
  listingId: number;
  upvoteCount: number;
  hasCurrentUserUpvoted: boolean;
};
