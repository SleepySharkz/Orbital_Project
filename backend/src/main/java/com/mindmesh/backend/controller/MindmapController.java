package com.mindmesh.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.responses.mindmap.MindmapResponseDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.MindmapService;

@RestController
@RequestMapping("/api/v1/modules")
public class MindmapController {

  private final MindmapService mindmapService;

  public MindmapController(MindmapService mindmapService) {
    this.mindmapService = mindmapService;
  }

  @GetMapping("/{moduleId}/mindmap")
  public ResponseEntity<MindmapResponseDto> getModuleMindmap(
      @PathVariable Long moduleId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        mindmapService.getModuleMindmap(moduleId, userDetails.getId()));
  }
}
