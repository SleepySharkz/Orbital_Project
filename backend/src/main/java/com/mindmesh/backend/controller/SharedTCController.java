package com.mindmesh.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.responses.sharing.SharedTCDetailDto;
import com.mindmesh.backend.dto.responses.sharing.SharedTCSummaryDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.SharedTCService;

@RestController
@RequestMapping("/api/v1/shared-tcs")
public class SharedTCController {

  private final SharedTCService sharedTcService;

  public SharedTCController(SharedTCService sharedTcService) {
    this.sharedTcService = sharedTcService;
  }

  @GetMapping
  public ResponseEntity<List<SharedTCSummaryDto>> listSharedTcs(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(sharedTcService.listSharedTcs(userDetails.getId()));
  }

  @GetMapping("/{sharedTcId}")
  public ResponseEntity<SharedTCDetailDto> getSharedTcById(
      @PathVariable Long sharedTcId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        sharedTcService.getSharedTcById(sharedTcId, userDetails.getId()));
  }
}
