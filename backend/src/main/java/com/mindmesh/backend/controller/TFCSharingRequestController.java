package com.mindmesh.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.requests.sharing.SendTFCSharingRequestDto;
import com.mindmesh.backend.dto.responses.sharing.TFCSharingRequestDetailDto;
import com.mindmesh.backend.dto.responses.sharing.TFCSharingRequestSummaryDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.TFCSharingRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TFCSharingRequestController {

    private final TFCSharingRequestService tfcSharingRequestService;

    public TFCSharingRequestController(TFCSharingRequestService tfcSharingRequestService) {
        this.tfcSharingRequestService = tfcSharingRequestService;
    }

    @PostMapping("/friends/{friendUserId}/tfc-sharing-requests")
    public ResponseEntity<TFCSharingRequestDetailDto> sendTfcSharingRequest(
        @PathVariable Long friendUserId,
        @Valid @RequestBody SendTFCSharingRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TFCSharingRequestDetailDto response = tfcSharingRequestService.sendTfcSharingRequest(
            userDetails.getId(),
            friendUserId,
            requestDto.getTfcIds());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tfc-sharing-requests/incoming")
    public ResponseEntity<List<TFCSharingRequestSummaryDto>> listIncomingSharingRequests(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TFCSharingRequestSummaryDto> response =
            tfcSharingRequestService.listIncomingSharingRequests(userDetails.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tfc-sharing-requests/outgoing")
    public ResponseEntity<List<TFCSharingRequestSummaryDto>> listOutgoingSharingRequests(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TFCSharingRequestSummaryDto> response =
            tfcSharingRequestService.listOutgoingSharingRequests(userDetails.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tfc-sharing-requests/{requestId}")
    public ResponseEntity<TFCSharingRequestDetailDto> getSharingRequestDetail(
        @PathVariable Long requestId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TFCSharingRequestDetailDto response = tfcSharingRequestService.getSharingRequestDetail(
            requestId,
            userDetails.getId());

        return ResponseEntity.ok(response);
    }
}