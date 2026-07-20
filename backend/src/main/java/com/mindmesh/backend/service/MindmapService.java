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

import com.mindmesh.backend.dto.responses.mindmap.MindmapResponseDto;
import com.mindmesh.backend.dto.responses.mindmap.MindmapResponseDto.MindmapEdgeDto;
import com.mindmesh.backend.dto.responses.mindmap.MindmapResponseDto.MindmapNodeDto;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCInsight;
import com.mindmesh.backend.enums.TCInsightStatus;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TCInsightRepository;
import com.mindmesh.backend.repository.TCRepository;

@Service
public class MindmapService {

  private final CourseModuleRepository courseModuleRepository;
  private final TCRepository tcRepository;
  private final TCInsightRepository tcInsightRepository;

  public MindmapService(
      CourseModuleRepository courseModuleRepository,
      TCRepository tcRepository,
      TCInsightRepository tcInsightRepository) {
    this.courseModuleRepository = courseModuleRepository;
    this.tcRepository = tcRepository;
    this.tcInsightRepository = tcInsightRepository;
  }

  @Transactional(readOnly = true)
  public MindmapResponseDto getModuleMindmap(Long moduleId, Long userId) {
    CourseModule module = courseModuleRepository.findByIdAndUserId(moduleId, userId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Module not found."));

    Set<String> activeTopics = module.getTopics()
        .stream()
        .map(topic -> normalizeTopic(topic.getTopicName()))
        .collect(Collectors.toSet());

    List<TC> activeTcs = tcRepository.findMindmapCandidates(userId, moduleId)
        .stream()
        .filter(tc -> activeTopics.contains(normalizeTopic(tc.getTopic())))
        .sorted(Comparator
            .comparing(TC::getTopic, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(TC::getId))
        .toList();

    Set<Long> activeTcIds = activeTcs.stream()
        .map(TC::getId)
        .collect(Collectors.toSet());

    List<MindmapNodeDto> nodes = activeTcs.stream()
        .map(this::toNodeDto)
        .toList();

    List<MindmapEdgeDto> edges = tcInsightRepository
        .findReadableMindmapEdges(
            userId,
            moduleId,
            TCInsightStatus.READY,
            TCInsightStatus.REFRESHING,
            TCInsightStatus.REFRESH_FAILED)
        .stream()
        .filter(insight -> activeTcIds.contains(insight.getTcA().getId()))
        .filter(insight -> activeTcIds.contains(insight.getTcB().getId()))
        .map(this::toEdgeDto)
        .toList();

    return new MindmapResponseDto(
        module.getId(),
        module.getCourseCode(),
        module.getSchoolSem(),
        nodes,
        edges);
  }

  private MindmapNodeDto toNodeDto(TC tc) {
    return new MindmapNodeDto(
        tc.getId(),
        tc.getTopic(),
        tc.getEntries().size(),
        tc.getUpdatedAt());
  }

  private MindmapEdgeDto toEdgeDto(TCInsight insight) {
    return new MindmapEdgeDto(
        insight.getId(),
        insight.getTcA().getId(),
        insight.getTcB().getId(),
        insight.getStatus(),
        insight.getTitle(),
        insight.getSummary(),
        insight.getUpdatedAt(),
        insight.getStatus() == TCInsightStatus.REFRESHING);
  }

  private String normalizeTopic(String topic) {
    return topic.trim().toLowerCase(Locale.ROOT);
  }
}
