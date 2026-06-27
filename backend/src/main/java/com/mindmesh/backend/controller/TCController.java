package com.mindmesh.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.responses.tc.TcContentResponse;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.TCService;

@RestController
@RequestMapping("/api/v1/tcs")
public class TCController {

  private final TCService tcService;

  public TCController(TCService tcService) {
    this.tcService = tcService;
  }

  @GetMapping("/{tcId}")
  public ResponseEntity<TcContentResponse> getTcById(
      @PathVariable Long tcId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    TcContentResponse response = tcService.getTCById(tcId, userDetails.getId());
    return ResponseEntity.ok(response);
  }
}
