package com.mindmesh.backend.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.MarketplaceListing;
import com.mindmesh.backend.enums.MarketplaceListingStatus;

public interface MarketplaceListingRepository extends JpaRepository<MarketplaceListing, Long> {

  // Performance optimisation using attributePaths. We also eagerly query other
  // attributes together as one read, to prevent second reads.
  // Likely need to load children entries
  @EntityGraph(attributePaths = { "publisher", "entries" })
  Optional<MarketplaceListing> findByIdAndPublisherId(Long id, Long publisherId);

  @EntityGraph(attributePaths = { "publisher", "entries" })
  Optional<MarketplaceListing> findByIdAndStatus(Long id, MarketplaceListingStatus status);

  // No need entries since these are used for surface level browsing
  @EntityGraph(attributePaths = { "publisher" })
  Page<MarketplaceListing> findByPublisherIdOrderByPublishedAtDesc(Long publisherId, Pageable pageable);

  @EntityGraph(attributePaths = { "publisher" })
  Page<MarketplaceListing> findByPublisherIdOrderByUpdatedAtDesc(Long publisherId, Pageable pageable);

  @EntityGraph(attributePaths = { "publisher" })
  Page<MarketplaceListing> findByStatusOrderByPublishedAtDesc(MarketplaceListingStatus status, Pageable pageable);

  @EntityGraph(attributePaths = { "publisher" })
  Page<MarketplaceListing> findByStatusOrderByImportCountDesc(MarketplaceListingStatus status, Pageable pageable);

  @EntityGraph(attributePaths = { "publisher" })
  Page<MarketplaceListing> findByStatusOrderByUpvoteCountDesc(MarketplaceListingStatus status, Pageable pageable);

  @EntityGraph(attributePaths = { "publisher" })
  Page<MarketplaceListing> findByStatusOrderByEntryCountDesc(MarketplaceListingStatus status, Pageable pageable);

  // This is our search engine utilizing the database directly through advanced
  // query methods
  @EntityGraph(attributePaths = { "publisher" })
  @Query("""
      SELECT listing
      FROM MarketplaceListing listing
      WHERE listing.status = :status
        AND (
          :q IS NULL
          OR LOWER(listing.publicTitle) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(COALESCE(listing.description, '')) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(listing.courseCode) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(listing.topic) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(COALESCE(listing.tags, '')) LIKE LOWER(CONCAT('%', :q, '%'))
        )
        AND (
          :module IS NULL
          OR LOWER(listing.courseCode) = LOWER(:module)
        )
        AND (
          :topic IS NULL
          OR LOWER(listing.topic) LIKE LOWER(CONCAT('%', :topic, '%'))
        )
        AND (
          :tag IS NULL
          OR LOWER(COALESCE(listing.tags, '')) LIKE LOWER(CONCAT('%', :tag, '%'))
        )
      """)
  Page<MarketplaceListing> searchPublishedListings(
      @Param("status") MarketplaceListingStatus status,
      @Param("q") String q,
      @Param("module") String module,
      @Param("topic") String topic,
      @Param("tag") String tag,
      Pageable pageable);

  boolean existsByPublisherIdAndSourceTcIdAndStatus(
      Long publisherId,
      Long sourceTcId,
      MarketplaceListingStatus status);

  boolean existsByPublisherIdAndSourceTcIdAndStatusIn(
      Long publisherId,
      Long sourceTcId,
      Iterable<MarketplaceListingStatus> statuses);

  // Publisher can still view his/her listing even though if its hidden from
  // public
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
