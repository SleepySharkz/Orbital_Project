package com.mindmesh.backend.service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TCInsightRepository;
import com.mindmesh.backend.repository.TCRepository;

@Service
public class TCInsightService {

  private final TCInsightRepository tcInsightRepository;
  private final TCRepository tcRepository;
  private final CourseModuleRepository courseModuleRepository;

  public TCInsightService(
      TCInsightRepository tcInsightRepository,
      TCRepository tcRepository,
      CourseModuleRepository courseModuleRepository) {
    this.tcInsightRepository = tcInsightRepository;
    this.tcRepository = tcRepository;
    this.courseModuleRepository = courseModuleRepository;
  }

  @Transactional
  public TCInsight findOrCreateGeneratingInsight(
      Long userId,
      Long moduleId,
      List<Long> selectedTcIds) {
    List<Long> normalizedTcIds = validateAndNormalizeSelection(selectedTcIds);

    CourseModule module = courseModuleRepository.findByIdAndUserId(moduleId, userId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Module not found."));

    List<TC> selectedTcs = tcRepository.findAllOwnedByIdIn(userId, normalizedTcIds)
        .stream()
        .sorted(Comparator.comparing(TC::getId))
        .toList();

    if (selectedTcs.size() != 2) {
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "One or more selected TCs were not found.");
    }

    if (selectedTcs.stream().anyMatch(tc -> !tc.getModule().getId().equals(moduleId))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Both selected TCs must belong to the selected module.");
    }

    Set<String> activeTopics = module.getTopics()
        .stream()
        .map(moduleTopic -> normalizeTopic(moduleTopic.getTopicName()))
        .collect(Collectors.toSet());

    if (selectedTcs.stream().anyMatch(tc -> !activeTopics.contains(normalizeTopic(tc.getTopic())))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Stale TCs cannot be used to create an insight.");
    }

    TC tcA = selectedTcs.get(0);
    TC tcB = selectedTcs.get(1);
    User user = module.getUser();

    return tcInsightRepository
        .findByUserIdAndModuleIdAndTcAIdAndTcBId(
            userId,
            moduleId,
            tcA.getId(),
            tcB.getId())
        .orElseGet(() -> tcInsightRepository.save(new TCInsight(user, module, tcA, tcB)));
  }

  private List<Long> validateAndNormalizeSelection(List<Long> selectedTcIds) {
    if (selectedTcIds == null
        || selectedTcIds.size() != 2
        || selectedTcIds.stream().anyMatch(id -> id == null)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Exactly two TC ids are required.");
    }

    if (selectedTcIds.get(0).equals(selectedTcIds.get(1))) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Two different TCs are required.");
    }

    return selectedTcIds.stream().sorted().toList();
  }

  private String normalizeTopic(String topic) {
    return topic.trim().toLowerCase(Locale.ROOT);
  }
}
