package com.mindmesh.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindmesh.backend.entity.CFC;

// Not much additional method signatures needed yet
public interface CFCRepository extends JpaRepository<CFC, Long> {
}
