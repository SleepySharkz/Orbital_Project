package com.mindmesh.backend.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import com.mindmesh.backend.enums.TFCSharingRequestStatus;
import com.mindmesh.backend.repository.TFCRepository;
import com.mindmesh.backend.repository.TFCSharingRequestRepository;
import com.mindmesh.backend.repository.UserRepository;

@Service
public class TFCSharingRequestService {

    private final UserRepository userRepository;
    private final TFCRepository tfcRepository;
    private final TFCSharingRequestRepository tfcSharingRequestRepository;
    private final FriendshipService friendshipService;

    public TFCSharingRequestService(
        UserRepository userRepository,
        TFCRepository tfcRepository,
        TFCSharingRequestRepository tfcSharingRequestRepository,
        FriendshipService friendshipService
    ) {
        this.userRepository = userRepository;
        this.tfcRepository = tfcRepository;
        this.tfcSharingRequestRepository = tfcSharingRequestRepository;
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
        return toDetailDto(savedRequest);
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

        return toDetailDto(sharingRequest);
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

    private TFCSharingRequestDetailDto toDetailDto(TFCSharingRequest request) {
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
            sortedItems(request).stream().map(this::toItemDto).toList()
        );
    }

    private TFCSharingRequestItemDto toItemDto(TFCSharingRequestItem item) {
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
            entries
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
}
