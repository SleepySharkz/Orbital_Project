package com.mindmesh.backend.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.TCInsightPoint;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.TCInsightStatus;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles({ "test", "local-ai-fake" })
class TCInsightRepositoryIntegrationTest {

  @Autowired
  private TCInsightRepository tcInsightRepository;

  @Autowired
  private TCRepository tcRepository;

  @Autowired
  private CourseModuleRepository courseModuleRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  @BeforeEach
  void cleanDatabase() {
    tcInsightRepository.deleteAll();
    tcRepository.deleteAll();
    courseModuleRepository.deleteAll();
    userRepository.deleteAll();
  }

  @AfterEach
  void cleanDatabaseAfterTest() {
    entityManager.clear();
    cleanDatabase();
  }

  @Test
  void canonicalPairUniqueConstraint_rejectsReverseOrderDuplicate() {
    TestData data = saveTestData();

    tcInsightRepository.saveAndFlush(
        new TCInsight(data.user(), data.module(), data.treesTc(), data.graphsTc()));

    assertThrows(
        DataIntegrityViolationException.class,
        () -> tcInsightRepository.saveAndFlush(
            new TCInsight(data.user(), data.module(), data.graphsTc(), data.treesTc())));

    entityManager.clear();
  }

  @Test
  void noUsefulLink_isPersistedButNotReturnedAsReadyInsight() {
    TestData data = saveTestData();
    TCInsight insight = new TCInsight(
        data.user(),
        data.module(),
        data.treesTc(),
        data.graphsTc());
    insight.markNoUsefulLink(
        "The available material does not support a concrete link yet.",
        "hash-a",
        "hash-b",
        Instant.parse("2026-07-13T10:00:00Z"));

    TCInsight saved = tcInsightRepository.saveAndFlush(insight);

    assertEquals(TCInsightStatus.NO_USEFUL_LINK, saved.getStatus());
    assertEquals(
        0,
        tcInsightRepository
            .findAllByUserIdAndModuleIdAndStatusOrderByUpdatedAtDesc(
                data.user().getId(),
                data.module().getId(),
                TCInsightStatus.READY)
            .size());
  }

  @Test
  void readyInsight_isReturnedAndDetailLoadsOrderedPoints() {
    TestData data = saveTestData();
    TCInsight insight = new TCInsight(
        data.user(),
        data.module(),
        data.treesTc(),
        data.graphsTc());
    insight.markReady(
        "Trees as structured graphs",
        "Tree algorithms rely on graph structure while exploiting the absence of cycles.",
        List.of(
            new TCInsightPoint(
                "Traversal",
                "Tree traversals are specialized graph traversals.",
                "DFS and BFS notes",
                "Graph traversal notes",
                1),
            new TCInsightPoint(
                "Acyclic structure",
                "A tree is a connected graph with no cycles.",
                "Tree definition",
                "Connectivity and cycles",
                0)),
        "hash-a",
        "hash-b",
        Instant.parse("2026-07-13T10:00:00Z"));

    TCInsight saved = tcInsightRepository.saveAndFlush(insight);
    entityManager.clear();
    List<TCInsight> readyInsights = tcInsightRepository
        .findAllByUserIdAndModuleIdAndStatusOrderByUpdatedAtDesc(
            data.user().getId(),
            data.module().getId(),
            TCInsightStatus.READY);
    TCInsight detail = tcInsightRepository
        .findDetailByIdAndUserId(saved.getId(), data.user().getId())
        .orElseThrow();

    assertEquals(1, readyInsights.size());
    assertEquals(saved.getId(), readyInsights.get(0).getId());
    assertEquals(2, detail.getPoints().size());
    assertEquals("Acyclic structure", detail.getPoints().get(0).getHeading());
    assertEquals("Traversal", detail.getPoints().get(1).getHeading());
  }

  @Test
  void findAllInvolvingTc_matchesEitherCanonicalSide() {
    TestData data = saveTestData();
    TCInsight insight = tcInsightRepository.saveAndFlush(
        new TCInsight(data.user(), data.module(), data.treesTc(), data.graphsTc()));

    List<TCInsight> involvingA = tcInsightRepository.findAllInvolvingTc(
        data.user().getId(),
        insight.getTcA().getId());
    List<TCInsight> involvingB = tcInsightRepository.findAllInvolvingTc(
        data.user().getId(),
        insight.getTcB().getId());

    assertEquals(List.of(insight.getId()), involvingA.stream().map(TCInsight::getId).toList());
    assertEquals(List.of(insight.getId()), involvingB.stream().map(TCInsight::getId).toList());
  }

  private TestData saveTestData() {
    User user = userRepository.save(
        new User("Timmy", "timmy@example.com", "hashed"));

    CourseModule module = new CourseModule(
        user,
        "CS2040S",
        "Year1Sem2",
        List.of());
    module.addTopic(new ModuleTopic(null, "Trees"));
    module.addTopic(new ModuleTopic(null, "Graphs"));
    module = courseModuleRepository.save(module);

    TC treesTc = tcRepository.save(new TC(module, user, "Trees"));
    TC graphsTc = tcRepository.save(new TC(module, user, "Graphs"));

    return new TestData(user, module, treesTc, graphsTc);
  }

  private record TestData(
      User user,
      CourseModule module,
      TC treesTc,
      TC graphsTc) {
  }
}
