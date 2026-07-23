package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.TC;

import jakarta.persistence.LockModeType;

public interface TCRepository extends JpaRepository<TC, Long> {

  Optional<TC> findByIdAndOwnerId(Long id, Long userId);

  @EntityGraph(attributePaths = {"owner", "module", "module.topics"})
  Optional<TC> findWithSourceMetadataByIdAndOwnerId(Long id, Long userId);

  Optional<TC> findByOwnerIdAndModuleIdAndTopic(Long ownerId, Long moduleId, String topic);

  @Query("""
      SELECT tc FROM TC tc
      WHERE tc.owner.id = :ownerId
        AND tc.module.id = :moduleId
        AND LOWER(TRIM(tc.topic)) = LOWER(TRIM(:topic))
      """)
  Optional<TC> findMatchingOwnedTc(
      @Param("ownerId") Long ownerId,
      @Param("moduleId") Long moduleId,
      @Param("topic") String topic);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"owner", "module", "entries", "entries.cfc"})
  @Query("""
      SELECT DISTINCT tc FROM TC tc
      WHERE tc.owner.id = :ownerId
        AND tc.module.id = :moduleId
        AND LOWER(TRIM(tc.topic)) = LOWER(TRIM(:topic))
      """)
  Optional<TC> findMatchingOwnedTcForUpdate(
      @Param("ownerId") Long ownerId,
      @Param("moduleId") Long moduleId,
      @Param("topic") String topic);

  List<TC> findAllByOwnerIdAndModuleIdOrderByUpdatedAtDesc(Long ownerId, Long moduleId);

  List<TC> findAllByModuleId(Long moduleId);

  List<TC> findAllByOwnerId(Long userId);

  @EntityGraph(attributePaths = {"owner", "module"})
  @Query("""
      SELECT DISTINCT tc
      FROM TC tc
      WHERE tc.owner.id = :ownerId
        AND tc.id IN :ids
      """)
  List<TC> findAllOwnedByIdIn(@Param("ownerId") Long ownerId, @Param("ids") List<Long> ids);

  @Query("""
      SELECT DISTINCT tc
      FROM TC tc
      JOIN FETCH tc.entries
      WHERE tc.owner.id = :ownerId
        AND tc.module.id = :moduleId
      ORDER BY tc.topic ASC
      """)
  List<TC> findMindmapCandidates(@Param("ownerId") Long ownerId, @Param("moduleId") Long moduleId);

  @Query("""
      SELECT DISTINCT tc
      FROM TC tc
      JOIN FETCH tc.owner
      JOIN FETCH tc.module
      JOIN FETCH tc.entries entry
      JOIN FETCH entry.cfc
      WHERE tc.owner.id = :ownerId
        AND tc.id IN :ids
      """)
  List<TC> findAllOwnedWithInsightInputsByIdIn(@Param("ownerId") Long ownerId, @Param("ids") List<Long> ids);
}
