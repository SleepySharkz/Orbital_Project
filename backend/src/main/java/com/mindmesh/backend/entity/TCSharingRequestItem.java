package com.mindmesh.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "tc_sharing_request_items",
    indexes = {
        @Index(
            name = "idx_tc_sharing_request_items_request_id",
            columnList = "sharing_request_id"
        )
    }
)
public class TCSharingRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sharing_request_id", nullable = false)
    private TCSharingRequest sharingRequest;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "source_tc_id", nullable = false)
    private Long sourceTcId;

    @Column(name = "source_module_id", nullable = false)
    private Long sourceModuleId;

    @Column(name = "source_owner_username", nullable = false)
    private String sourceOwnerUsername;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(name = "school_sem", nullable = false)
    private String schoolSem;

    @Column(nullable = false)
    private String topic;

    @Column(name = "source_was_stale_at_send_time", nullable = false)
    private Boolean sourceWasStaleAtSendTime;

    @Column(name = "source_updated_at")
    private LocalDateTime sourceUpdatedAt;

    @OneToMany(mappedBy = "sharingRequestItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TCSharingRequestEntrySnapshot> entrySnapshots = new ArrayList<>();

    protected TCSharingRequestItem() {
        // Required by JPA.
    }

    public TCSharingRequestItem(
        TCSharingRequest sharingRequest,
        Integer displayOrder,
        Long sourceTcId,
        Long sourceModuleId,
        String sourceOwnerUsername,
        String courseCode,
        String schoolSem,
        String topic,
        Boolean sourceWasStaleAtSendTime,
        LocalDateTime sourceUpdatedAt
    ) {
        if (sharingRequest == null) {
            throw new IllegalArgumentException("Sharing request is required.");
        }

        if (displayOrder == null || displayOrder < 0) {
            throw new IllegalArgumentException("Display order must be non-negative.");
        }

        if (sourceTcId == null || sourceModuleId == null) {
            throw new IllegalArgumentException("Source TC and module IDs are required.");
        }

        if (isBlank(sourceOwnerUsername) || isBlank(courseCode) || isBlank(schoolSem) || isBlank(topic)) {
            throw new IllegalArgumentException("Source metadata is required.");
        }

        if (sourceWasStaleAtSendTime == null) {
            throw new IllegalArgumentException("Source stale status is required.");
        }

        this.sharingRequest = sharingRequest;
        this.displayOrder = displayOrder;
        this.sourceTcId = sourceTcId;
        this.sourceModuleId = sourceModuleId;
        this.sourceOwnerUsername = sourceOwnerUsername;
        this.courseCode = courseCode;
        this.schoolSem = schoolSem;
        this.topic = topic;
        this.sourceWasStaleAtSendTime = sourceWasStaleAtSendTime;
        this.sourceUpdatedAt = sourceUpdatedAt;

        sharingRequest.addItem(this);
    }

    public void addEntrySnapshot(TCSharingRequestEntrySnapshot snapshot) {
        if (snapshot == null || entrySnapshots.contains(snapshot)) {
            return;
        }

        if (snapshot.getSharingRequestItem() != null && snapshot.getSharingRequestItem() != this) {
            snapshot.getSharingRequestItem().removeEntrySnapshot(snapshot);
        }

        entrySnapshots.add(snapshot);
        snapshot.setSharingRequestItem(this);
    }

    public void removeEntrySnapshot(TCSharingRequestEntrySnapshot snapshot) {
        if (snapshot == null) {
            return;
        }

        if (entrySnapshots.remove(snapshot) && snapshot.getSharingRequestItem() == this) {
            snapshot.setSharingRequestItem(null);
        }
    }

    void setSharingRequest(TCSharingRequest sharingRequest) {
        this.sharingRequest = sharingRequest;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() {
        return id;
    }

    public TCSharingRequest getSharingRequest() {
        return sharingRequest;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Long getSourceTcId() {
        return sourceTcId;
    }

    public Long getSourceModuleId() {
        return sourceModuleId;
    }

    public String getSourceOwnerUsername() {
        return sourceOwnerUsername;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getSchoolSem() {
        return schoolSem;
    }

    public String getTopic() {
        return topic;
    }

    public Boolean getSourceWasStaleAtSendTime() {
        return sourceWasStaleAtSendTime;
    }

    public LocalDateTime getSourceUpdatedAt() {
        return sourceUpdatedAt;
    }

    public List<TCSharingRequestEntrySnapshot> getEntrySnapshots() {
        return entrySnapshots;
    }
}
