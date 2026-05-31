package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindmesh.backend.entity.CFC;

// Not much additional method signatures needed yet
public interface CFCRepository extends JpaRepository<CFC, Long> {

    public List<CFC> findByModuleIdAndModuleUserIdOrderByCreatedAtDesc(Long moduleId, Long userId);

    public Optional<CFC> findByIdAndModuleUserId(Long cfcId, Long userId);
}