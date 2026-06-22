package com.mindmesh.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.responses.tfc.TfcContentResponse;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.TFCService;

@RestController
@RequestMapping("/api/v1/tfcs")
public class TFCController {

  private final TFCService tfcService;

  public TFCController(TFCService tfcService) {
    this.tfcService = tfcService;
  }

  @GetMapping("/{tfcId}")
  public ResponseEntity<TfcContentResponse> getTfcById(
      @PathVariable Long tfcId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    TfcContentResponse response = tfcService.getTFCById(tfcId, userDetails.getId());
    return ResponseEntity.ok(response);
  }
}
