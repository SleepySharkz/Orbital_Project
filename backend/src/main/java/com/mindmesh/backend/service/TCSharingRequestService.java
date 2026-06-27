package com.mindmesh.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.sharing.TCSharingRequestDetailDto;
import com.mindmesh.backend.dto.responses.sharing.TCSharingRequestEntrySnapshotDto;
import com.mindmesh.backend.dto.responses.sharing.TCSharingRequestItemDto;
import com.mindmesh.backend.dto.responses.sharing.TCSharingRequestSummaryDto;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TC;
import com.mindmesh.backend.entity.TCSharingRequest;
import com.mindmesh.backend.entity.TCSharingRequestEntrySnapshot;
import com.mindmesh.backend.entity.TCSharingRequestItem;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.TCSharingCompatibilityStatus;
import com.mindmesh.backend.enums.TCSharingRequestStatus;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TCRepository;
import com.mindmesh.backend.repository.TCSharingRequestRepository;
import com.mindmesh.backend.repository.UserRepository;
import com.mindmesh.backend.entity.SharedTC;
import com.mindmesh.backend.entity.SharedTCEntry;
import com.mindmesh.backend.repository.SharedTCRepository;

@Service
public class TCSharingRequestService {

    private final UserRepository userRepository;
    private final TCRepository tcRepository;
    private final TCSharingRequestRepository tcSharingRequestRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final FriendshipService friendshipService;
    private final SharedTCRepository sharedTcRepository;

    public TCSharingRequestService(
        UserRepository userRepository,
        TCRepository tcRepository,
        TCSharingRequestRepository tcSharingRequestRepository,
        CourseModuleRepository courseModuleRepository,
        SharedTCRepository sharedTcRepository,
        FriendshipService friendshipService
    ) {
        this.userRepository = userRepository;
        this.tcRepository = tcRepository;
        this.tcSharingRequestRepository = tcSharingRequestRepository;
        this.courseModuleRepository = courseModuleRepository;
        this.sharedTcRepository = sharedTcRepository;
        this.friendshipService = friendshipService;
    }

