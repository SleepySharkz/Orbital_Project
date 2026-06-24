package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.FriendRequest;
import com.mindmesh.backend.enums.FriendRequestStatus;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    
    @EntityGraph(attributePaths = {"sender", "recipient"})
    List<FriendRequest> findByRecipientIdAndStatusOrderByCreatedAtDesc(
        Long recipientId,
        FriendRequestStatus status
    );

    @EntityGraph(attributePaths = {"sender", "recipient"})
    List<FriendRequest> findBySenderIdAndStatusOrderByCreatedAtDesc(
        Long senderId,
        FriendRequestStatus status
    );

    @Query("""
        SELECT COUNT(friendRequest)
        FROM FriendRequest friendRequest
        WHERE friendRequest.status = :status
            AND (
                (
                    friendRequest.sender.id = :firstUserId
                    AND friendRequest.recipient.id = :secondUserId
                )
            OR
                (
                    friendRequest.sender.id = :secondUserId
                    AND friendRequest.recipient.id = :firstUserId
                )
            )
        """)
    long countBetweenUsersWithStatus(
        @Param("firstUserId") Long firstUserId,
        @Param("secondUserId") Long secondUserId,
        @Param("status") FriendRequestStatus status
    );

    @EntityGraph(attributePaths = {"sender", "recipient"})
    Optional<FriendRequest> findByIdAndRecipientId(Long id,Long recipientId);


    @EntityGraph(attributePaths = {"sender", "recipient"})
    @Query("""
        SELECT friendRequest
        FROM FriendRequest friendRequest
        WHERE friendRequest.status = :status
            AND (
            friendRequest.sender.id = :userId
            OR friendRequest.recipient.id = :userId
            )
        """)
    List<FriendRequest> findInvolvingUserWithStatus(
        @Param("userId") Long userId,
        @Param("status") FriendRequestStatus status
    );


    // can replace the Queries with (for example):
    // " boolean existsByStatusAndSenderIdAndRecipientIdOrStatusAndSenderIdAndRecipientId"
    // But that is uncessarily long and also makes us query status twice.
}
