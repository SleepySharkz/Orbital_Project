package com.mindmesh.backend.dto.responses.sharing;

import java.time.Instant;
import java.util.List;

public class SharedTCDetailDto {

    private final Long id;
    private final Long moduleId;
    private final String courseCode;
    private final String schoolSem;
    private final String topic;
    private final Long sharedByUserId;
    private final String sharedByUsername;
    private final Instant acceptedAt;
    private final List<SharedTCEntryDto> entries;

    public SharedTCDetailDto(
        Long id,
        Long moduleId,
        String courseCode,
        String schoolSem,
        String topic,
        Long sharedByUserId,
        String sharedByUsername,
        Instant acceptedAt,
        List<SharedTCEntryDto> entries
    ) {
        this.id = id;
        this.moduleId = moduleId;
        this.courseCode = courseCode;
        this.schoolSem = schoolSem;
        this.topic = topic;
        this.sharedByUserId = sharedByUserId;
        this.sharedByUsername = sharedByUsername;
        this.acceptedAt = acceptedAt;
        this.entries = entries;
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

    public Long getSharedByUserId() {
        return sharedByUserId;
    }

    public String getSharedByUsername() {
        return sharedByUsername;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public List<SharedTCEntryDto> getEntries() {
        return entries;
    }
}
