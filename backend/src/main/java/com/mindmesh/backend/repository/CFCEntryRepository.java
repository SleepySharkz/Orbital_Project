package com.mindmesh.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mindmesh.backend.entity.CFCEntry;

public interface CFCEntryRepository extends JpaRepository<CFCEntry, Long> {

  List<CFCEntry> findAllByCfcModuleUserIdAndCfcModuleIdAndTopic(Long ownerId, Long moduleId, String topic);
}
