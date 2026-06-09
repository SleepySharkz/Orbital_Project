package com.mindmesh.backend.dto.requests.cfc;

import jakarta.validation.constraints.NotBlank;

public class UpdateCFCSummaryRequestDto {
    @NotBlank(message = "Summary is Required")
    private String summary;

    public UpdateCFCSummaryRequestDto() {}

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
