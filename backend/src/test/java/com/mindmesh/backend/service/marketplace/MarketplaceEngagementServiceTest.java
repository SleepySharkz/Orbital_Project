package com.mindmesh.backend.service.marketplace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.marketplace.MarketplaceUpvoteResponseDto;
import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.enums.PublisherVisibility;
import com.mindmesh.backend.repository.MarketplaceListingReportRepository;
import com.mindmesh.backend.repository.MarketplaceListingRepository;
import com.mindmesh.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MarketplaceEngagementServiceTest {

  @Mock
  private MarketplaceListingRepository marketplaceListingRepository;

  @Mock
  private MarketplaceListingReportRepository marketplaceListingReportRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private MarketplaceEngagementService marketplaceEngagementService;

  private User publisher;
  private User voter;
  private MarketplaceListing listing;

  @BeforeEach
  void setUp() {
    publisher = new User("Publisher", "publisher@example.com", "hashed");
    ReflectionTestUtils.setField(publisher, "id", 1L);

    voter = new User("Voter", "voter@example.com", "hashed");
    ReflectionTestUtils.setField(voter, "id", 2L);

    listing = new MarketplaceListing(
        publisher,
        10L,
        20L,
        "Trees Guide",
        "Public notes",
        "CS2040",
        "Year 1 Sem 2",
        "Trees",
        "trees",
        "NUS",
        PublisherVisibility.DISPLAY_NAME,
        "Publisher");
    ReflectionTestUtils.setField(listing, "id", 50L);
  }

  @Test
  void addAndRemoveUpvote_areIdempotentAndUpdateCount() {
    when(marketplaceListingRepository.findByIdAndStatus(50L, MarketplaceListingStatus.PUBLISHED))
        .thenReturn(Optional.of(listing));
    when(userRepository.findById(2L)).thenReturn(Optional.of(voter));

    MarketplaceUpvoteResponseDto added = marketplaceEngagementService.addUpvote(50L, 2L);
    MarketplaceUpvoteResponseDto duplicate = marketplaceEngagementService.addUpvote(50L, 2L);
    MarketplaceUpvoteResponseDto removed = marketplaceEngagementService.removeUpvote(50L, 2L);
    MarketplaceUpvoteResponseDto repeatedRemoval = marketplaceEngagementService.removeUpvote(50L, 2L);

    assertEquals(1, added.getUpvoteCount());
    assertTrue(added.isHasCurrentUserUpvoted());
    assertEquals(1, duplicate.getUpvoteCount());
    assertEquals(0, removed.getUpvoteCount());
    assertFalse(removed.isHasCurrentUserUpvoted());
    assertEquals(0, repeatedRemoval.getUpvoteCount());
  }

  @Test
  void addUpvote_allowsPublisherButRejectsNonPublishedListings() {
    when(marketplaceListingRepository.findByIdAndStatus(50L, MarketplaceListingStatus.PUBLISHED))
        .thenReturn(Optional.of(listing));
    when(userRepository.findById(1L)).thenReturn(Optional.of(publisher));

    MarketplaceUpvoteResponseDto publisherUpvote = marketplaceEngagementService.addUpvote(50L, 1L);

    assertEquals(1, publisherUpvote.getUpvoteCount());
    assertTrue(publisherUpvote.isHasCurrentUserUpvoted());
    verify(marketplaceListingRepository).save(listing);

    when(marketplaceListingRepository.findByIdAndStatus(60L, MarketplaceListingStatus.PUBLISHED))
        .thenReturn(Optional.empty());
    ResponseStatusException nonPublished = assertThrows(
        ResponseStatusException.class,
        () -> marketplaceEngagementService.addUpvote(60L, 2L));
    assertEquals(404, nonPublished.getStatusCode().value());
  }
}
