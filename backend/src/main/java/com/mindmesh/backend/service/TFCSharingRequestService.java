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

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.responses.sharing.TFCSharingRequestDetailDto;
import com.mindmesh.backend.dto.responses.sharing.TFCSharingRequestEntrySnapshotDto;
import com.mindmesh.backend.dto.responses.sharing.TFCSharingRequestItemDto;
import com.mindmesh.backend.dto.responses.sharing.TFCSharingRequestSummaryDto;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.ModuleTopic;
import com.mindmesh.backend.entity.TFC;
import com.mindmesh.backend.entity.TFCSharingRequest;
import com.mindmesh.backend.entity.TFCSharingRequestEntrySnapshot;
import com.mindmesh.backend.entity.TFCSharingRequestItem;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.TFCSharingCompatibilityStatus;
import com.mindmesh.backend.enums.TFCSharingRequestStatus;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.repository.TFCRepository;
import com.mindmesh.backend.repository.TFCSharingRequestRepository;
import com.mindmesh.backend.repository.UserRepository;

@Service
public class TFCSharingRequestService {

    private final UserRepository userRepository;
    private final TFCRepository tfcRepository;
    private final TFCSharingRequestRepository tfcSharingRequestRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final FriendshipService friendshipService;

    public TFCSharingRequestService(
        UserRepository userRepository,
        TFCRepository tfcRepository,
        TFCSharingRequestRepository tfcSharingRequestRepository,
        CourseModuleRepository courseModuleRepository,
        FriendshipService friendshipService
    ) {
        this.userRepository = userRepository;
        this.tfcRepository = tfcRepository;
        this.tfcSharingRequestRepository = tfcSharingRequestRepository;
        this.courseModuleRepository = courseModuleRepository;
        this.friendshipService = friendshipService;
    }

