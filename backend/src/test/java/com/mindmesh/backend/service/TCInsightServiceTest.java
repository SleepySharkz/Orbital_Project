package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TCInsightRepository;
import com.mindmesh.backend.repository.TCRepository;

@ExtendWith(MockitoExtension.class)
class TCInsightServiceTest {

  @Mock
  private TCInsightRepository tcInsightRepository;

  @Mock
  private TCRepository tcRepository;

  @Mock
  private CourseModuleRepository courseModuleRepository;

  private TCInsightService tcInsightService;
  private User owner;
  private CourseModule module;
  private TC treesTc;
  private TC graphsTc;

  @BeforeEach
  void setUp() {
    tcInsightService = new TCInsightService(
        tcInsightRepository,
        tcRepository,
        courseModuleRepository);

    owner = new User("Timmy", "timmy@example.com", "hashed");
    ReflectionTestUtils.setField(owner, "id", 7L);

    module = new CourseModule(owner, "CS2040S", "Year1Sem2", List.of());
    ReflectionTestUtils.setField(module, "id", 12L);
    module.addTopic(new ModuleTopic(null, "Trees"));
    module.addTopic(new ModuleTopic(null, "Graphs"));

    treesTc = tc(101L, module, owner, "Trees");
    graphsTc = tc(102L, module, owner, "Graphs");
  }

  @Test
  void findOrCreateGeneratingInsight_reverseOrderReusesCanonicalPair() {
    when(courseModuleRepository.findByIdAndUserId(12L, 7L))
        .thenReturn(Optional.of(module));
    when(tcRepository.findAllOwnedByIdIn(7L, List.of(101L, 102L)))
        .thenReturn(List.of(graphsTc, treesTc));
    when(tcInsightRepository.findByUserIdAndModuleIdAndTcAIdAndTcBId(
        7L, 12L, 101L, 102L))
        .thenReturn(Optional.empty());
    when(tcInsightRepository.save(any(TCInsight.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    TCInsight first = tcInsightService.findOrCreateGeneratingInsight(
        7L,
        12L,
        List.of(102L, 101L));

    when(tcInsightRepository.findByUserIdAndModuleIdAndTcAIdAndTcBId(
        7L, 12L, 101L, 102L))
        .thenReturn(Optional.of(first));

    TCInsight second = tcInsightService.findOrCreateGeneratingInsight(
        7L,
        12L,
        List.of(101L, 102L));

    assertSame(first, second);
    assertEquals(101L, first.getTcA().getId());
    assertEquals(102L, first.getTcB().getId());
    assertEquals("Trees", first.getTopicA());
    assertEquals("Graphs", first.getTopicB());
    verify(tcInsightRepository, times(1)).save(any(TCInsight.class));
  }

  @Test
  void findOrCreateGeneratingInsight_rejectsTcNotOwnedByCurrentUser() {
    when(courseModuleRepository.findByIdAndUserId(12L, 7L))
        .thenReturn(Optional.of(module));
    when(tcRepository.findAllOwnedByIdIn(7L, List.of(101L, 102L)))
        .thenReturn(List.of(treesTc));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> tcInsightService.findOrCreateGeneratingInsight(
            7L,
            12L,
            List.of(101L, 102L)));

    assertEquals(404, exception.getStatusCode().value());
    verify(tcInsightRepository, never()).save(any(TCInsight.class));
  }

  @Test
  void findOrCreateGeneratingInsight_rejectsTcsFromDifferentModules() {
    CourseModule otherModule = new CourseModule(owner, "CS2100", "Year1Sem2", List.of());
    ReflectionTestUtils.setField(otherModule, "id", 13L);
    otherModule.addTopic(new ModuleTopic(null, "Binary"));
    TC otherModuleTc = tc(103L, otherModule, owner, "Binary");

    when(courseModuleRepository.findByIdAndUserId(12L, 7L))
        .thenReturn(Optional.of(module));
    when(tcRepository.findAllOwnedByIdIn(7L, List.of(101L, 103L)))
        .thenReturn(List.of(treesTc, otherModuleTc));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> tcInsightService.findOrCreateGeneratingInsight(
            7L,
            12L,
            List.of(101L, 103L)));

    assertEquals(400, exception.getStatusCode().value());
    verify(tcInsightRepository, never()).save(any(TCInsight.class));
  }

  @Test
  void findOrCreateGeneratingInsight_rejectsStaleTc() {
    module.removeTopic(module.getTopics().get(0));

    when(courseModuleRepository.findByIdAndUserId(12L, 7L))
        .thenReturn(Optional.of(module));
    when(tcRepository.findAllOwnedByIdIn(7L, List.of(101L, 102L)))
        .thenReturn(List.of(treesTc, graphsTc));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> tcInsightService.findOrCreateGeneratingInsight(
            7L,
            12L,
            List.of(101L, 102L)));

    assertEquals(400, exception.getStatusCode().value());
    verify(tcInsightRepository, never()).save(any(TCInsight.class));
  }

  @Test
  void findOrCreateGeneratingInsight_rejectsSharedTcIdThatIsNotAnOwnedTc() {
    Long sharedTcId = 900L;

    when(courseModuleRepository.findByIdAndUserId(12L, 7L))
        .thenReturn(Optional.of(module));
    when(tcRepository.findAllOwnedByIdIn(7L, List.of(101L, sharedTcId)))
        .thenReturn(List.of(treesTc));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> tcInsightService.findOrCreateGeneratingInsight(
            7L,
            12L,
            List.of(101L, sharedTcId)));

    assertEquals(404, exception.getStatusCode().value());
    verify(tcInsightRepository, never()).save(any(TCInsight.class));
  }

  @Test
  void findOrCreateGeneratingInsight_rejectsSelectionWithoutExactlyTwoIds() {
    ResponseStatusException tooFew = assertThrows(
        ResponseStatusException.class,
        () -> tcInsightService.findOrCreateGeneratingInsight(
            7L,
            12L,
            List.of(101L)));

    ResponseStatusException tooMany = assertThrows(
        ResponseStatusException.class,
        () -> tcInsightService.findOrCreateGeneratingInsight(
            7L,
            12L,
            List.of(101L, 102L, 103L)));

    ResponseStatusException duplicate = assertThrows(
        ResponseStatusException.class,
        () -> tcInsightService.findOrCreateGeneratingInsight(
            7L,
            12L,
            List.of(101L, 101L)));

    assertEquals(400, tooFew.getStatusCode().value());
    assertEquals(400, tooMany.getStatusCode().value());
    assertEquals(400, duplicate.getStatusCode().value());
    verify(courseModuleRepository, never()).findByIdAndUserId(any(), any());
  }

  private TC tc(Long id, CourseModule tcModule, User tcOwner, String topic) {
    TC tc = new TC(tcModule, tcOwner, topic);
    ReflectionTestUtils.setField(tc, "id", id);
    return tc;
  }
}
