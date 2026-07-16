package com.mindmesh.backend.enums;

public enum MarketplaceListingStatus {
  PUBLISHED,

  // Hidden from the public, but still in the marketplace
  // Existing imports remain visible though
  UNLISTED,

  // Future work -> To review flagged published content by admins
  UNDER_REVIEW,
  REMOVED
}
