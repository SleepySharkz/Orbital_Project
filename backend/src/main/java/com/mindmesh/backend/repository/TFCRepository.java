package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindmesh.backend.entity.TFC;

public interface TFCRepository extends JpaRepository<TFC, Long> {

  Optional<TFC> findByIdAndOwnerId(Long id, Long userId);

  Optional<TFC> findByOwnerIdAndModuleIdAndTopic(Long ownerId, Long moduleId, String topic);

  List<TFC> findAllByModuleId(Long moduleId);

  List<TFC> findAllByOwnerId(Long userId);
}
