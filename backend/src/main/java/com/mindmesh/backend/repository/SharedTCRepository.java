package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mindmesh.backend.entity.SharedTC;

public interface SharedTCRepository extends JpaRepository<SharedTC, Long> {

    @EntityGraph(attributePaths = {"originalOwner", "module", "entries"})
    List<SharedTC> findByOwnerIdOrderByAcceptedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"owner", "originalOwner", "module", "entries"})
    Optional<SharedTC> findByIdAndOwnerId(Long id, Long ownerId);

}