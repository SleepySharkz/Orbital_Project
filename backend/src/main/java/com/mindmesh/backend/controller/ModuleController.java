package com.mindmesh.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.requests.ModuleRelated.CreateModuleRequestDto;
import com.mindmesh.backend.dto.requests.ModuleRelated.ModuleTopicDto;
import com.mindmesh.backend.dto.requests.ModuleRelated.UpdateModuleRequestDto;
import com.mindmesh.backend.dto.responses.ModuleRelated.ModuleResponseDto;
import com.mindmesh.backend.dto.responses.ModuleRelated.ModuleSummaryDto;
import com.mindmesh.backend.dto.responses.ModuleRelated.ModuleTopicsResponseDto;
import com.mindmesh.backend.dto.responses.cfc.CFCSummaryDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.CFCService;
import com.mindmesh.backend.service.ModuleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/modules")
public class ModuleController {

  private final ModuleService moduleService;
  private final CFCService cfcService;

  public ModuleController(ModuleService moduleService, CFCService cfcService) {
    this.moduleService = moduleService;
    this.cfcService = cfcService;
  }

  @PostMapping
  public ResponseEntity<ModuleResponseDto> createModule(
      @Valid @RequestBody CreateModuleRequestDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ModuleResponseDto moduleCreationResponse = moduleService.createModule(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(moduleCreationResponse);
  }

  @GetMapping
  public ResponseEntity<List<ModuleSummaryDto>> getModules(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<ModuleSummaryDto> modules = moduleService.getModulesForUser(userDetails.getId());
    return ResponseEntity.ok(modules);
  }

  @GetMapping("/{moduleId}")
  public ResponseEntity<ModuleResponseDto> getModuleById(
      @PathVariable Long moduleId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ModuleResponseDto module = moduleService.getModuleById(moduleId, userDetails.getId());
    return ResponseEntity.ok(module);
  }

  @GetMapping("/{moduleId}/topics")
  public ResponseEntity<ModuleTopicsResponseDto> getModuleTopics(
      @PathVariable Long moduleId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ModuleTopicsResponseDto moduleTopics = moduleService.getModuleTopics(moduleId, userDetails.getId());
    return ResponseEntity.ok(moduleTopics);
  }

  @PutMapping("/{moduleId}")
  public ResponseEntity<ModuleResponseDto> updateModule(
      @PathVariable Long moduleId,
      @Valid @RequestBody UpdateModuleRequestDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    ModuleResponseDto updatedModule = moduleService.updateModule(moduleId, userDetails.getId(), request);
    return ResponseEntity.ok(updatedModule);
  }

  @GetMapping("/{moduleId}/cfcs")
    public ResponseEntity<List<CFCSummaryDto>> getCFCsForModule(
      @PathVariable Long moduleId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<CFCSummaryDto> cfcs = cfcService.getCFCsForModule(moduleId, userDetails.getId());
    return ResponseEntity.ok(cfcs);
  }

  @PostMapping("/{moduleId}/topics")
    public ResponseEntity<ModuleTopicsResponseDto> addModuleTopic(
      @PathVariable Long moduleId,
      @Valid @RequestBody ModuleTopicDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
      ModuleTopicsResponseDto updatedTopics = moduleService.addTopicToModule(
        moduleId,
        userDetails.getId(),
        request.getTopic()
      );
      return ResponseEntity.ok(updatedTopics);
    }
  
  @DeleteMapping("/{moduleId}/topics")
    public ResponseEntity<ModuleTopicsResponseDto> removeModuleTopic(
      @PathVariable Long moduleId,
      @Valid @RequestBody ModuleTopicDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
      ModuleTopicsResponseDto updatedTopics = moduleService.removeTopicFromModule(
        moduleId,
        userDetails.getId(),
        request.getTopic()
      );
      return ResponseEntity.ok(updatedTopics);
    }
  
}
