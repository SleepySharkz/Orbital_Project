package com.mindmesh.backend.dto.responses.sharing;

import java.time.Instant;

public class SharedTFCSummaryDto {

    private final Long id;
    private final Long moduleId;
    private final String courseCode;
    private final String schoolSem;
    private final String topic;
    private final int entryCount;
    private final Long sharedByUserId;
    private final String sharedByUsername;
    private final Instant acceptedAt;

    public SharedTFCSummaryDto(
        Long id,
        Long moduleId,
        String courseCode,
        String schoolSem,
        String topic,
        int entryCount,
        Long sharedByUserId,
        String sharedByUsername,
        Instant acceptedAt
    ) {
        this.id = id;
        this.moduleId = moduleId;
        this.courseCode = courseCode;
        this.schoolSem = schoolSem;
        this.topic = topic;
        this.entryCount = entryCount;
        this.sharedByUserId = sharedByUserId;
        this.sharedByUsername = sharedByUsername;
        this.acceptedAt = acceptedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getModuleId() {
        return moduleId;
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

    public int getEntryCount() {
        return entryCount;
    }

    public Long getSharedByUserId() {
        return sharedByUserId;
    }

    public String getSharedByUsername() {
        return sharedByUsername;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }
}