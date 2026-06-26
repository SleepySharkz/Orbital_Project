package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.TCSharingRequest;
import com.mindmesh.backend.enums.TCSharingRequestStatus;

public interface TCSharingRequestRepository extends JpaRepository<TCSharingRequest, Long> {

    @EntityGraph(attributePaths = {"sender", "recipient"})
    List<TCSharingRequest> findByRecipientIdAndStatusOrderByCreatedAtDesc(
        Long recipientId,
        TCSharingRequestStatus status
      );

    @EntityGraph(attributePaths = {"sender", "recipient"})
    List<TCSharingRequest> findBySenderIdAndStatusOrderByCreatedAtDesc(
        Long senderId,
        TCSharingRequestStatus status
      );

    boolean existsBySenderIdAndRecipientIdAndStatus(
        Long senderId,
        Long recipientId,
        TCSharingRequestStatus status
      );

    @EntityGraph(attributePaths = {"sender", "recipient"})
    @Query("""
        SELECT request
        FROM TCSharingRequest request
        WHERE request.id = :requestId
          AND (
            request.sender.id = :userId
            OR request.recipient.id = :userId
          )
        """)
    Optional<TCSharingRequest> findVisibleDetailById(
        @Param("requestId") Long requestId,
        @Param("userId") Long userId
      );

    @EntityGraph(attributePaths = {"sender", "recipient"})
    List<TCSharingRequest> findBySenderIdAndRecipientIdAndStatus(
        Long senderId,
        Long recipientId,
        TCSharingRequestStatus status
      );

    @EntityGraph(attributePaths = {"sender", "recipient"})
    @Query("""
          SELECT request
          FROM TCSharingRequest request
          WHERE request.status = :status
            AND (
              (request.sender.id = :firstUserId AND request.recipient.id = :secondUserId)
              OR
              (request.sender.id = :secondUserId AND request.recipient.id = :firstUserId)
            )
          """)
      List<TCSharingRequest> findBetweenUsersWithStatus(
              @Param("firstUserId") Long firstUserId,
              @Param("secondUserId") Long secondUserId,
              @Param("status") TCSharingRequestStatus status);

      @EntityGraph(attributePaths = {"sender", "recipient"})
      Optional<TCSharingRequest> findByIdAndSenderId(Long id, Long senderId);
}