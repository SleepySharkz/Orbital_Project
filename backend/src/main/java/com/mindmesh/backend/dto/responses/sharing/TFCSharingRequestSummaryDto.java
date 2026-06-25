package com.mindmesh.backend.dto.responses.sharing;

import java.time.Instant;
import java.util.List;

import com.mindmesh.backend.enums.TFCSharingRequestStatus;

public class TFCSharingRequestSummaryDto {

    private final Long id;
    private final Long senderUserId;
    private final String senderUsername;
    private final String senderEmail;
    private final Long recipientUserId;
    private final String recipientUsername;
    private final String recipientEmail;
    private final TFCSharingRequestStatus status;
    private final int itemCount;
    private final List<String> topics;
    private final Instant createdAt;

    public TFCSharingRequestSummaryDto(
        Long id,
        Long senderUserId,
        String senderUsername,
        String senderEmail,
        Long recipientUserId,
        String recipientUsername,
        String recipientEmail,
        TFCSharingRequestStatus status,
        int itemCount,
        List<String> topics,
        Instant createdAt
    ) {
        this.id = id;
        this.senderUserId = senderUserId;
        this.senderUsername = senderUsername;
        this.senderEmail = senderEmail;
        this.recipientUserId = recipientUserId;
        this.recipientUsername = recipientUsername;
        this.recipientEmail = recipientEmail;
        this.status = status;
        this.itemCount = itemCount;
        this.topics = topics;
        this.createdAt = createdAt;
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

    public TFCSharingRequestStatus getStatus() {
        return status;
    }

    public int getItemCount() {
        return itemCount;
    }

    public List<String> getTopics() {
        return topics;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}