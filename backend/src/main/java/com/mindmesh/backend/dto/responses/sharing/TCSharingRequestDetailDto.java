package com.mindmesh.backend.dto.responses.sharing;

import java.time.Instant;
import java.util.List;

import com.mindmesh.backend.enums.TCSharingRequestStatus;

public class TCSharingRequestDetailDto {

    private final Long id;
    private final Long senderUserId;
    private final String senderUsername;
    private final String senderEmail;
    private final Long recipientUserId;
    private final String recipientUsername;
    private final String recipientEmail;
    private final TCSharingRequestStatus status;
    private final Instant createdAt;
    private final Instant respondedAt;
    private final List<TCSharingRequestItemDto> items;
    private final Boolean canAccept;
    private final List<String> blockingReasons;

    public TCSharingRequestDetailDto(
        Long id,
        Long senderUserId,
        String senderUsername,
        String senderEmail,
        Long recipientUserId,
        String recipientUsername,
        String recipientEmail,
        TCSharingRequestStatus status,
        Instant createdAt,
        Instant respondedAt,
        List<TCSharingRequestItemDto> items,
        Boolean canAccept,
        List<String> blockingReasons
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
        this.items = items;
        this.canAccept = canAccept;
        this.blockingReasons = blockingReasons;
    }

    public Long getId() {
        return this.id;
    }

    public Long getSenderUserId() {
        return this.senderUserId;
    }

    public String getSenderUsername() {
        return this.senderUsername;
    }

    public String getSenderEmail() {
        return this.senderEmail;
    }

    public Long getRecipientUserId() {
        return this.recipientUserId;
    }

    public String getRecipientUsername() {
        return this.recipientUsername;
    }

    public String getRecipientEmail() {
        return this.recipientEmail;
    }

    public TCSharingRequestStatus getStatus() {
        return this.status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getRespondedAt() {
        return this.respondedAt;
    }

    public List<TCSharingRequestItemDto> getItems() {
        return this.items;
    }
    
    public Boolean getCanAccept() {
        return this.canAccept;
    }

    public List<String> getBlockingReasons() {
        return this.blockingReasons;
    }
}
