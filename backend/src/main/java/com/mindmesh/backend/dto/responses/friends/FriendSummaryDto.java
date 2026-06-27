package com.mindmesh.backend.dto.responses.friends;

import java.time.Instant;

//Represents the other user from the current user's view.
public class FriendSummaryDto {

    private final Long userId;
    private final String username;
    private final String email;
    private final Instant friendsSince;

    public FriendSummaryDto(
        Long userId,
        String username,
        String email,
        Instant friendsSince
    ) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.friendsSince = friendsSince;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Instant getFriendsSince() {
        return friendsSince;
    }
}
