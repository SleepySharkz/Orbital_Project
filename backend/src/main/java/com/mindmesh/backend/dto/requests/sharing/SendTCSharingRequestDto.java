package com.mindmesh.backend.dto.requests.sharing;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SendTCSharingRequestDto {

    @NotEmpty(message = "At least one TC must be selected.")
    private List<@NotNull @Positive Long> tcIds;

    public SendTCSharingRequestDto() {
    }

    public List<Long> getTcIds() {
        return tcIds;
    }

    public void setTcIds(List<Long> tcIds) {
        this.tcIds = tcIds;
    }
}