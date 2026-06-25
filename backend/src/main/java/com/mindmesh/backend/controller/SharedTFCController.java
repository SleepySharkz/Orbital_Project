package com.mindmesh.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.responses.sharing.SharedTFCDetailDto;
import com.mindmesh.backend.dto.responses.sharing.SharedTFCSummaryDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.SharedTFCService;

@RestController
@RequestMapping("/api/v1/shared-tfcs")
public class SharedTFCController {

  private final SharedTFCService sharedTfcService;

  public SharedTFCController(SharedTFCService sharedTfcService) {
    this.sharedTfcService = sharedTfcService;
  }

  @GetMapping
  public ResponseEntity<List<SharedTFCSummaryDto>> listSharedTfcs(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(sharedTfcService.listSharedTfcs(userDetails.getId()));
  }

  @GetMapping("/{sharedTfcId}")
  public ResponseEntity<SharedTFCDetailDto> getSharedTfcById(
      @PathVariable Long sharedTfcId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    return ResponseEntity.ok(
        sharedTfcService.getSharedTfcById(sharedTfcId, userDetails.getId()));
  }
}
