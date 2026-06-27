package com.mindmesh.backend.dto.requests.friends;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SendFriendRequestDto {
    
    @NotNull(message = "Recipient user ID is required.")
    @Positive(message = "Recipient user ID must be positive.")
    private Long recipientUserId;

    public SendFriendRequestDto() {
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }
}
