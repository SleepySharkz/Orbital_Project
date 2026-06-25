package com.mindmesh.backend.dto.requests.sharing;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SendTFCSharingRequestDto {

    @NotEmpty(message = "At least one TFC must be selected.")
    private List<@NotNull @Positive Long> tfcIds;

    public SendTFCSharingRequestDto() {
    }

    public List<Long> getTfcIds() {
        return tfcIds;
    }

    public void setTfcIds(List<Long> tfcIds) {
        this.tfcIds = tfcIds;
    }
}