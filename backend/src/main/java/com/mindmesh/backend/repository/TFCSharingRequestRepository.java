package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.TFCSharingRequest;
import com.mindmesh.backend.enums.TFCSharingRequestStatus;

public interface TFCSharingRequestRepository extends JpaRepository<TFCSharingRequest, Long> {

  @EntityGraph(attributePaths = {"sender", "recipient"})
  List<TFCSharingRequest> findByRecipientIdAndStatusOrderByCreatedAtDesc(
      Long recipientId,
      TFCSharingRequestStatus status
    );

  @EntityGraph(attributePaths = {"sender", "recipient"})
  List<TFCSharingRequest> findBySenderIdAndStatusOrderByCreatedAtDesc(
      Long senderId,
      TFCSharingRequestStatus status
    );

  boolean existsBySenderIdAndRecipientIdAndStatus(
      Long senderId,
      Long recipientId,
      TFCSharingRequestStatus status
    );

  @EntityGraph(attributePaths = {"sender", "recipient"})
  @Query("""
      SELECT request
      FROM TFCSharingRequest request
      WHERE request.id = :requestId
        AND (
          request.sender.id = :userId
          OR request.recipient.id = :userId
        )
      """)
  Optional<TFCSharingRequest> findVisibleDetailById(
      @Param("requestId") Long requestId,
      @Param("userId") Long userId
    );

  @EntityGraph(attributePaths = {"sender", "recipient"})
  List<TFCSharingRequest> findBySenderIdAndRecipientIdAndStatus(
      Long senderId,
      Long recipientId,
      TFCSharingRequestStatus status
    );
}