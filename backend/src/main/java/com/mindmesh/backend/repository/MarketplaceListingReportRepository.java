package com.mindmesh.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindmesh.backend.entity.MarketplaceListingReport;

public interface MarketplaceListingReportRepository extends JpaRepository<MarketplaceListingReport, Long> {

  boolean existsByListingIdAndReporterId(Long listingId, Long reporterId);

  long countByListingId(Long listingId);
}
