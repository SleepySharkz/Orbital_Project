package com.mindmesh.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.enums.TCInsightStatus;

public interface TCInsightRepository extends JpaRepository<TCInsight, Long> {

  @EntityGraph(attributePaths = { "tcA", "tcB" })
  Optional<TCInsight> findByUserIdAndModuleIdAndTcAIdAndTcBId(
      Long userId,
      Long moduleId,
      Long tcAId,
      Long tcBId);

  @EntityGraph(attributePaths = { "tcA", "tcB" })
  List<TCInsight> findAllByUserIdAndModuleIdAndStatusOrderByUpdatedAtDesc(
      Long userId,
      Long moduleId,
      TCInsightStatus status);

  @EntityGraph(attributePaths = { "module", "tcA", "tcB" })
  @Query("""
      SELECT insight
      FROM TCInsight insight
      WHERE insight.user.id = :userId
        AND (insight.tcA.id = :tcId OR insight.tcB.id = :tcId)
      ORDER BY insight.updatedAt DESC
      """)
  List<TCInsight> findAllInvolvingTc(
      @Param("userId") Long userId,
      @Param("tcId") Long tcId);

  @EntityGraph(attributePaths = { "user", "module", "tcA", "tcB", "points" })
  Optional<TCInsight> findDetailByIdAndUserId(Long id, Long userId);


  @EntityGraph(attributePaths = { "tcA", "tcB" })
  @Query("""
      SELECT insight
      FROM TCInsight insight
      WHERE insight.user.id = :userId
        AND insight.module.id = :moduleId
        AND (
          insight.status = :readyStatus
          OR (
            insight.status = :refreshingStatus
            AND insight.title IS NOT NULL
            AND insight.summary IS NOT NULL
          )
        )
      ORDER BY insight.updatedAt DESC
      """)
  List<TCInsight> findReadableMindmapEdges(
      @Param("userId") Long userId,
      @Param("moduleId") Long moduleId,
      @Param("readyStatus") TCInsightStatus readyStatus,
      @Param("refreshingStatus") TCInsightStatus refreshingStatus
  );
}
