package com.mindmesh.backend.dto.responses.sharing;

import java.time.LocalDateTime;
import java.util.List;

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
        List<TFCSharingRequestEntrySnapshotDto> entries
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
    }

    public Long getId() {
        return id;
    }

    public Long getSourceTfcId() {
        return sourceTfcId;
    }

    public Long getSourceModuleId() {
        return sourceModuleId;
    }

    public String getSourceOwnerUsername() {
        return sourceOwnerUsername;
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

    public Boolean getSourceWasStaleAtSendTime() {
        return sourceWasStaleAtSendTime;
    }

    public LocalDateTime getSourceUpdatedAt() {
        return sourceUpdatedAt;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public List<TFCSharingRequestEntrySnapshotDto> getEntries() {
        return entries;
    }
}