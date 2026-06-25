package com.mindmesh.backend.entity;

import java.time.Instant;
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
    name = "shared_tfcs",
    indexes = {
        @Index(name = "idx_shared_tfcs_owner_accepted_at", columnList = "owner_id,accepted_at"),
        @Index(name = "idx_shared_tfcs_source_request", columnList = "source_sharing_request_id"),
        @Index(name = "idx_shared_tfcs_source_item", columnList = "source_sharing_request_item_id")
    }
)
public class SharedTFC {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "original_owner_id", nullable = false)
    private User originalOwner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_sharing_request_id", nullable = false)
    private TFCSharingRequest sourceSharingRequest;

    @Column(name = "source_sharing_request_item_id", nullable = false)
    private Long sourceSharingRequestItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(name = "course_code", nullable = false)
    private String courseCode;

    @Column(name = "school_sem", nullable = false)
    private String schoolSem;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "original_owner_username", nullable = false)
    private String originalOwnerUsername;

    @Column(name = "accepted_at", nullable = false)
    private Instant acceptedAt;

    @OneToMany(mappedBy= "sharedTfc", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SharedTFCEntry> entries = new ArrayList<>();

    protected SharedTFC() {
        //JPA stuff
    }

    public SharedTFC(
      User owner,
      User originalOwner,
      TFCSharingRequest sourceSharingRequest,
      Long sourceSharingRequestItemId,
      CourseModule module,
      String courseCode,
      String schoolSem,
      String topic,
      String originalOwnerUsername,
      Instant acceptedAt
    ) {
        if (owner == null || originalOwner == null || sourceSharingRequest == null || module == null) {
        throw new IllegalArgumentException("Shared TFC relationships are required.");
        }

        if (sourceSharingRequestItemId == null) {
            throw new IllegalArgumentException("Source sharing request item id is required.");
        }

        if (isBlank(courseCode) || isBlank(schoolSem) || isBlank(topic) || isBlank(originalOwnerUsername)) {
            throw new IllegalArgumentException("Shared TFC metadata is required.");
        }

        if (acceptedAt == null) {
            throw new IllegalArgumentException("Accepted timestamp is required.");
        }

        this.owner = owner;
        this.originalOwner = originalOwner;
        this.sourceSharingRequest = sourceSharingRequest;
        this.sourceSharingRequestItemId = sourceSharingRequestItemId;
        this.module = module;
        this.courseCode = courseCode;
        this.schoolSem = schoolSem;
        this.topic = topic;
        this.originalOwnerUsername = originalOwnerUsername;
        this.acceptedAt = acceptedAt;
    }

    public void addEntry(SharedTFCEntry entry) {
        if (entry == null || entries.contains(entry)) {
        return;
        }

        if (entry.getSharedTfc() != null && entry.getSharedTfc() != this) {
        entry.getSharedTfc().removeEntry(entry);
        }

        entries.add(entry);
        entry.setSharedTfc(this);
    }

    public void removeEntry(SharedTFCEntry entry) {
        if (entry == null) {
        return;
        }

        if (entries.remove(entry) && entry.getSharedTfc() == this) {
        entry.setSharedTfc(null);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public User getOriginalOwner() {
        return originalOwner;
    }

    public TFCSharingRequest getSourceSharingRequest() {
        return sourceSharingRequest;
    }

    public Long getSourceSharingRequestItemId() {
        return sourceSharingRequestItemId;
    }

    public CourseModule getModule() {
        return module;
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

    public String getOriginalOwnerUsername() {
        return originalOwnerUsername;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public List<SharedTFCEntry> getEntries() {
        return entries;
    }
}