    @Transactional
    public TCSharingRequestDetailDto sendTcSharingRequest(
        Long senderId,
        Long recipientId,
        List<Long> tcIds
    ) {
        validateRecipientId(senderId, recipientId);
        validateSelectedTcIds(tcIds);

        User sender = userRepository.findById(senderId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Authenticated user not found.")
            );

        User recipient = userRepository.findById(recipientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Recipient user not found.")
            );

        if (!friendshipService.areFriends(senderId, recipientId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You can only create TC sharing requests with friends."
            );
        }

        if (tcSharingRequestRepository.existsBySenderIdAndRecipientIdAndStatus(
            senderId,
            recipientId,
            TCSharingRequestStatus.PENDING)
        ) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A pending TC sharing request already exists for this friend."
            );
        }

        List<Long> uniqueTcIds = new ArrayList<>(new LinkedHashSet<>(tcIds));
        Map<Long, TC> ownedTcsById = loadOwnedTcsById(senderId, uniqueTcIds);

        TCSharingRequest sharingRequest = new TCSharingRequest(sender, recipient);

        for (int index = 0; index < tcIds.size(); index++) {
            TC tc = ownedTcsById.get(tcIds.get(index));
            buildItemSnapshot(sharingRequest, tc, index);
        }

        TCSharingRequest savedRequest = tcSharingRequestRepository.save(sharingRequest);
        return toDetailDto(savedRequest, senderId);
    }

    @Transactional(readOnly = true)
    public List<TCSharingRequestSummaryDto> listIncomingSharingRequests(Long userId) {
        return tcSharingRequestRepository
            .findByRecipientIdAndStatusOrderByCreatedAtDesc(
                userId,
                TCSharingRequestStatus.PENDING
            )
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TCSharingRequestSummaryDto> listOutgoingSharingRequests(Long userId) {
        return tcSharingRequestRepository
            .findBySenderIdAndStatusOrderByCreatedAtDesc(
                userId,
                TCSharingRequestStatus.PENDING
            )
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public TCSharingRequestDetailDto getSharingRequestDetail(Long requestId, Long userId) {
        TCSharingRequest sharingRequest = tcSharingRequestRepository
            .findVisibleDetailById(requestId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "TC sharing request not found.")
            );

        return toDetailDto(sharingRequest, userId);
    }

    @Transactional
    public TCSharingRequestDetailDto cancelTcSharingRequest(Long requestId, Long senderId) {
        TCSharingRequest sharingRequest = tcSharingRequestRepository
            .findByIdAndSenderId(requestId, senderId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "TC sharing request not found.")
            );

        ensurePending(sharingRequest);

        sharingRequest.cancel(Instant.now());
        TCSharingRequest savedRequest = tcSharingRequestRepository.save(sharingRequest);

        return toDetailDto(savedRequest, senderId);
    }

    private void validateRecipientId(Long senderId, Long recipientId) {
        if (recipientId == null || recipientId <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Recipient user ID must be positive."
            );
        }

        if (senderId != null && senderId.equals(recipientId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot create a TC sharing request with yourself."
            );
        }
    }

    private void validateSelectedTcIds(List<Long> tcIds) {
        if (tcIds == null || tcIds.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "At least one TC must be selected."
            );
        }

        for (Long tcId : tcIds) {
            if (tcId == null || tcId <= 0) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All TC IDs must be positive."
                );
            }
        }

        Set<Long> seen = new HashSet<>();
        for (Long tcId : tcIds) {
            if (!seen.add(tcId)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate TC IDs are not allowed in one TC sharing request."
                );
            }
        }
    }

    private Map<Long, TC> loadOwnedTcsById(Long senderId, List<Long> uniqueTcIds) {
        List<TC> ownedTcs = tcRepository.findAllOwnedByIdIn(senderId, uniqueTcIds);

        if (ownedTcs.size() != uniqueTcIds.size()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "One or more TCs could not be found."
            );
        }

        return ownedTcs
            .stream()
            .collect(Collectors.toMap(TC::getId, Function.identity()));
    }

    private TCSharingRequestItem buildItemSnapshot(
        TCSharingRequest sharingRequest,
        TC tc,
        int displayOrder
    ) {
        CourseModule module = tc.getModule();

        TCSharingRequestItem item = new TCSharingRequestItem(
            sharingRequest,
            displayOrder,
            tc.getId(),
            module.getId(),
            tc.getOwner().getUsername(),
            module.getCourseCode(),
            module.getSchoolSem(),
            tc.getTopic(),
            isTcStale(tc),
            tc.getUpdatedAt()
        );

        List<CFCEntry> entries = tc
            .getEntries()
            .stream()
            .sorted(Comparator.comparing(CFCEntry::getCreatedAt).reversed())
            .toList();

        for (int index = 0; index < entries.size(); index++) {
            buildEntrySnapshot(item, entries.get(index), index);
        }

        return item;
    }

    private TCSharingRequestEntrySnapshot buildEntrySnapshot(
        TCSharingRequestItem item,
        CFCEntry entry,
        int displayOrder
    ) {
        GeneratedCFCPage generatedCFCPage = entry.getGeneratedCFCPage();

        return new TCSharingRequestEntrySnapshot(
            item,
            displayOrder,
            entry.getId(),
            generatedCFCPage.getFlashcardQuestion(),
            generatedCFCPage.getFlashcardNoteContent(),
            entry.getQuestionText(),
            entry.getRoughNote(),
            entry.getCreatedAt()
        );
    }

    private Boolean isTcStale(TC tc) {
        String tcTopic = normalizeTopic(tc.getTopic());

        return tc.getModule()
            .getTopics()
            .stream()
            .map(ModuleTopic::getTopicName)
            .map(this::normalizeTopic)
            .noneMatch(topic -> topic.equals(tcTopic));
    }

    private String normalizeTopic(String topic) {
        return topic == null ? "" : topic.trim().toLowerCase(Locale.ROOT);
    }

    private TCSharingRequestSummaryDto toSummaryDto(TCSharingRequest request) {
        List<TCSharingRequestItem> sortedItems = sortedItems(request);
        List<String> topics = sortedItems
            .stream()
            .map(TCSharingRequestItem::getTopic)
            .toList();

        return new TCSharingRequestSummaryDto(
            request.getId(),
            request.getSender().getId(),
            request.getSender().getUsername(),
            request.getSender().getEmail(),
            request.getRecipient().getId(),
            request.getRecipient().getUsername(),
            request.getRecipient().getEmail(),
            request.getStatus(),
            sortedItems.size(),
            topics,
            request.getCreatedAt()
        );
    }

    private TCSharingRequestDetailDto toDetailDto(TCSharingRequest request, Long viewerUserId) {
    
        boolean viewerIsRecipient = request.getRecipient().getId().equals(viewerUserId);
        CompatibilityEvaluation compatibility = viewerIsRecipient
            ? evaluateCompatibility(request)
            : CompatibilityEvaluation.hidden();

        List<TCSharingRequestItemDto> items = sortedItems(request)
            .stream()
            .map(item -> toItemDto(item, compatibility.forItem(item.getId())))
            .toList();

        boolean canAccept = viewerIsRecipient
            && request.getStatus() == TCSharingRequestStatus.PENDING
            && compatibility.canAccept();

        return new TCSharingRequestDetailDto(
            request.getId(),
            request.getSender().getId(),
            request.getSender().getUsername(),
            request.getSender().getEmail(),
            request.getRecipient().getId(),
            request.getRecipient().getUsername(),
            request.getRecipient().getEmail(),
            request.getStatus(),
            request.getCreatedAt(),
            request.getRespondedAt(),
            items,
            canAccept,
            compatibility.blockingReasons()
        );
    }
    
    private CompatibilityEvaluation evaluateCompatibility(TCSharingRequest request) {
        List<ItemCompatibility> itemResults = sortedItems(request)
            .stream()
            .map(item -> evaluateItemCompatibility(item, request.getRecipient().getId()))
            .toList();

        return new CompatibilityEvaluation(itemResults);
    }

    private ItemCompatibility evaluateItemCompatibility(
        TCSharingRequestItem item,
        Long recipientId
    ) {
        Optional<CourseModule> matchingModule =
            courseModuleRepository.findByUserIdAndCourseCodeIgnoreCaseAndSchoolSemIgnoreCase(
                recipientId,
                item.getCourseCode(),
                item.getSchoolSem()
            );

        if (matchingModule.isEmpty()) {
            return ItemCompatibility.missingModule(
                item.getId(),
                "Missing module " + item.getCourseCode() + " / " + item.getSchoolSem() + "."
            );
        }

        CourseModule module = matchingModule.get();
        boolean hasTopic = module
            .getTopics()
            .stream()
            .map(ModuleTopic::getTopicName)
            .map(this::normalizeTopic)
            .anyMatch(topic -> topic.equals(normalizeTopic(item.getTopic())));

        if (!hasTopic) {
            return ItemCompatibility.missingTopic(
                item.getId(),
                module.getId(),
                "Module " + item.getCourseCode() + " / " + item.getSchoolSem()
                    + " exists, but topic " + item.getTopic() + " is missing."
            );
        }

        return ItemCompatibility.ready(item.getId(), module.getId());
    }



    private TCSharingRequestItemDto toItemDto(
        TCSharingRequestItem item,
        ItemCompatibility compatibility
    ) {
        List<TCSharingRequestEntrySnapshotDto> entries = sortedEntrySnapshots(item)
            .stream()
            .map(this::toEntryDto)
            .toList();

        return new TCSharingRequestItemDto(
            item.getId(),
            item.getSourceTcId(),
            item.getSourceModuleId(),
            item.getSourceOwnerUsername(),
            item.getCourseCode(),
            item.getSchoolSem(),
            item.getTopic(),
            item.getSourceWasStaleAtSendTime(),
            item.getSourceUpdatedAt(),
            entries.size(),
            entries,
            compatibility.matchingRecipientModuleId(),
            compatibility.hasMatchingModule(),
            compatibility.hasMatchingTopic(),
            compatibility.status(),
            compatibility.blockingReason()
        );
    }

    private TCSharingRequestEntrySnapshotDto toEntryDto(
        TCSharingRequestEntrySnapshot snapshot) {
        return new TCSharingRequestEntrySnapshotDto(
            snapshot.getId(),
            snapshot.getSourceEntryId(),
            snapshot.getFlashcardQuestion(),
            snapshot.getFlashcardNoteContent(),
            snapshot.getQuestionText(),
            snapshot.getRoughNote(),
            snapshot.getSourceEntryCreatedAt()
        );
    }

    private List<TCSharingRequestItem> sortedItems(TCSharingRequest request) {
        return request
            .getItems()
            .stream()
            .sorted(Comparator.comparing(TCSharingRequestItem::getDisplayOrder))
            .toList();
    }

    private List<TCSharingRequestEntrySnapshot> sortedEntrySnapshots(
        TCSharingRequestItem item) {
        return item
            .getEntrySnapshots()
            .stream()
            .sorted(Comparator.comparing(TCSharingRequestEntrySnapshot::getDisplayOrder))
            .toList();
    }

    @Transactional
    public TCSharingRequestDetailDto acceptTcSharingRequest(Long requestId, Long recipientId) {
        TCSharingRequest sharingRequest = loadRecipientSharingRequest(requestId, recipientId);
        ensurePending(sharingRequest);
        ensureSourceTcsStillExist(sharingRequest);

        CompatibilityEvaluation compatibility = evaluateCompatibility(sharingRequest);
        if (!compatibility.canAccept()) {
            String reason = compatibility.blockingReasons().isEmpty()
                ? "TC sharing request is not compatible with your modules."
                : String.join(" ", compatibility.blockingReasons());

            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "TC sharing request cannot be accepted: " + reason
            );
        }

        Instant acceptedAt = Instant.now();

        for (TCSharingRequestItem item : sortedItems(sharingRequest)) {
            CourseModule recipientModule = findRecipientModuleForAcceptedItem(item, recipientId);
            SharedTC sharedTc = copySharedTc(
                sharingRequest,
                item,
                recipientModule,
                acceptedAt
            );
            sharedTcRepository.save(sharedTc);
        }

        sharingRequest.accept(acceptedAt);
        TCSharingRequest savedRequest = tcSharingRequestRepository.save(sharingRequest);

        return toDetailDto(savedRequest, recipientId);
    }

    @Transactional
    public TCSharingRequestDetailDto declineTcSharingRequest(Long requestId, Long recipientId) {
        TCSharingRequest sharingRequest = loadRecipientSharingRequest(requestId, recipientId);
        ensurePending(sharingRequest);

        sharingRequest.decline(Instant.now());
        TCSharingRequest savedRequest = tcSharingRequestRepository.save(sharingRequest);

        return toDetailDto(savedRequest, recipientId);
    }

    private TCSharingRequest loadRecipientSharingRequest(Long requestId, Long recipientId) {
        TCSharingRequest sharingRequest = tcSharingRequestRepository
            .findVisibleDetailById(requestId, recipientId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "TC sharing request not found.")
            );

        if (!sharingRequest.getRecipient().getId().equals(recipientId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "TC sharing request not found."
            );
        }

        return sharingRequest;
    }

    private void ensurePending(TCSharingRequest sharingRequest) {
        if (sharingRequest.getStatus() != TCSharingRequestStatus.PENDING) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only pending TC sharing requests can be resolved."
            );
        }
    }

    private void ensureSourceTcsStillExist(TCSharingRequest sharingRequest) {
        List<Long> sourceTcIds = sortedItems(sharingRequest)
            .stream()
            .map(TCSharingRequestItem::getSourceTcId)
            .distinct()
            .toList();

        List<TC> existingSourceTcs = tcRepository.findAllOwnedByIdIn(
            sharingRequest.getSender().getId(),
            sourceTcIds
        );

        if (existingSourceTcs.size() != sourceTcIds.size()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "One or more source TCs no longer exist."
            );
        }
    }

    private CourseModule findRecipientModuleForAcceptedItem(
        TCSharingRequestItem item,
        Long recipientId
    ) {
        return courseModuleRepository
            .findByUserIdAndCourseCodeIgnoreCaseAndSchoolSemIgnoreCase(
                recipientId,
                item.getCourseCode(),
                item.getSchoolSem()
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Recipient module no longer matches this TC sharing request.")
            );
    }

    private SharedTC copySharedTc(
        TCSharingRequest sharingRequest,
        TCSharingRequestItem item,
        CourseModule recipientModule,
        Instant acceptedAt
    ) {
        SharedTC sharedTc = new SharedTC(
            sharingRequest.getRecipient(),
            sharingRequest.getSender(),
            sharingRequest,
            item.getId(),
            recipientModule,
            item.getCourseCode(),
            item.getSchoolSem(),
            item.getTopic(),
            item.getSourceOwnerUsername(),
            acceptedAt
        );

        for (TCSharingRequestEntrySnapshot snapshot : sortedEntrySnapshots(item)) {
            copySharedTcEntry(sharedTc, snapshot);
        }

        return sharedTc;
    }

    private SharedTCEntry copySharedTcEntry(
        SharedTC sharedTc,
        TCSharingRequestEntrySnapshot snapshot
    ) {
        return new SharedTCEntry(
            sharedTc,
            snapshot.getDisplayOrder(),
            snapshot.getSourceEntryId(),
            snapshot.getFlashcardQuestion(),
            snapshot.getFlashcardNoteContent(),
            snapshot.getQuestionText(),
            snapshot.getRoughNote(),
            snapshot.getSourceEntryCreatedAt()
        );
    }

    //helper classes
    private record CompatibilityEvaluation(List<ItemCompatibility> itemResults) {
        static CompatibilityEvaluation hidden() {
            return new CompatibilityEvaluation(List.of());
        }

        ItemCompatibility forItem(Long itemId) {
            return itemResults
                .stream()
                .filter(result -> result.itemId().equals(itemId))
                .findFirst()
                .orElse(ItemCompatibility.hidden());
        }

        boolean canAccept() {
            return !itemResults.isEmpty()
                && itemResults.stream().allMatch(ItemCompatibility::isReady);
        }

        List<String> blockingReasons() {
            return itemResults
                .stream()
                .map(ItemCompatibility::blockingReason)
                .filter(reason -> reason != null && !reason.isBlank())
                .toList();
        }
    }

    private record ItemCompatibility(
        Long itemId,
        Long matchingRecipientModuleId,
        Boolean hasMatchingModule,
        Boolean hasMatchingTopic,
        TCSharingCompatibilityStatus status,
        String blockingReason
    ) {
        static ItemCompatibility hidden() {
            return new ItemCompatibility(null, null, null, null, null, null);
        }

        static ItemCompatibility ready(Long itemId, Long matchingRecipientModuleId) {
            return new ItemCompatibility(
                itemId,
                matchingRecipientModuleId,
                true,
                true,
                TCSharingCompatibilityStatus.READY,
                null
            );
        }

        static ItemCompatibility missingModule(Long itemId, String reason) {
            return new ItemCompatibility(
                itemId,
                null,
                false,
                false,
                TCSharingCompatibilityStatus.MISSING_MODULE,
                reason
            );
        }

        static ItemCompatibility missingTopic(
            Long itemId,
            Long matchingRecipientModuleId,
            String reason
        ) {
            return new ItemCompatibility(
                itemId,
                matchingRecipientModuleId,
                true,
                false,
                TCSharingCompatibilityStatus.MISSING_TOPIC,
                reason
            );
        }

        boolean isReady() {
            return status == TCSharingCompatibilityStatus.READY;
        }
    }
}
