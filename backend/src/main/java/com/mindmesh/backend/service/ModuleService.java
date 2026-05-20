package com.mindmesh.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.requests.ModuleRelated.CreateModuleRequestDto;
import com.mindmesh.backend.dto.responses.ModuleRelated.ModuleResponseDto;
import com.mindmesh.backend.dto.responses.ModuleRelated.ModuleSummaryDto;
import com.mindmesh.backend.dto.responses.ModuleRelated.ModuleTopicsResponseDto;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.UserRepository;

@Service
public class ModuleService {

  private final CourseModuleRepository courseModuleRepository;
  private final UserRepository userRepository;

  public ModuleService(
      CourseModuleRepository courseModuleRepository,
      UserRepository userRepository) {
    this.courseModuleRepository = courseModuleRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public ModuleResponseDto createModule(CreateModuleRequestDto request, Long userId) {
    // field validation already handled by dto

    List<String> normTopics = normalizeTopics(request.getTopics());
    checkDuplicateTopics(normTopics);

    String normCourseCode = normalizeCourseCode(request.getCourseCode());
    String normSchoolSem = normalizeSchoolSem(request.getSchoolSem());

    if (courseModuleRepository.existsByUserIdAndCourseCodeIgnoreCaseAndSchoolSemIgnoreCase(
        userId,
        normCourseCode,
        normSchoolSem)) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "A module with that course code and semester already exists.");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));

    CourseModule module = new CourseModule(user, normCourseCode, normSchoolSem, List.of());
    for (String topicName : normTopics) {
      module.addTopic(new ModuleTopic(null, topicName));
    }

    CourseModule savedModule = courseModuleRepository.save(module);
    return toModuleResponseDto(savedModule);
  }

  @Transactional(readOnly = true)
  public List<ModuleSummaryDto> getModulesForUser(Long userId) {
    return courseModuleRepository.findByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(s -> toModuleSummaryDto(s))
        .toList();
  }

  @Transactional(readOnly = true)
  public ModuleResponseDto getModuleById(Long moduleId, Long userId) {
    CourseModule module = getOwnedModuleOrThrow(moduleId, userId);
    return toModuleResponseDto(module);
  }

  @Transactional(readOnly = true)
  public ModuleTopicsResponseDto getModuleTopics(Long moduleId, Long userId) {
    CourseModule module = getOwnedModuleOrThrow(moduleId, userId);
    return toModuleTopicsResponseDto(module);
  }

  // -- private helper methods
  private CourseModule getOwnedModuleOrThrow(Long moduleId, Long userId) {
    return courseModuleRepository.findByIdAndUserId(moduleId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found."));
  }

  private void checkDuplicateTopics(List<String> topics) {
    Set<String> seen = new HashSet<>();

    for (String topic : topics) {
      String topicLowerCase = topic.toLowerCase(Locale.ROOT);
      if (seen.contains(topicLowerCase)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate topics found in new module");
      }
      seen.add(topicLowerCase);
    }
  }

  private String normalizeCourseCode(String courseCode) {
    return courseCode.trim().toUpperCase(Locale.ROOT);
  }

  private String normalizeSchoolSem(String schoolSem) {
    return schoolSem.trim();
  }

  private List<String> normalizeTopics(List<String> topics) {
    return topics
        .stream()
        .map(s -> s.trim()).toList();
  }

  // Mappers to response dtos
  private ModuleResponseDto toModuleResponseDto(CourseModule module) {
    return new ModuleResponseDto(
        module.getId(),
        module.getCourseCode(),
        module.getSchoolSem(),
        module.getTopics().stream().map(ModuleTopic::getTopicName).toList(),
        module.getCreatedAt(),
        module.getUpdatedAt());
  }

  private ModuleSummaryDto toModuleSummaryDto(CourseModule module) {
    return new ModuleSummaryDto(
        module.getId(),
        module.getCourseCode(),
        module.getSchoolSem());
  }

  private ModuleTopicsResponseDto toModuleTopicsResponseDto(CourseModule module) {
    return new ModuleTopicsResponseDto(
        module.getId(),
        module.getCourseCode(),
        module.getTopics().stream().map(ModuleTopic::getTopicName).toList());
  }
}
