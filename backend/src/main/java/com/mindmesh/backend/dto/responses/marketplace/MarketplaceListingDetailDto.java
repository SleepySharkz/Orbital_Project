package com.mindmesh.backend.dto.responses.marketplace;

import java.time.Instant;
import java.util.List;

import com.mindmesh.backend.enums.MarketplaceListingStatus;
import com.mindmesh.backend.enums.PublisherVisibility;

public class MarketplaceListingDetailDto {

    private final Long id;
    private final Long sourceTcId;
    private final Long sourceModuleId;
    private final String publicTitle;
    private final String description;
    private final String courseCode;
    private final String schoolSem;
    private final String topic;
    private final List<String> tags;
    private final String institution;
    private final String difficulty;
    private final PublisherVisibility publisherVisibility;
    private final String publisherDisplayName;
    private final MarketplaceListingStatus status;
    private final Integer entryCount;
    private final Integer upvoteCount;
    private final Integer importCount;
    private final Instant publishedAt;
    private final Instant updatedAt;
    private final Instant unlistedAt;
    private final Instant removedAt;
    private final List<MarketplaceListingEntrySnapshotDto> entries;

    public MarketplaceListingDetailDto(
        Long id,
        Long sourceTcId,
        Long sourceModuleId,
        String publicTitle,
        String description,
        String courseCode,
        String schoolSem,
        String topic,
        List<String> tags,
        String institution,
        String difficulty,
        PublisherVisibility publisherVisibility,
        String publisherDisplayName,
        MarketplaceListingStatus status,
        Integer entryCount,
        Integer upvoteCount,
        Integer importCount,
        Instant publishedAt,
        Instant updatedAt,
        Instant unlistedAt,
        Instant removedAt,
        List<MarketplaceListingEntrySnapshotDto> entries
    ) {
        this.id = id;
        this.sourceTcId = sourceTcId;
        this.sourceModuleId = sourceModuleId;
        this.publicTitle = publicTitle;
        this.description = description;
        this.courseCode = courseCode;
        this.schoolSem = schoolSem;
        this.topic = topic;
        this.tags = tags;
        this.institution = institution;
        this.difficulty = difficulty;
        this.publisherVisibility = publisherVisibility;
        this.publisherDisplayName = publisherDisplayName;
        this.status = status;
        this.entryCount = entryCount;
        this.upvoteCount = upvoteCount;
        this.importCount = importCount;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
        this.unlistedAt = unlistedAt;
        this.removedAt = removedAt;
        this.entries = entries;
    }

    public Long getId() {
        return id;
    }

    public Long getSourceTcId() {
        return sourceTcId;
    }

    public Long getSourceModuleId() {
        return sourceModuleId;
    }

    public String getPublicTitle() {
        return publicTitle;
    }

    public String getDescription() {
        return description;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getSchoolSem() {
        return schoolSem;
    }

    public String getTopic() {
        return topic;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getInstitution() {
        return institution;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public PublisherVisibility getPublisherVisibility() {
        return publisherVisibility;
    }

    public String getPublisherDisplayName() {
        return publisherDisplayName;
    }

    public MarketplaceListingStatus getStatus() {
        return status;
    }

    public Integer getEntryCount() {
        return entryCount;
    }

    public Integer getUpvoteCount() {
        return upvoteCount;
    }

    public Integer getImportCount() {
        return importCount;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getUnlistedAt() {
        return unlistedAt;
    }

    public Instant getRemovedAt() {
        return removedAt;
    }

    public List<MarketplaceListingEntrySnapshotDto> getEntries() {
        return entries;
    }
}
