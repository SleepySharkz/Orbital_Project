package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.enums.MarketplaceListingStatus;

public interface MarketplaceListingRepository extends JpaRepository<MarketplaceListing, Long> {

  // Just pure find queries (Pagination to be done later IMPORTANT)
  @EntityGraph(attributePaths = { "publisher", "entries" })
  Optional<MarketplaceListing> findByIdAndPublisherId(Long id, Long publisherId);

  @EntityGraph(attributePaths = { "publisher", "entries" })
  Optional<MarketplaceListing> findByIdAndStatus(Long id, MarketplaceListingStatus status);

  @EntityGraph(attributePaths = { "publisher" })
  List<MarketplaceListing> findByPublisherIdOrderByPublishedAtDesc(Long publisherId);

  @EntityGraph(attributePaths = { "publisher" })
  List<MarketplaceListing> findByStatusOrderByPublishedAtDesc(MarketplaceListingStatus status);

  boolean existsByPublisherIdAndSourceTcIdAndStatus(
      Long publisherId,
      Long sourceTcId,
      MarketplaceListingStatus status);

  @EntityGraph(attributePaths = { "publisher", "entries" })
  @Query("""
      SELECT listing
      FROM MarketplaceListing listing
      WHERE listing.id = :listingId
        AND (
          listing.status = :publicStatus
          OR listing.publisher.id = :publisherId
        )
      """)
  Optional<MarketplaceListing> findVisibleToPublisherOrPublic(
      @Param("listingId") Long listingId,
      @Param("publisherId") Long publisherId,
      @Param("publicStatus") MarketplaceListingStatus publicStatus);
}
