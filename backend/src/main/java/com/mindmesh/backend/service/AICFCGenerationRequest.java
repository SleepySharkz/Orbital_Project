package com.mindmesh.backend.service;

import java.util.List;

public class AICFCGenerationRequest {
    private String courseCode;
    private String schoolSem;
    private String sourceType;
    private String sourceTitle;
    private List<AICFCGenerationRequestItem> items;

    public AICFCGenerationRequest(
        String courseCode,
        String schoolSem,
        String sourceType,
        String sourceTitle,
        List<AICFCGenerationRequestItem> items) {
            this.courseCode = courseCode;
            this.schoolSem = schoolSem;
            this.sourceType = sourceType;
            this.sourceTitle = sourceTitle;
            this.items = items;
        }

    public String getCourseCode() {
        return this.courseCode;
    }

    public String getSchoolSem() {
        return this.schoolSem;
    }

    public String getSourceType() {
        return this.sourceType;
    }

    public String getSourceTitle() {
        return this.sourceTitle;
    }

    public List<AICFCGenerationRequestItem> getAICFCGenerationRequestItem() {
        return this.items;
    }

    public List<AICFCGenerationRequestItem> getItems() {
        return this.items;
    }
}
