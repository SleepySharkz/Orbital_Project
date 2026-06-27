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

import com.mindmesh.backend.dto.requests.friends.SendFriendRequestDto;
import com.mindmesh.backend.dto.responses.friends.FriendRequestResponseDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.FriendshipService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/friend-requests")
public class FriendRequestController {

    private final FriendshipService friendshipService;

    public FriendRequestController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<FriendRequestResponseDto>> getIncomingRequests(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
            friendshipService.listIncomingRequests(userDetails.getId())
        );
    }

    @GetMapping("/outgoing")
    public ResponseEntity<List<FriendRequestResponseDto>> getOutgoingRequests(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
            friendshipService.listOutgoingRequests(userDetails.getId())
        );
    }

    @PostMapping
    public ResponseEntity<FriendRequestResponseDto> sendFriendRequest(
        @Valid @RequestBody SendFriendRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FriendRequestResponseDto response =
            friendshipService.sendFriendRequest(
                userDetails.getId(),
                requestDto.getRecipientUserId()
            );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<FriendRequestResponseDto> acceptFriendRequest(
        @PathVariable Long requestId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FriendRequestResponseDto response =
            friendshipService.acceptFriendRequest(
                requestId,
                userDetails.getId()
            );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{requestId}/decline")
    public ResponseEntity<FriendRequestResponseDto> declineFriendRequest(
        @PathVariable Long requestId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        FriendRequestResponseDto response =
            friendshipService.declineFriendRequest(
                requestId,
                userDetails.getId()
            );

        return ResponseEntity.ok(response);
    }
}
