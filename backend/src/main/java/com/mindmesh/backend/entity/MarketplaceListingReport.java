package com.mindmesh.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.mindmesh.backend.enums.MarketplaceReportReason;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "marketplace_listing_reports", uniqueConstraints = {
    @UniqueConstraint(name = "uk_marketplace_listing_reports_listing_reporter", columnNames = { "listing_id",
        "reporter_id" })
})
public class MarketplaceListingReport {

  private static final int MAX_DETAILS_LENGTH = 1000;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "listing_id", nullable = false)
  private MarketplaceListing listing;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "reporter_id", nullable = false)
  private User reporter;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private MarketplaceReportReason reason;

  @Column(columnDefinition = "TEXT")
  private String details;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected MarketplaceListingReport() {
  }

  public MarketplaceListingReport(
      MarketplaceListing listing,
      User reporter,
      MarketplaceReportReason reason,
      String details) {
    if (listing == null || reporter == null || reason == null) {
      throw new IllegalArgumentException("Listing, reporter, and report reason are required.");
    }

    String normalizedDetails = normalizeDetails(details);
    if (normalizedDetails != null && normalizedDetails.length() > MAX_DETAILS_LENGTH) {
      throw new IllegalArgumentException("Report details must not exceed 1000 characters.");
    }

    this.listing = listing;
    this.reporter = reporter;
    this.reason = reason;
    this.details = normalizedDetails;
  }

  private String normalizeDetails(String value) {
    if (value == null) {
      return null;
    }

    String normalizedValue = value.trim();
    return normalizedValue.isEmpty() ? null : normalizedValue;
  }

  public Long getId() {
    return id;
  }

  public MarketplaceListing getListing() {
    return listing;
  }

  public User getReporter() {
    return reporter;
  }

  public MarketplaceReportReason getReason() {
    return reason;
  }

  public String getDetails() {
    return details;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
