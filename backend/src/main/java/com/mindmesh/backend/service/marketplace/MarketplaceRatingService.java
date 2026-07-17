package com.mindmesh.backend.service.marketplace;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.marketplace.MarketplaceUpvoteResponseDto;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.UserRepository;

@Service
public class MarketplaceRatingService {

  private final MarketplaceListingRepository marketplaceListingRepository;
  private final UserRepository userRepository;

  public MarketplaceRatingService(
      MarketplaceListingRepository marketplaceListingRepository,
      UserRepository userRepository) {
    this.marketplaceListingRepository = marketplaceListingRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public MarketplaceUpvoteResponseDto addUpvote(Long listingId, Long userId) {
    MarketplaceListing listing = getPublishedListing(listingId);
    User user = getUser(userId);
    listing.addUpvote(user);
    marketplaceListingRepository.save(listing);

    return toResponse(listing, user);
  }

  @Transactional
  public MarketplaceUpvoteResponseDto removeUpvote(Long listingId, Long userId) {
    MarketplaceListing listing = getPublishedListing(listingId);
    User user = getUser(userId);

    listing.removeUpvote(user);
    marketplaceListingRepository.save(listing);

    return toResponse(listing, user);
  }

  private MarketplaceListing getPublishedListing(Long listingId) {
    return marketplaceListingRepository
        .findByIdAndStatus(listingId, MarketplaceListingStatus.PUBLISHED)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Marketplace listing not found."));
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
  }

  private MarketplaceUpvoteResponseDto toResponse(MarketplaceListing listing, User user) {
    return new MarketplaceUpvoteResponseDto(
        listing.getId(),
        listing.getUpvoteCount(),
        listing.hasUpvoteFrom(user));
  }
}
