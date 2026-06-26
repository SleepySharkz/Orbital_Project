package com.mindmesh.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.friends.FriendRequestResponseDto;
import com.mindmesh.backend.entity.FriendRequest;
import com.mindmesh.backend.entity.Friendship;
import com.mindmesh.backend.entity.TCSharingRequest;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.FriendRequestStatus;
import com.mindmesh.backend.enums.TCSharingRequestStatus;
import com.mindmesh.backend.repository.FriendRequestRepository;
import com.mindmesh.backend.repository.FriendshipRepository;
import com.mindmesh.backend.repository.TCSharingRequestRepository;
import com.mindmesh.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private FriendRequestRepository friendRequestRepository;

  @Mock
  private FriendshipRepository friendshipRepository;

  @Mock
  private TCSharingRequestRepository tcSharingRequestRepository;

  @InjectMocks
  private FriendshipService friendshipService;

  private User alice;
  private User bob;

  @BeforeEach
  void setUpUsers() {
    alice = buildUser(1L, "Alice", "alice@example.com");
    bob = buildUser(2L, "Bob", "bob@example.com");
  }

  @Test
  void sendFriendRequest_rejectsSelfRequest() {
    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> friendshipService.sendFriendRequest(1L, 1L));

    assertEquals(400, exception.getStatusCode().value());
    verify(friendRequestRepository, never()).save(any(FriendRequest.class));
  }

  @Test
  void sendFriendRequest_rejectsMissingRecipient() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> friendshipService.sendFriendRequest(1L, 99L));

    assertEquals(404, exception.getStatusCode().value());
    verify(friendRequestRepository, never()).save(any(FriendRequest.class));
  }

  @Test
  void sendFriendRequest_rejectsAlreadyFriendsPair() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
    when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
    when(friendshipRepository.existsByUserAIdAndUserBId(1L, 2L))
        .thenReturn(true);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> friendshipService.sendFriendRequest(1L, 2L));

    assertEquals(409, exception.getStatusCode().value());
    verify(friendRequestRepository, never()).save(any(FriendRequest.class));
  }

  @Test
  void sendFriendRequest_rejectsPendingRequestInEitherDirection() {
    when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
    when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
    when(friendshipRepository.existsByUserAIdAndUserBId(1L, 2L))
        .thenReturn(false);
    when(friendRequestRepository.countBetweenUsersWithStatus(
        2L,
        1L,
        FriendRequestStatus.PENDING))
        .thenReturn(1L);

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> friendshipService.sendFriendRequest(2L, 1L));

    assertEquals(409, exception.getStatusCode().value());
    verify(friendRequestRepository, never()).save(any(FriendRequest.class));
  }

  @Test
  void sendFriendRequest_savesPendingRequest() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
    when(userRepository.findById(2L)).thenReturn(Optional.of(bob));
    when(friendshipRepository.existsByUserAIdAndUserBId(1L, 2L))
        .thenReturn(false);
    when(friendRequestRepository.countBetweenUsersWithStatus(
        1L,
        2L,
        FriendRequestStatus.PENDING))
        .thenReturn(0L);
    when(friendRequestRepository.save(any(FriendRequest.class)))
        .thenAnswer(invocation -> {
          FriendRequest request = invocation.getArgument(0);
          ReflectionTestUtils.setField(request, "id", 10L);
          ReflectionTestUtils.setField(request, "createdAt", Instant.now());
          return request;
        });

    FriendRequestResponseDto response =
        friendshipService.sendFriendRequest(1L, 2L);

    ArgumentCaptor<FriendRequest> requestCaptor =
        ArgumentCaptor.forClass(FriendRequest.class);
    verify(friendRequestRepository).save(requestCaptor.capture());

    FriendRequest savedRequest = requestCaptor.getValue();
    assertEquals(FriendRequestStatus.PENDING, savedRequest.getStatus());
    assertEquals(alice, savedRequest.getSender());
    assertEquals(bob, savedRequest.getRecipient());
    assertEquals(10L, response.getId());
  }

  @Test
  void acceptFriendRequest_createsCanonicalFriendshipAndAcceptsRequest() {
    FriendRequest request = buildRequest(20L, bob, alice);

    when(friendRequestRepository.findByIdAndRecipientId(20L, 1L))
        .thenReturn(Optional.of(request));
    when(friendshipRepository.existsByUserAIdAndUserBId(1L, 2L))
        .thenReturn(false);
    when(friendshipRepository.saveAndFlush(any(Friendship.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(friendRequestRepository.save(request)).thenReturn(request);

    FriendRequestResponseDto response =
        friendshipService.acceptFriendRequest(20L, 1L);

    ArgumentCaptor<Friendship> friendshipCaptor =
        ArgumentCaptor.forClass(Friendship.class);
    verify(friendshipRepository).saveAndFlush(friendshipCaptor.capture());

    Friendship savedFriendship = friendshipCaptor.getValue();
    assertEquals(1L, savedFriendship.getUserA().getId());
    assertEquals(2L, savedFriendship.getUserB().getId());
    assertEquals(FriendRequestStatus.ACCEPTED, request.getStatus());
    assertNotNull(request.getRespondedAt());
    assertEquals(FriendRequestStatus.ACCEPTED, response.getStatus());
  }

  @Test
  void acceptFriendRequest_rejectsNonRecipient() {
    when(friendRequestRepository.findByIdAndRecipientId(20L, 3L))
        .thenReturn(Optional.empty());

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> friendshipService.acceptFriendRequest(20L, 3L));

    assertEquals(404, exception.getStatusCode().value());
    verify(friendshipRepository, never()).saveAndFlush(any(Friendship.class));
  }

  @Test
  void acceptFriendRequest_rejectsCompletedRequest() {
    FriendRequest request = buildRequest(20L, alice, bob);
    request.decline(Instant.now());

    when(friendRequestRepository.findByIdAndRecipientId(20L, 2L))
        .thenReturn(Optional.of(request));

    ResponseStatusException exception = assertThrows(
        ResponseStatusException.class,
        () -> friendshipService.acceptFriendRequest(20L, 2L));

    assertEquals(409, exception.getStatusCode().value());
    verify(friendshipRepository, never()).saveAndFlush(any(Friendship.class));
  }

  @Test
  void declineFriendRequest_marksRequestDeclinedWithoutFriendship() {
    FriendRequest request = buildRequest(20L, alice, bob);

    when(friendRequestRepository.findByIdAndRecipientId(20L, 2L))
        .thenReturn(Optional.of(request));
    when(friendRequestRepository.save(request)).thenReturn(request);

    FriendRequestResponseDto response =
        friendshipService.declineFriendRequest(20L, 2L);

    assertEquals(FriendRequestStatus.DECLINED, request.getStatus());
    assertNotNull(request.getRespondedAt());
    assertEquals(FriendRequestStatus.DECLINED, response.getStatus());
    verify(friendshipRepository, never()).saveAndFlush(any(Friendship.class));
  }

  @Test
  void areFriends_canonicalizesEitherArgumentOrder() {
    when(friendshipRepository.existsByUserAIdAndUserBId(1L, 2L))
        .thenReturn(true);

    assertTrue(friendshipService.areFriends(1L, 2L));
    assertTrue(friendshipService.areFriends(2L, 1L));
    assertFalse(friendshipService.areFriends(1L, 1L));

    verify(friendshipRepository, times(2))
        .existsByUserAIdAndUserBId(1L, 2L);
  }

  @Test
  void removeFriend_deletesCanonicalFriendship() {
    Friendship friendship = new Friendship(bob, alice);
    when(friendshipRepository.findByUserAIdAndUserBId(1L, 2L))
        .thenReturn(Optional.of(friendship));
    when(tcSharingRequestRepository.findBetweenUsersWithStatus(
        2L,
        1L,
        TCSharingRequestStatus.PENDING))
        .thenReturn(List.of());

    friendshipService.removeFriend(2L, 1L);

    verify(friendshipRepository).delete(friendship);
  }

  @Test
  void removeFriend_cancelsPendingTcSharingRequestsBetweenUsers() {
    Friendship friendship = new Friendship(bob, alice);
    TCSharingRequest aliceToBob = buildTcSharingRequest(10L, alice, bob);
    TCSharingRequest bobToAlice = buildTcSharingRequest(11L, bob, alice);

    when(friendshipRepository.findByUserAIdAndUserBId(1L, 2L))
        .thenReturn(Optional.of(friendship));
    when(tcSharingRequestRepository.findBetweenUsersWithStatus(
        1L,
        2L,
        TCSharingRequestStatus.PENDING))
        .thenReturn(List.of(aliceToBob, bobToAlice));

    friendshipService.removeFriend(1L, 2L);

    assertEquals(TCSharingRequestStatus.CANCELLED, aliceToBob.getStatus());
    assertEquals(TCSharingRequestStatus.CANCELLED, bobToAlice.getStatus());
    assertNotNull(aliceToBob.getRespondedAt());
    assertNotNull(bobToAlice.getRespondedAt());
    verify(tcSharingRequestRepository).saveAll(List.of(aliceToBob, bobToAlice));
    verify(friendshipRepository).delete(friendship);
  }

  private User buildUser(Long id, String username, String email) {
    User user = new User(username, email, "hashed-password");
    ReflectionTestUtils.setField(user, "id", id);
    return user;
  }

  private FriendRequest buildRequest(
      Long id,
      User sender,
      User recipient) {
    FriendRequest request = new FriendRequest(sender, recipient);
    ReflectionTestUtils.setField(request, "id", id);
    ReflectionTestUtils.setField(request, "createdAt", Instant.now());
    return request;
  }

  private TCSharingRequest buildTcSharingRequest(
      Long id,
      User sender,
      User recipient) {
    TCSharingRequest request = new TCSharingRequest(sender, recipient);
    ReflectionTestUtils.setField(request, "id", id);
    ReflectionTestUtils.setField(request, "createdAt", Instant.now());
    return request;
  }
}
