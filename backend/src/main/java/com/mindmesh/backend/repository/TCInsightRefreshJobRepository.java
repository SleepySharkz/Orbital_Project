package com.mindmesh.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mindmesh.backend.entity.TCInsightRefreshJob;
import com.mindmesh.backend.enums.TCInsightRefreshJobStatus;

import jakarta.persistence.LockModeType;

public interface TCInsightRefreshJobRepository
    extends JpaRepository<TCInsightRefreshJob, Long> {

  Optional<TCInsightRefreshJob> findByInsightId(Long insightId);

  @EntityGraph(attributePaths = "insight")
  List<TCInsightRefreshJob> findAllByInsightIdIn(Collection<Long> insightIds);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("""
      SELECT job
      FROM TCInsightRefreshJob job
      JOIN FETCH job.insight insight
      JOIN FETCH insight.tcA
      JOIN FETCH insight.tcB
      WHERE job.id = :jobId
      """)
  Optional<TCInsightRefreshJob> findLockedById(@Param("jobId") Long jobId);

  @Query("""
      SELECT job.id
      FROM TCInsightRefreshJob job
      WHERE job.status = :status
      ORDER BY job.id ASC
      """)
  List<Long> findIdsByStatus(
      @Param("status") TCInsightRefreshJobStatus status,
      Pageable pageable);
}
