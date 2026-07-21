package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.enums.SharedTCStatus;

import jakarta.persistence.LockModeType;

public interface SharedTCRepository extends JpaRepository<SharedTC, Long> {

    @EntityGraph(attributePaths = {"originalOwner", "module", "entries"})
    @Query("""
        SELECT DISTINCT sharedTc FROM SharedTC sharedTc
        WHERE sharedTc.owner.id = :ownerId
          AND (sharedTc.status = :activeStatus OR sharedTc.status IS NULL)
        ORDER BY sharedTc.acceptedAt DESC
        """)
    List<SharedTC> findActiveByOwnerId(
        @Param("ownerId") Long ownerId,
        @Param("activeStatus") SharedTCStatus activeStatus);

    @EntityGraph(attributePaths = {"owner", "originalOwner", "module", "entries", "mergedIntoTc"})
    Optional<SharedTC> findByIdAndOwnerId(Long id, Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"owner", "originalOwner", "module", "entries", "mergedIntoTc"})
    @Query("""
        SELECT DISTINCT sharedTc FROM SharedTC sharedTc
        WHERE sharedTc.id = :sharedTcId AND sharedTc.owner.id = :ownerId
        """)
    Optional<SharedTC> findLockedByIdAndOwnerId(
        @Param("sharedTcId") Long sharedTcId,
        @Param("ownerId") Long ownerId);

}
