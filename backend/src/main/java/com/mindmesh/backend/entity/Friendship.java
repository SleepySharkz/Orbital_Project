package com.mindmesh.backend.entity;

import java.time.Instant;
import java.util.Objects;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "friendships",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_friendships_user_pair",
            columnNames = {"user_a_id", "user_b_id"}
        )
    },
    indexes = {
        @Index(
            name = "idx_friendships_user_a",
            columnList = "user_a_id"
        ),
        @Index(
            name = "idx_friendships_user_b",
            columnList = "user_b_id"
        )
    }
)
public class Friendship {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_a_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_b_id", nullable = false)
    private User userB;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Friendship() {
        // Required by JPA.
    }

    public Friendship(User firstUser, User secondUser) {
        if (firstUser == null || secondUser == null) {
            throw new IllegalArgumentException(
                "Both users are required."
            );
        }

        if (firstUser.getId() == null || secondUser.getId() == null) {
            throw new IllegalArgumentException(
                "Both users must be persisted before creating a friendship."
            );
        }

        if (Objects.equals(firstUser.getId(), secondUser.getId())) {
            throw new IllegalArgumentException(
                "A user cannot be friends with themselves."
            );
        }

        if (firstUser.getId() < secondUser.getId()) {
            this.userA = firstUser;
            this.userB = secondUser;
        } else {
            this.userA = secondUser;
            this.userB = firstUser;
        }
    }

    public User getOtherUser(Long currentUserId) {
        if (Objects.equals(userA.getId(), currentUserId)) {
            return userB;
        }

        if (Objects.equals(userB.getId(), currentUserId)) {
            return userA;
        }

        throw new IllegalArgumentException(
            "The supplied user does not belong to this friendship."
        );
    }

    public Long getId() {
        return id;
    }

    public User getUserA() {
        return userA;
    }

    public User getUserB() {
        return userB;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
