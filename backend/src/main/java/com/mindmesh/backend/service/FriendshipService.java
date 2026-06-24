package com.mindmesh.backend.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.friends.FriendRequestResponseDto;
import com.mindmesh.backend.dto.responses.friends.FriendSummaryDto;
import com.mindmesh.backend.dto.responses.friends.UserSearchResultDto;
import com.mindmesh.backend.entity.FriendRequest;
import com.mindmesh.backend.entity.Friendship;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.FriendRequestStatus;
import com.mindmesh.backend.repository.FriendRequestRepository;
import com.mindmesh.backend.repository.FriendshipRepository;
import com.mindmesh.backend.repository.UserRepository;


@Service
public class FriendshipService {

    private final UserRepository userRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipService(
        UserRepository userRepository,
        FriendRequestRepository friendRequestRepository,
        FriendshipRepository friendshipRepository
    ) {
        this.userRepository = userRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @Transactional(readOnly = true)
    public List<UserSearchResultDto> searchUsersByEmail(
        String query,
        Long currentUserId
    ) {
        String normalizedQuery = query == null ? "" : query.trim();

        if (normalizedQuery.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Email search query must not be blank."
            );
        }

        List<User> matchingUsers =
            userRepository.findTop20ByEmailContainingIgnoreCaseOrderByEmailAsc(normalizedQuery);


        Set<Long> friendIds = new HashSet<>();
        //to load friend data onlu only once
        for (Friendship friendship : friendshipRepository.findAllForUser(currentUserId)) {

            friendIds.add(friendship.getOtherUser(currentUserId).getId());

        }

        Set<Long> incomingPendingUserIds = new HashSet<>();
        Set<Long> outgoingPendingUserIds = new HashSet<>();

        List<FriendRequest> pendingRequests =
            friendRequestRepository.findInvolvingUserWithStatus(
                currentUserId,
                FriendRequestStatus.PENDING
            );

        for (FriendRequest request : pendingRequests) {

            if (Objects.equals(request.getRecipient().getId(), currentUserId)) {
                incomingPendingUserIds.add(request.getSender().getId());
            }

            if (Objects.equals(request.getSender().getId(), currentUserId)) {
                outgoingPendingUserIds.add(request.getRecipient().getId());
            }

        }

        return matchingUsers
            .stream()
            .map(user -> new UserSearchResultDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                Objects.equals(user.getId(), currentUserId),
                friendIds.contains(user.getId()),
                incomingPendingUserIds.contains(user.getId()),
                outgoingPendingUserIds.contains(user.getId())))
            .toList();
    }

    @Transactional
    public FriendRequestResponseDto sendFriendRequest(
        Long senderId,
        Long recipientId
    ) {
        if (Objects.equals(senderId, recipientId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot send a friend request to yourself."
            );
        }

        User sender = userRepository
            .findById(senderId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Authenticated user not found.")
            );

        User recipient = userRepository
            .findById(recipientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Recipient user not found.")
            );

        long[] pair = canonicalPair(senderId, recipientId);

        if (friendshipRepository.existsByUserAIdAndUserBId(pair[0], pair[1])) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Users are already friends."
            );
        }

        long pendingRequestCount =
            friendRequestRepository.countBetweenUsersWithStatus(
                senderId,
                recipientId,
                FriendRequestStatus.PENDING
            );

        if (pendingRequestCount > 0) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A pending friend request already exists between these users.");
            }

        FriendRequest savedRequest =
            friendRequestRepository.save(new FriendRequest(sender, recipient));

        return toFriendRequestResponseDto(savedRequest);
    }

    @Transactional
    public FriendRequestResponseDto acceptFriendRequest(
        Long requestId,
        Long recipientId
    ) {
        // Look up by both IDs to prevent diff user responding
        FriendRequest request = friendRequestRepository
            .findByIdAndRecipientId(requestId, recipientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Pending friend request not found."));

        ensurePending(request);

        Long senderId = request.getSender().getId();
        long[] pair = canonicalPair(senderId, recipientId);

        if (friendshipRepository.existsByUserAIdAndUserBId(pair[0], pair[1])) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Users are already friends."
            );
        }

        try {

            friendshipRepository.saveAndFlush(
                new Friendship(request.getSender(), request.getRecipient())
            );

        } catch (DataIntegrityViolationException exception) {

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Users are already friends.",
                exception
            );

        }

        request.accept(Instant.now());
        FriendRequest savedRequest = friendRequestRepository.save(request);

        return toFriendRequestResponseDto(savedRequest);
    }

    @Transactional
    public FriendRequestResponseDto declineFriendRequest(
        Long requestId,
        Long recipientId
    ) {
        FriendRequest request = friendRequestRepository
            .findByIdAndRecipientId(requestId, recipientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Pending friend request not found.")
            );

        ensurePending(request);

        request.decline(Instant.now());
        FriendRequest savedRequest = friendRequestRepository.save(request);

        return toFriendRequestResponseDto(savedRequest);
    }

    @Transactional(readOnly = true)
    public List<FriendSummaryDto> listFriends(Long userId) {
        return friendshipRepository
            .findAllForUser(userId)
            .stream()
            .map(friendship -> toFriendSummaryDto(friendship, userId))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponseDto> listIncomingRequests(Long userId) {
        return friendRequestRepository
            .findByRecipientIdAndStatusOrderByCreatedAtDesc(
                userId,
                FriendRequestStatus.PENDING)
            .stream()
            .map(this::toFriendRequestResponseDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<FriendRequestResponseDto> listOutgoingRequests(Long userId) {
        return friendRequestRepository
            .findBySenderIdAndStatusOrderByCreatedAtDesc(
                userId,
                FriendRequestStatus.PENDING)
            .stream()
            .map(this::toFriendRequestResponseDto)
            .toList();
    }

    //for future PRIVATE SHARING
    @Transactional(readOnly = true)
    public boolean areFriends(Long firstUserId, Long secondUserId) {
        if (firstUserId == null || secondUserId == null || Objects.equals(firstUserId, secondUserId)) {
            return false;
        }

        long[] pair = canonicalPair(firstUserId, secondUserId);
        return friendshipRepository.existsByUserAIdAndUserBId(pair[0], pair[1]);
    }

    @Transactional
    public void removeFriend(Long currentUserId, Long friendUserId) {
        if (friendUserId == null || friendUserId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Friend user ID must be positive."
            );
        }

        if (Objects.equals(currentUserId, friendUserId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A user cannot unfriend themselves."
            );
        }

        long[] pair = canonicalPair(currentUserId, friendUserId);

        Friendship friendship = friendshipRepository
            .findByUserAIdAndUserBId(pair[0], pair[1])
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Friendship not found.")
            );

        friendshipRepository.delete(friendship);
    }

    private void ensurePending(FriendRequest request) {
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only pending friend requests can be resolved."
            );
        }
    }

    
    private long[] canonicalPair(Long firstUserId, Long secondUserId) {
        if (firstUserId == null || secondUserId == null) {
            throw new IllegalArgumentException("Both user IDs are required.");
        }

        return firstUserId < secondUserId
            ? new long[] {firstUserId, secondUserId}
            : new long[] {secondUserId, firstUserId};
    }

    private FriendRequestResponseDto toFriendRequestResponseDto(FriendRequest request) {
        User sender = request.getSender();
        User recipient = request.getRecipient();

        return new FriendRequestResponseDto(
            request.getId(),
            sender.getId(),
            sender.getUsername(),
            sender.getEmail(),
            recipient.getId(),
            recipient.getUsername(),
            recipient.getEmail(),
            request.getStatus(),
            request.getCreatedAt(),
            request.getRespondedAt()
        );
    }

    private FriendSummaryDto toFriendSummaryDto(
        Friendship friendship,
        Long currentUserId
    ) {
        User friend = friendship.getOtherUser(currentUserId);

        return new FriendSummaryDto(
            friend.getId(),
            friend.getUsername(),
            friend.getEmail(),
            friendship.getCreatedAt()
        );
    }
}