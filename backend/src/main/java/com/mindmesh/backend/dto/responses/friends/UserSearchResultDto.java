package com.mindmesh.backend.dto.responses.friends;


public class UserSearchResultDto {

    private final Long id;
    private final String username;
    private final String email;
    private final boolean isSelf;
    private final boolean isFriend;
    private final boolean incomingRequestPending;
    private final boolean outgoingRequestPending;

    public UserSearchResultDto(
        Long id,
        String username,
        String email,
        boolean self,
        boolean friend,
        boolean incomingRequestPending,
        boolean outgoingRequestPending
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.isSelf = self;
        this.isFriend = friend;
        this.incomingRequestPending = incomingRequestPending;
        this.outgoingRequestPending = outgoingRequestPending;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public boolean getIsSelf() {
        return isSelf;
    }

    public boolean getIsFriend() {
        return isFriend;
    }

    public boolean getIncomingRequestPending() {
        return incomingRequestPending;
    }

    public boolean getOutgoingRequestPending() {
        return outgoingRequestPending;
    }
}
