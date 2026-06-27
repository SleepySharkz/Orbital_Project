package com.mindmesh.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mindmesh.backend.dto.responses.friends.UserSearchResultDto;
import com.mindmesh.backend.security.CustomUserDetails;
import com.mindmesh.backend.service.FriendshipService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final FriendshipService friendshipService;

    public UserController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchResultDto>> searchUsersByEmail(
        @RequestParam("email") String email,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        List<UserSearchResultDto> results = 
            friendshipService.searchUsersByEmail(email, userDetails.getId());

        return ResponseEntity.ok(results);
    }
}
