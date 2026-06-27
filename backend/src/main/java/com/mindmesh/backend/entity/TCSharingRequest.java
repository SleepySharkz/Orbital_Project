package com.mindmesh.backend.entity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.mindmesh.backend.enums.TCSharingRequestStatus;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "tc_sharing_requests",
    indexes = {
        @Index(
            name = "idx_tc_sharing_requests_recipient_status",
            columnList = "recipient_id,status"
        ),
        @Index(
            name = "idx_tc_sharing_requests_sender_status",
            columnList = "sender_id,status"
        ),
        @Index(
            name = "idx_tc_sharing_requests_sender_recipient_status",
            columnList = "sender_id,recipient_id,status"
        )
    }
)
public class TCSharingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TCSharingRequestStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "responded_at")
    private Instant respondedAt;

    @OneToMany(mappedBy = "sharingRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TCSharingRequestItem> items = new ArrayList<>();

    protected TCSharingRequest() {
        // JPA.
    }

    public TCSharingRequest(User sender, User recipient) {
        if (sender == null || recipient == null) {
            throw new IllegalArgumentException("Sender and recipient are required.");
        }

        this.sender = sender;
        this.recipient = recipient;
        this.status = TCSharingRequestStatus.PENDING;
    }

    public void addItem(TCSharingRequestItem item) {
        if (item == null || items.contains(item)) {
            return;
        }

        if (item.getSharingRequest() != null && item.getSharingRequest() != this) {
            item.getSharingRequest().removeItem(item);
        }

        items.add(item);
        item.setSharingRequest(this);
    }

    public void removeItem(TCSharingRequestItem item) {
        if (item == null) {
            return;
        }

        if (items.remove(item) && item.getSharingRequest() == this) {
            item.setSharingRequest(null);
        }
    }

    public void accept(Instant responseTime) {
        completeWith(TCSharingRequestStatus.ACCEPTED, responseTime);
    }

    public void decline(Instant responseTime) {
        completeWith(TCSharingRequestStatus.DECLINED, responseTime);
    }

    public void cancel(Instant responseTime) {
        completeWith(TCSharingRequestStatus.CANCELLED, responseTime);
    }

    //security so that only pending response can be accepted
    private void completeWith(
        TCSharingRequestStatus completedStatus,
        Instant responseTime) {
        if (status != TCSharingRequestStatus.PENDING) {
            throw new IllegalStateException("Only pending TC sharing requests can be resolved.");
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

    public TCSharingRequestStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public List<TCSharingRequestItem> getItems() {
        return items;
    }
}
