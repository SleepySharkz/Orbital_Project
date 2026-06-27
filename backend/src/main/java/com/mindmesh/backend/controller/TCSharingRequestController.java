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

import com.mindmesh.backend.dto.requests.sharing.SendTCSharingRequestDto;
import com.mindmesh.backend.dto.responses.sharing.TCSharingRequestDetailDto;
import com.mindmesh.backend.dto.responses.sharing.TCSharingRequestSummaryDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.TCSharingRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class TCSharingRequestController {

    private final TCSharingRequestService tcSharingRequestService;

    public TCSharingRequestController(TCSharingRequestService tcSharingRequestService) {
        this.tcSharingRequestService = tcSharingRequestService;
    }

    @PostMapping("/friends/{friendUserId}/tc-sharing-requests")
    public ResponseEntity<TCSharingRequestDetailDto> sendTcSharingRequest(
        @PathVariable Long friendUserId,
        @Valid @RequestBody SendTCSharingRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TCSharingRequestDetailDto response = tcSharingRequestService.sendTcSharingRequest(
            userDetails.getId(),
            friendUserId,
            requestDto.getTcIds());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tc-sharing-requests/incoming")
    public ResponseEntity<List<TCSharingRequestSummaryDto>> listIncomingSharingRequests(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TCSharingRequestSummaryDto> response =
            tcSharingRequestService.listIncomingSharingRequests(userDetails.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tc-sharing-requests/outgoing")
    public ResponseEntity<List<TCSharingRequestSummaryDto>> listOutgoingSharingRequests(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<TCSharingRequestSummaryDto> response =
            tcSharingRequestService.listOutgoingSharingRequests(userDetails.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/tc-sharing-requests/{requestId}")
    public ResponseEntity<TCSharingRequestDetailDto> getSharingRequestDetail(
        @PathVariable Long requestId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TCSharingRequestDetailDto response = tcSharingRequestService.getSharingRequestDetail(
            requestId,
            userDetails.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/tc-sharing-requests/{requestId}/accept")
    public ResponseEntity<TCSharingRequestDetailDto> acceptSharingRequest(
        @PathVariable Long requestId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TCSharingRequestDetailDto response = tcSharingRequestService.acceptTcSharingRequest(
            requestId,
            userDetails.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/tc-sharing-requests/{requestId}/decline")
    public ResponseEntity<TCSharingRequestDetailDto> declineSharingRequest(
        @PathVariable Long requestId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TCSharingRequestDetailDto response = tcSharingRequestService.declineTcSharingRequest(
            requestId,
            userDetails.getId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/tc-sharing-requests/{requestId}/cancel")
    public ResponseEntity<TCSharingRequestDetailDto> cancelSharingRequest(
        @PathVariable Long requestId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        TCSharingRequestDetailDto response = tcSharingRequestService.cancelTcSharingRequest(
            requestId,
            userDetails.getId());

        return ResponseEntity.ok(response);
    }
}
