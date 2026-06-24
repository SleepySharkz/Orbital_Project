package com.mindmesh.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.responses.friends.FriendSummaryDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.FriendshipService;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping
    public ResponseEntity<List<FriendSummaryDto>> getFriends(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(
            friendshipService.listFriends(userDetails.getId())
        );
    }

    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<Void> removeFriend(
        @PathVariable Long friendUserId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        friendshipService.removeFriend(userDetails.getId(), friendUserId);
        return ResponseEntity.noContent().build();
    }
}
