package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mindmesh.backend.entity.MarketplaceImport;

public interface MarketplaceImportRepository extends JpaRepository<MarketplaceImport, Long> {

  boolean existsByImporterIdAndSourceListingId(Long importerId, Long sourceListingId);

  @EntityGraph(attributePaths = { "sourceListing", "entries" })
  List<MarketplaceImport> findByImporterIdOrderByImportedAtDesc(Long importerId);

  @EntityGraph(attributePaths = { "sourceListing", "entries" })
  Optional<MarketplaceImport> findByIdAndImporterId(Long id, Long importerId);
}
