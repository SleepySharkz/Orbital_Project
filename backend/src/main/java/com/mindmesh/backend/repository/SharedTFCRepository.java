package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mindmesh.backend.entity.SharedTFC;

public interface SharedTFCRepository extends JpaRepository<SharedTFC, Long> {

    @EntityGraph(attributePaths = {"originalOwner", "module", "entries"})
    List<SharedTFC> findByOwnerIdOrderByAcceptedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"owner", "originalOwner", "module", "entries"})
    Optional<SharedTFC> findByIdAndOwnerId(Long id, Long ownerId);

}