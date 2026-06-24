package com.mindmesh.backend.entity;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.mindmesh.backend.enums.FriendRequestStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "friend_requests",
    indexes = {
        @Index(
            name = "idx_friend_requests_recipient_status",
            columnList = "recipient_id,status"
        ),
        @Index(
            name = "idx_friend_requests_sender_status",
            columnList = "sender_id,status"
        )
    }
)
public class FriendRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Store the enum name, not its numeric position, so adding enum values later
    // does not reinterpret existing database rows.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendRequestStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at") //can stay null until response is made
    private Instant respondedAt;

    protected FriendRequest() {
        // Required by JPA.
    }

    public FriendRequest(User sender, User recipient) {
        if (sender == null || recipient == null) {
        throw new IllegalArgumentException("Sender and recipient are required.");
        }

        this.sender = sender;
        this.recipient = recipient;
        this.status = FriendRequestStatus.PENDING;
    }

    public void accept(Instant responseTime) {
        completeWith(FriendRequestStatus.ACCEPTED, responseTime);
    }

    public void decline(Instant responseTime) {
        completeWith(FriendRequestStatus.DECLINED, responseTime);
    }

    public void cancel(Instant responseTime) {
        completeWith(FriendRequestStatus.CANCELLED, responseTime);
    }

    private void completeWith(
        FriendRequestStatus completedStatus,
        Instant responseTime) {
        if (status != FriendRequestStatus.PENDING) {
        throw new IllegalStateException("Only pending friend requests can be resolved.");
        }

        if (responseTime == null) {
        throw new IllegalArgumentException("Response time is required.");
        }

        this.status = completedStatus;
        this.respondedAt = responseTime;
    }

    public Long getId() {
        return id;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
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