    @Transactional
    public TFCSharingRequestDetailDto sendTfcSharingRequest(
        Long senderId,
        Long recipientId,
        List<Long> tfcIds
    ) {
        validateRecipientId(senderId, recipientId);
        validateSelectedTfcIds(tfcIds);

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
                "You can only create TFC sharing requests with friends."
            );
        }

        if (tfcSharingRequestRepository.existsBySenderIdAndRecipientIdAndStatus(
            senderId,
            recipientId,
            TFCSharingRequestStatus.PENDING)
        ) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A pending TFC sharing request already exists for this friend."
            );
        }

        List<Long> uniqueTfcIds = new ArrayList<>(new LinkedHashSet<>(tfcIds));
        Map<Long, TFC> ownedTfcsById = loadOwnedTfcsById(senderId, uniqueTfcIds);

        TFCSharingRequest sharingRequest = new TFCSharingRequest(sender, recipient);

        for (int index = 0; index < tfcIds.size(); index++) {
            TFC tfc = ownedTfcsById.get(tfcIds.get(index));
            buildItemSnapshot(sharingRequest, tfc, index);
        }

        TFCSharingRequest savedRequest = tfcSharingRequestRepository.save(sharingRequest);
        return toDetailDto(savedRequest, senderId);
    }

    @Transactional(readOnly = true)
    public List<TFCSharingRequestSummaryDto> listIncomingSharingRequests(Long userId) {
        return tfcSharingRequestRepository
            .findByRecipientIdAndStatusOrderByCreatedAtDesc(
                userId,
                TFCSharingRequestStatus.PENDING
            )
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TFCSharingRequestSummaryDto> listOutgoingSharingRequests(Long userId) {
        return tfcSharingRequestRepository
            .findBySenderIdAndStatusOrderByCreatedAtDesc(
                userId,
                TFCSharingRequestStatus.PENDING
            )
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public TFCSharingRequestDetailDto getSharingRequestDetail(Long requestId, Long userId) {
        TFCSharingRequest sharingRequest = tfcSharingRequestRepository
            .findVisibleDetailById(requestId, userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "TFC sharing request not found.")
            );

        return toDetailDto(sharingRequest, userId);
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
                "You cannot create a TFC sharing request with yourself."
            );
        }
    }

    private void validateSelectedTfcIds(List<Long> tfcIds) {
        if (tfcIds == null || tfcIds.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "At least one TFC must be selected."
            );
        }

        for (Long tfcId : tfcIds) {
            if (tfcId == null || tfcId <= 0) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "All TFC IDs must be positive."
                );
            }
        }

        Set<Long> seen = new HashSet<>();
        for (Long tfcId : tfcIds) {
            if (!seen.add(tfcId)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Duplicate TFC IDs are not allowed in one TFC sharing request."
                );
            }
        }
    }

    private Map<Long, TFC> loadOwnedTfcsById(Long senderId, List<Long> uniqueTfcIds) {
        List<TFC> ownedTfcs = tfcRepository.findAllOwnedByIdIn(senderId, uniqueTfcIds);

        if (ownedTfcs.size() != uniqueTfcIds.size()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "One or more TFCs could not be found."
            );
        }

        return ownedTfcs
            .stream()
            .collect(Collectors.toMap(TFC::getId, Function.identity()));
    }

    private TFCSharingRequestItem buildItemSnapshot(
        TFCSharingRequest sharingRequest,
        TFC tfc,
        int displayOrder
    ) {
        CourseModule module = tfc.getModule();

        TFCSharingRequestItem item = new TFCSharingRequestItem(
            sharingRequest,
            displayOrder,
            tfc.getId(),
            module.getId(),
            tfc.getOwner().getUsername(),
            module.getCourseCode(),
            module.getSchoolSem(),
            tfc.getTopic(),
            isTfcStale(tfc),
            tfc.getUpdatedAt()
        );

        List<CFCEntry> entries = tfc
            .getEntries()
            .stream()
            .sorted(Comparator.comparing(CFCEntry::getCreatedAt).reversed())
            .toList();

        for (int index = 0; index < entries.size(); index++) {
            buildEntrySnapshot(item, entries.get(index), index);
        }

        return item;
    }

    private TFCSharingRequestEntrySnapshot buildEntrySnapshot(
        TFCSharingRequestItem item,
        CFCEntry entry,
        int displayOrder
    ) {
        GeneratedCFCPage generatedCFCPage = entry.getGeneratedCFCPage();

        return new TFCSharingRequestEntrySnapshot(
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

    private Boolean isTfcStale(TFC tfc) {
        String tfcTopic = normalizeTopic(tfc.getTopic());

        return tfc.getModule()
            .getTopics()
            .stream()
            .map(ModuleTopic::getTopicName)
            .map(this::normalizeTopic)
            .noneMatch(topic -> topic.equals(tfcTopic));
    }

    private String normalizeTopic(String topic) {
        return topic == null ? "" : topic.trim().toLowerCase(Locale.ROOT);
    }

    private TFCSharingRequestSummaryDto toSummaryDto(TFCSharingRequest request) {
        List<TFCSharingRequestItem> sortedItems = sortedItems(request);
        List<String> topics = sortedItems
            .stream()
            .map(TFCSharingRequestItem::getTopic)
            .toList();

        return new TFCSharingRequestSummaryDto(
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

    private TFCSharingRequestDetailDto toDetailDto(TFCSharingRequest request, Long viewerUserId) {
    
        boolean viewerIsRecipient = request.getRecipient().getId().equals(viewerUserId);
        CompatibilityEvaluation compatibility = viewerIsRecipient
            ? evaluateCompatibility(request)
            : CompatibilityEvaluation.hidden();

        List<TFCSharingRequestItemDto> items = sortedItems(request)
            .stream()
            .map(item -> toItemDto(item, compatibility.forItem(item.getId())))
            .toList();

        boolean canAccept = viewerIsRecipient
            && request.getStatus() == TFCSharingRequestStatus.PENDING
            && compatibility.canAccept();

        return new TFCSharingRequestDetailDto(
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
    
    private CompatibilityEvaluation evaluateCompatibility(TFCSharingRequest request) {
        List<ItemCompatibility> itemResults = sortedItems(request)
            .stream()
            .map(item -> evaluateItemCompatibility(item, request.getRecipient().getId()))
            .toList();

        return new CompatibilityEvaluation(itemResults);
    }

    private ItemCompatibility evaluateItemCompatibility(
        TFCSharingRequestItem item,
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



    private TFCSharingRequestItemDto toItemDto(
        TFCSharingRequestItem item,
        ItemCompatibility compatibility
    ) {
        List<TFCSharingRequestEntrySnapshotDto> entries = sortedEntrySnapshots(item)
            .stream()
            .map(this::toEntryDto)
            .toList();

        return new TFCSharingRequestItemDto(
            item.getId(),
            item.getSourceTfcId(),
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

    private TFCSharingRequestEntrySnapshotDto toEntryDto(
        TFCSharingRequestEntrySnapshot snapshot) {
        return new TFCSharingRequestEntrySnapshotDto(
            snapshot.getId(),
            snapshot.getSourceEntryId(),
            snapshot.getFlashcardQuestion(),
            snapshot.getFlashcardNoteContent(),
            snapshot.getQuestionText(),
            snapshot.getRoughNote(),
            snapshot.getSourceEntryCreatedAt()
        );
    }

    private List<TFCSharingRequestItem> sortedItems(TFCSharingRequest request) {
        return request
            .getItems()
            .stream()
            .sorted(Comparator.comparing(TFCSharingRequestItem::getDisplayOrder))
            .toList();
    }

    private List<TFCSharingRequestEntrySnapshot> sortedEntrySnapshots(
        TFCSharingRequestItem item) {
        return item
            .getEntrySnapshots()
            .stream()
            .sorted(Comparator.comparing(TFCSharingRequestEntrySnapshot::getDisplayOrder))
            .toList();
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
        TFCSharingCompatibilityStatus status,
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
                TFCSharingCompatibilityStatus.READY,
                null
            );
        }

        static ItemCompatibility missingModule(Long itemId, String reason) {
            return new ItemCompatibility(
                itemId,
                null,
                false,
                false,
                TFCSharingCompatibilityStatus.MISSING_MODULE,
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
                TFCSharingCompatibilityStatus.MISSING_TOPIC,
                reason
            );
        }

        boolean isReady() {
            return status == TFCSharingCompatibilityStatus.READY;
        }
    }
}