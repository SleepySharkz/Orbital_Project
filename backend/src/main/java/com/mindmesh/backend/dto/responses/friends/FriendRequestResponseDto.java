package com.mindmesh.backend.dto.responses.friends;

import java.time.Instant;

import com.mindmesh.backend.enums.FriendRequestStatus;

public class FriendRequestResponseDto {

    private final Long id;
    private final Long senderUserId;
    private final String senderUsername;
    private final String senderEmail;
    private final Long recipientUserId;
    private final String recipientUsername;
    private final String recipientEmail;
    private final FriendRequestStatus status;
    private final Instant createdAt;
    private final Instant respondedAt;

    public FriendRequestResponseDto(
        Long id,
        Long senderUserId,
        String senderUsername,
        String senderEmail,
        Long recipientUserId,
        String recipientUsername,
        String recipientEmail,
        FriendRequestStatus status,
        Instant createdAt,
        Instant respondedAt
    ) {
        this.id = id;
        this.senderUserId = senderUserId;
        this.senderUsername = senderUsername;
        this.senderEmail = senderEmail;
        this.recipientUserId = recipientUserId;
        this.recipientUsername = recipientUsername;
        this.recipientEmail = recipientEmail;
        this.status = status;
        this.createdAt = createdAt;
        this.respondedAt = respondedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getSenderUserId() {
        return senderUserId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public FriendRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }
}
