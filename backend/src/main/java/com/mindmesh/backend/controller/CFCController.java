package com.mindmesh.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mindmesh.backend.dto.requests.cfc.CreateCFCRequestDto;
import com.mindmesh.backend.dto.responses.cfc.CFCResponseDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.CFCService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/cfcs")
public class CFCController {

  private final CFCService cfcService;

  public CFCController(CFCService cfcService) {
    this.cfcService = cfcService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<CFCResponseDto> createCFC(
      @RequestPart("request") @Valid CreateCFCRequestDto requestDto,
      @RequestParam(required = false) Map<String, MultipartFile> imageFileMap,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    CFCResponseDto response = cfcService.createCFC(
        requestDto,
        userDetails.getId(),
        imageFileMap);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{cfcId}")
  public ResponseEntity<CFCResponseDto> getCFCById(
    @PathVariable Long cfcId,
    @AuthenticationPrincipal CustomUserDetails userDetails) {
      CFCResponseDto response = cfcService.getCFCById(cfcId, userDetails.getId());
      return ResponseEntity.ok(response);
    }
}
