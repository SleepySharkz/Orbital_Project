package com.mindmesh.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.requests.mindmap.DiscoverInsightRequestDto;
import com.mindmesh.backend.dto.responses.mindmap.MindmapInsightDetailDto;
import com.mindmesh.backend.enums.TCInsightStatus;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.MindmapInsightDiscoveryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class MindmapInsightController {

  private final MindmapInsightDiscoveryService discoveryService;

  public MindmapInsightController(MindmapInsightDiscoveryService discoveryService) {
    this.discoveryService = discoveryService;
  }

  @PostMapping("/modules/{moduleId}/mindmap/insights/discover")
  public ResponseEntity<MindmapInsightDetailDto> discover(
      @PathVariable Long moduleId,
      @Valid @RequestBody DiscoverInsightRequestDto request,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    MindmapInsightDetailDto detail = discoveryService.discover(
        userDetails.getId(),
        moduleId,
        request.getTcIds());

    HttpStatus responseStatus = detail.getStatus() == TCInsightStatus.GENERATING
        ? HttpStatus.ACCEPTED
        : HttpStatus.OK;
    return ResponseEntity.status(responseStatus).body(detail);
  }

  @GetMapping("/mindmap/insights/{insightId}")
  public ResponseEntity<MindmapInsightDetailDto> getDetail(
      @PathVariable Long insightId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        discoveryService.getDetail(insightId, userDetails.getId()));
  }
}