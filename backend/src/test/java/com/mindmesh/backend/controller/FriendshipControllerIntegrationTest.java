package com.mindmesh.backend.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.mindmesh.backend.entity.FriendRequest;
import com.mindmesh.backend.entity.Friendship;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.FriendRequestStatus;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.FriendRequestRepository;
import com.mindmesh.backend.repository.FriendshipRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.security.CustomUserDetails;

@SpringBootTest
@ActiveProfiles({"test", "local-ai-fake"})
class FriendshipControllerIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FriendRequestRepository friendRequestRepository;

  @Autowired
  private FriendshipRepository friendshipRepository;

  @Autowired
  private CourseModuleRepository courseModuleRepository;

  @Autowired
  private CFCRepository cfcRepository;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(
            org.springframework.security.test.web.servlet.setup
                .SecurityMockMvcConfigurers.springSecurity())
        .build();

    cfcRepository.deleteAll();
    courseModuleRepository.deleteAll();
    friendRequestRepository.deleteAll();
    friendshipRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  void searchUsers_returnsRelationshipFlags() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    User carol = saveUser("Carol", "carol@example.com");
    User dave = saveUser("Dave", "dave@example.com");

    friendshipRepository.save(new Friendship(alice, bob));
    friendRequestRepository.save(new FriendRequest(carol, alice));
    friendRequestRepository.save(new FriendRequest(alice, dave));

    mockMvc.perform(get("/api/v1/users/search")
        .param("email", "EXAMPLE.COM")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(4)))
        .andExpect(jsonPath("$[0].email").value("alice@example.com"))
        .andExpect(jsonPath("$[0].isSelf").value(true))
        .andExpect(jsonPath("$[1].email").value("bob@example.com"))
        .andExpect(jsonPath("$[1].isFriend").value(true))
        .andExpect(jsonPath("$[2].email").value("carol@example.com"))
        .andExpect(jsonPath("$[2].incomingRequestPending").value(true))
        .andExpect(jsonPath("$[3].email").value("dave@example.com"))
        .andExpect(jsonPath("$[3].outgoingRequestPending").value(true));
  }

  @Test
  void sendFriendRequest_persistsPendingRequest() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");

    mockMvc.perform(post("/api/v1/friend-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "recipientUserId": %d
            }
            """.formatted(bob.getId()))
        .with(authentication(authFor(alice))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.senderUserId").value(alice.getId()))
        .andExpect(jsonPath("$.recipientUserId").value(bob.getId()))
        .andExpect(jsonPath("$.status").value("PENDING"));

    assertEquals(1L, friendRequestRepository.count());
  }

  @Test
  void sendFriendRequest_rejectsSameAndOppositeDirectionDuplicates()
      throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendRequestRepository.save(new FriendRequest(alice, bob));

    mockMvc.perform(post("/api/v1/friend-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBodyFor(bob))
        .with(authentication(authFor(alice))))
        .andExpect(status().isConflict());

    mockMvc.perform(post("/api/v1/friend-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content(requestBodyFor(alice))
        .with(authentication(authFor(bob))))
        .andExpect(status().isConflict());

    assertEquals(1L, friendRequestRepository.count());
  }

  @Test
  void requestLists_returnOnlyCurrentUsersPendingRequests() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    User carol = saveUser("Carol", "carol@example.com");

    FriendRequest incoming =
        friendRequestRepository.save(new FriendRequest(bob, alice));
    FriendRequest outgoing =
        friendRequestRepository.save(new FriendRequest(alice, carol));
    friendRequestRepository.save(new FriendRequest(bob, carol));

    mockMvc.perform(get("/api/v1/friend-requests/incoming")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(incoming.getId()))
        .andExpect(jsonPath("$[0].senderEmail").value("bob@example.com"));

    mockMvc.perform(get("/api/v1/friend-requests/outgoing")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].id").value(outgoing.getId()))
        .andExpect(jsonPath("$[0].recipientEmail").value("carol@example.com"));
  }

  @Test
  void acceptFriendRequest_createsCanonicalFriendship() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    FriendRequest request =
        friendRequestRepository.save(new FriendRequest(bob, alice));

    mockMvc.perform(post(
        "/api/v1/friend-requests/" + request.getId() + "/accept")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACCEPTED"))
        .andExpect(jsonPath("$.respondedAt").isNotEmpty());

    FriendRequest acceptedRequest =
        friendRequestRepository.findById(request.getId()).orElseThrow();
    assertEquals(FriendRequestStatus.ACCEPTED, acceptedRequest.getStatus());
    assertNotNull(acceptedRequest.getRespondedAt());

    Friendship friendship = friendshipRepository.findAll().get(0);
    assertEquals(1L, friendshipRepository.count());
    assertEquals(
        Math.min(alice.getId(), bob.getId()),
        friendship.getUserA().getId().longValue());
    assertEquals(
        Math.max(alice.getId(), bob.getId()),
        friendship.getUserB().getId().longValue());
  }

  @Test
  void declineFriendRequest_doesNotCreateFriendship() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    FriendRequest request =
        friendRequestRepository.save(new FriendRequest(bob, alice));

    mockMvc.perform(post(
        "/api/v1/friend-requests/" + request.getId() + "/decline")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DECLINED"));

    assertEquals(0L, friendshipRepository.count());
  }

  @Test
  void nonRecipientCannotResolveRequest() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    User carol = saveUser("Carol", "carol@example.com");
    FriendRequest request =
        friendRequestRepository.save(new FriendRequest(alice, bob));

    mockMvc.perform(post(
        "/api/v1/friend-requests/" + request.getId() + "/accept")
        .with(authentication(authFor(carol))))
        .andExpect(status().isNotFound());

    mockMvc.perform(post(
        "/api/v1/friend-requests/" + request.getId() + "/decline")
        .with(authentication(authFor(carol))))
        .andExpect(status().isNotFound());
  }

  @Test
  void completedRequestCannotBeResolvedAgain() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    FriendRequest request = new FriendRequest(alice, bob);
    request.decline(Instant.now());
    request = friendRequestRepository.save(request);

    mockMvc.perform(post(
        "/api/v1/friend-requests/" + request.getId() + "/accept")
        .with(authentication(authFor(bob))))
        .andExpect(status().isConflict());
  }

  @Test
  void friendList_isSymmetricAndFriendCanBeRemoved() throws Exception {
    User alice = saveUser("Alice", "alice@example.com");
    User bob = saveUser("Bob", "bob@example.com");
    friendshipRepository.saveAndFlush(new Friendship(bob, alice));

    mockMvc.perform(get("/api/v1/friends")
        .with(authentication(authFor(alice))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].userId").value(bob.getId()))
        .andExpect(jsonPath("$[0].friendsSince").isNotEmpty());

    mockMvc.perform(get("/api/v1/friends")
        .with(authentication(authFor(bob))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].userId").value(alice.getId()));

    mockMvc.perform(delete("/api/v1/friends/" + bob.getId())
        .with(authentication(authFor(alice))))
        .andExpect(status().isNoContent());

    assertEquals(0L, friendshipRepository.count());
  }

  @Test
  void friendEndpoints_requireAuthentication() throws Exception {
    mockMvc.perform(get("/api/v1/users/search").param("email", "alice"))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/v1/friends"))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/v1/friend-requests/incoming"))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/v1/friend-requests")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "recipientUserId": 1
            }
            """))
        .andExpect(status().isForbidden());
  }

  private User saveUser(String username, String email) {
    return userRepository.save(new User(username, email, "hashed-password"));
  }

  private String requestBodyFor(User recipient) {
    return """
        {
          "recipientUserId": %d
        }
        """.formatted(recipient.getId());
  }

  private UsernamePasswordAuthenticationToken authFor(User user) {
    CustomUserDetails userDetails = new CustomUserDetails(
        user.getId(),
        user.getEmail(),
        user.getUsername(),
        user.getPasswordHash(),
        AuthorityUtils.NO_AUTHORITIES);

    return new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities());
  }
}
