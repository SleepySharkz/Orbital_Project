package com.mindmesh.backend.dto.responses.sharing;

import java.time.LocalDateTime;
import java.util.List;

import com.mindmesh.backend.enums.TFCSharingCompatibilityStatus;

public class TFCSharingRequestItemDto {

    private final Long id;
    private final Long sourceTfcId;
    private final Long sourceModuleId;
    private final String sourceOwnerUsername;
    private final String courseCode;
    private final String schoolSem;
    private final String topic;
    private final Boolean sourceWasStaleAtSendTime;
    private final LocalDateTime sourceUpdatedAt;
    private final int entryCount;
    private final List<TFCSharingRequestEntrySnapshotDto> entries;
    private final Long matchingRecipientModuleId;
    private final Boolean hasMatchingModule;
    private final Boolean hasMatchingTopic;
    private final TFCSharingCompatibilityStatus compatibilityStatus;
    private final String blockingReason;

    public TFCSharingRequestItemDto(
        Long id,
        Long sourceTfcId,
        Long sourceModuleId,
        String sourceOwnerUsername,
        String courseCode,
        String schoolSem,
        String topic,
        Boolean sourceWasStaleAtSendTime,
        LocalDateTime sourceUpdatedAt,
        int entryCount,
        List<TFCSharingRequestEntrySnapshotDto> entries,
        Long matchingRecipientModuleId,
        Boolean hasMatchingModule,
        Boolean hasMatchingTopic,
        TFCSharingCompatibilityStatus compatibilityStatus,
        String blockingReason

    ) {
        this.id = id;
        this.sourceTfcId = sourceTfcId;
        this.sourceModuleId = sourceModuleId;
        this.sourceOwnerUsername = sourceOwnerUsername;
        this.courseCode = courseCode;
        this.schoolSem = schoolSem;
        this.topic = topic;
        this.sourceWasStaleAtSendTime = sourceWasStaleAtSendTime;
        this.sourceUpdatedAt = sourceUpdatedAt;
        this.entryCount = entryCount;
        this.entries = entries;
        this.matchingRecipientModuleId = matchingRecipientModuleId;
        this.hasMatchingModule = hasMatchingModule;
        this.hasMatchingTopic = hasMatchingTopic;
        this.compatibilityStatus = compatibilityStatus;
        this.blockingReason = blockingReason;

    }

    public Long getId() {
        return this.id;
    }

    public Long getSourceTfcId() {
        return this.sourceTfcId;
    }

    public Long getSourceModuleId() {
        return this.sourceModuleId;
    }

    public String getSourceOwnerUsername() {
        return this.sourceOwnerUsername;
    }

    public String getCourseCode() {
        return this.courseCode;
    }

    public String getSchoolSem() {
        return this.schoolSem;
    }

    public String getTopic() {
        return this.topic;
    }

    public Boolean getSourceWasStaleAtSendTime() {
        return this.sourceWasStaleAtSendTime;
    }

    public LocalDateTime getSourceUpdatedAt() {
        return this.sourceUpdatedAt;
    }

    public int getEntryCount() {
        return this.entryCount;
    }

    public List<TFCSharingRequestEntrySnapshotDto> getEntries() {
        return this.entries;
    }

    public Long getMatchingRecipientModuleId() {
        return this.matchingRecipientModuleId;
    }

    public Boolean getHasMatchingModule() {
        return this.hasMatchingModule;
    }

    public Boolean getHasMatchingTopic() {
        return this.hasMatchingTopic;
    }

    public TFCSharingCompatibilityStatus getCompatibilityStatus() {
        return this.compatibilityStatus;
    }

    public String getBlockingReason() {
        return this.blockingReason;
    }
}