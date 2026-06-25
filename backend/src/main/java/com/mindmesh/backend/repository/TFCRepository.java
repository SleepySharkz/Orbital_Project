package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.TFC;

public interface TFCRepository extends JpaRepository<TFC, Long> {

  Optional<TFC> findByIdAndOwnerId(Long id, Long userId);

  Optional<TFC> findByOwnerIdAndModuleIdAndTopic(Long ownerId, Long moduleId, String topic);

  List<TFC> findAllByOwnerIdAndModuleIdOrderByUpdatedAtDesc(Long ownerId, Long moduleId);

  List<TFC> findAllByModuleId(Long moduleId);

  List<TFC> findAllByOwnerId(Long userId);

  @EntityGraph(attributePaths = {"owner", "module"})
  @Query("""
      SELECT DISTINCT tfc
      FROM TFC tfc
      WHERE tfc.owner.id = :ownerId
        AND tfc.id IN :ids
      """)
  List<TFC> findAllOwnedByIdIn(@Param("ownerId") Long ownerId, @Param("ids") List<Long> ids);
}
