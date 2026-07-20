package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.TCInsightPoint;
import com.mindmesh.backend.entity.TCInsightRefreshJob;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.TCInsightRefreshJobStatus;
import com.mindmesh.backend.enums.TCInsightStatus;
import com.mindmesh.backend.repository.TCInsightRefreshJobRepository;

@ExtendWith(MockitoExtension.class)
class TCInsightRefreshJobServiceTest {

  @Mock private TCInsightRefreshJobRepository jobRepository;
  @Mock private TCInsightService insightService;

  @Test
  void failedRefreshKeepsPreviousReadableContent() {
    User owner = new User("Timmy", "timmy@example.com", "hashed");
    ReflectionTestUtils.setField(owner, "id", 1L);
    CourseModule module = new CourseModule(owner, "CS2040", "Y1S2", List.of());
    ReflectionTestUtils.setField(module, "id", 2L);
    TC tcA = new TC(module, owner, "Trees");
    TC tcB = new TC(module, owner, "Graphs");
    ReflectionTestUtils.setField(tcA, "id", 10L);
    ReflectionTestUtils.setField(tcB, "id", 11L);
    TCInsight insight = new TCInsight(owner, module, tcA, tcB);
    TCInsightPoint point = new TCInsightPoint("Link", "Explanation", null, null, 0);
    insight.markReady("Old title", "Old summary", List.of(point), "a", "b", Instant.now());

    TCInsightRefreshJob job = new TCInsightRefreshJob(insight, "new-a", "b");
    job.markRunning(Instant.now());
    when(jobRepository.findLockedById(7L)).thenReturn(Optional.of(job));

    new TCInsightRefreshJobService(jobRepository, insightService)
        .completeFailure(7L, "provider unavailable", Instant.now());

    assertEquals(TCInsightRefreshJobStatus.FAILED, job.getStatus());
    assertEquals(TCInsightStatus.REFRESH_FAILED, insight.getStatus());
    assertEquals("Old title", insight.getTitle());
    assertEquals("Old summary", insight.getSummary());
    assertSame(point, insight.getPoints().get(0));
  }
}
