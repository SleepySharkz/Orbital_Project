package com.mindmesh.backend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.ai.AIGeneratedCFCEntry;
import com.mindmesh.backend.dto.ai.AIGeneratedCFCResponse;
import com.mindmesh.backend.dto.requests.cfc.CFCHeaderDto;
import com.mindmesh.backend.dto.requests.cfc.CreateCFCRequestDto;
import com.mindmesh.backend.dto.requests.cfc.QnNotePairDto;
import com.mindmesh.backend.dto.requests.cfc.UpdateCFCEntryContentRequestDto;
import com.mindmesh.backend.dto.requests.cfc.UpdateCFCSummaryRequestDto;
import com.mindmesh.backend.dto.responses.cfc.CFCEntryResponseDto;
import com.mindmesh.backend.dto.responses.cfc.CFCResponseDto;
import com.mindmesh.backend.dto.responses.cfc.CFCSummaryDto;
import com.mindmesh.backend.dto.responses.cfc.SourceMaterialDto;
import com.mindmesh.backend.entity.CFC;
import com.mindmesh.backend.entity.CFCEntry;
import com.mindmesh.backend.entity.CourseModule;
import com.mindmesh.backend.entity.GeneratedCFCPage;
import com.mindmesh.backend.entity.User;
import com.mindmesh.backend.enums.SourceType;
import com.mindmesh.backend.repository.CFCRepository;
import com.mindmesh.backend.repository.CourseModuleRepository;
import com.mindmesh.backend.service.ai.AICFCGenerationService;

import jakarta.transaction.Transactional;

@Service
public class CFCService {

  private final CFCRepository cfcRepository;
  private final CourseModuleRepository courseModuleRepository;

  private final AICFCGenerationService aicfcGenerationService;
  private final TFCService tfcService;

  public CFCService(
      CFCRepository cfcRepository,
      CourseModuleRepository courseModuleRepository,
      AICFCGenerationService aicfcGenerationService,
      TFCService tfcService) {
    this.cfcRepository = cfcRepository;
    this.courseModuleRepository = courseModuleRepository;
    this.aicfcGenerationService = aicfcGenerationService;
    this.tfcService = tfcService;
  }

  @Transactional
  public CFCResponseDto createCFC(CreateCFCRequestDto requestDto, Long userId,
      Map<String, MultipartFile> imageFileMap) {
    Map<String, MultipartFile> uploadedFiles = imageFileMap == null ? Map.of() : imageFileMap;

    Long moduleId = requestDto.getModuleId();
    CourseModule module = courseModuleRepository
        .findByIdAndUserId(moduleId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module NOT FOUND"));
    User owner = module.getUser();

    List<QnNotePairDto> items = requestDto.getItems();
    CFCHeaderDto headerDto = requestDto.getFlashcardHeader();
    SourceType sourceType = headerDto.getSourceType();
    String sourceTitle = headerDto.getSourceTitle();

    // Start validating
    checkUniqueItemIDs(items);
    checkTopicsBelongToModule(module, items);
    checkEachItemHasQuestionOrImage(items, uploadedFiles);
    checkImageKeysMappedToFiles(items, uploadedFiles);
    checkImageCountPerItem(items);
    checkUploadedFilesArePng(items, uploadedFiles);

    AIGeneratedCFCResponse generatedCFC = aicfcGenerationService.generateCFC(
        module,
        requestDto,
        uploadedFiles);

    String title = generatedCFC.getTitle();
    String summary = generatedCFC.getSummary();

    Map<Long, AIGeneratedCFCEntry> generatedByItemId = generatedCFC.getEntries()
        .stream()
        .collect(Collectors.toMap(
            AIGeneratedCFCEntry::getRequestItemId,
            Function.identity()));

    CFC cfc = new CFC(
        module,
        sourceType,
        sourceTitle,
        title,
        summary);

    for (QnNotePairDto item : items) {
      AIGeneratedCFCEntry generatedEntry = generatedByItemId.get(item.getItemId());

      if (generatedEntry == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_GATEWAY,
            "AI generation did not return content for item " + item.getItemId());
      }

      GeneratedCFCPage generatedCFCPage = new GeneratedCFCPage(
          generatedEntry.getFlashcardQuestion(),
          generatedEntry.getFlashcardNoteContent());

      // Gets auto added to parent CFC
      CFCEntry cfcEntry = new CFCEntry(
          cfc,
          item.getItemId(),
          item.getTopic().trim(),
          normalizeQuestionText(item.getQuestionText()),
          item.getRoughNote().trim(),
          generatedCFCPage);
    }

    CFC savedCfc = cfcRepository.save(cfc);

    List<String> distinctTopics = savedCfc
        .getEntries()
        .stream()
        .map(entry -> entry.getTopic())
        .distinct()
        .toList();

    for (String topic : distinctTopics) {
      tfcService.syncTFCForTopic(module, owner, topic);
    }

    return toCFCResponseDto(savedCfc);

  }

  @Transactional
  public List<CFCSummaryDto> getCFCsForModule(Long moduleId, Long userId) {
    courseModuleRepository.findByIdAndUserId(moduleId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

    return cfcRepository.findByModuleIdAndModuleUserIdOrderByCreatedAtDesc(moduleId, userId)
        .stream()
        .map(this::toCFCSummaryDto)
        .toList();
  }

  @Transactional
  public CFCResponseDto getCFCById(Long cfcId, Long userId) {
    CFC cfc = cfcRepository.findByIdAndModuleUserId(cfcId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CFC not found."));

    return toCFCResponseDto(cfc);
  }

  private CFCSummaryDto toCFCSummaryDto(CFC cfc) {
    return new CFCSummaryDto(
        cfc.getId(),
        cfc.getModule().getId(),
        cfc.getModule().getCourseCode(),
        cfc.getModule().getSchoolSem(),
        cfc.getSourceType(),
        cfc.getSourceTitle(),
        cfc.getTitle(),
        cfc.getSummary(),
        cfc.getCreatedAt());
  }

  @Transactional
  public CFCResponseDto updateCFCSummary(
      Long cfcId,
      Long userId,
      UpdateCFCSummaryRequestDto requestDto) {
    CFC cfc = cfcRepository.findByIdAndModuleUserId(cfcId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CFC not found."));

    cfc.setSummary(requestDto.getSummary().trim());

    CFC savedCfc = cfcRepository.save(cfc);
    return toCFCResponseDto(savedCfc);
  }

  @Transactional
  public CFCEntryResponseDto updateCFCEntryContent(
      Long cfcId,
      Long entryId,
      Long userId,
      UpdateCFCEntryContentRequestDto requestDto) {
    CFC cfc = cfcRepository.findByIdAndModuleUserId(cfcId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CFC not found."));

    CFCEntry entry = cfc.getEntries()
        .stream()
        .filter(currentEntry -> currentEntry.getId().equals(entryId))
        .findFirst()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CFC entry not found."));

    entry.getGeneratedCFCPage().updateContent(
        requestDto.getFlashcardQuestion().trim(),
        requestDto.getFlashcardNoteContent().trim());

    cfcRepository.save(cfc);
    return toCFCEntryResponseDto(entry);
  }

  // Helper validators
  private void checkUniqueItemIDs(List<QnNotePairDto> items) {
    Set<Long> seen = new HashSet<>();

    for (QnNotePairDto item : items) {
      Long itemID = item.getItemId();

      if (seen.contains(itemID)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate item ids found!");
      }

      seen.add(itemID);
    }
  }

  private void checkTopicsBelongToModule(CourseModule module, List<QnNotePairDto> items) {
    for (QnNotePairDto item : items) {
      String topic = item.getTopic();

      List<String> moduleTopicNames = module
          .getTopics()
          .stream()
          .map(moduletopic -> moduletopic.getTopicName())
          .toList();

      if (!moduleTopicNames.contains(topic)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic not found in Module!");
      }
    }
  }

  private void checkEachItemHasQuestionOrImage(List<QnNotePairDto> items, Map<String, MultipartFile> imageFileMap) {
    for (QnNotePairDto item : items) {
      boolean hasText = item.getQuestionText() != null && !item.getQuestionText().isBlank();
      boolean hasImage = item.getImageKeys() != null
          && !item.getImageKeys().isEmpty()
          && item
              .getImageKeys()
              .stream()
              .anyMatch(key -> imageFileMap.containsKey(key) && !imageFileMap.get(key).isEmpty());

      if (!hasText && !hasImage) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Item with id " + item.getItemId() + " must have a question text or an image.");
      }
    }
  }

  private void checkImageKeysMappedToFiles(List<QnNotePairDto> items, Map<String, MultipartFile> imageFileMap) {
    for (QnNotePairDto item : items) {
      List<String> imageKeys = item.getImageKeys();
      if (imageKeys == null || imageKeys.isEmpty())
        continue;

      for (String key : imageKeys) {
        if (!imageFileMap.containsKey(key) || imageFileMap.get(key).isEmpty()) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Image key '" + key + "' in item " + item.getItemId() + " has no corresponding uploaded file.");
        }
      }
    }
  }

  private void checkImageCountPerItem(List<QnNotePairDto> items) {
    for (QnNotePairDto item : items) {
      Long itemID = item.getItemId();
      List<String> imageKeys = item.getImageKeys();
      if (imageKeys != null && imageKeys.size() > 2)
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "More than 2 images sent in " + itemID.toString() + "th entry");
    }
  }

  private void checkUploadedFilesArePng(List<QnNotePairDto> items, Map<String, MultipartFile> imageFileMap) {
    for (QnNotePairDto item : items) {
      List<String> imageKeys = item.getImageKeys();
      if (imageKeys == null || imageKeys.isEmpty()) {
        continue;
      }

      for (String key : imageKeys) {
        MultipartFile file = imageFileMap.get(key);
        String contentType = file.getContentType();

        if (contentType == null || !contentType.equalsIgnoreCase("image/png")) {
          throw new ResponseStatusException(
              HttpStatus.BAD_REQUEST,
              "Uploaded file for image key '" + key + "' must be a PNG.");
        }
      }
    }
  }

  // private GeneratedCFCPage buildPlaceholderGeneratedCFCPage() {
  // return new GeneratedCFCPage(
  // "Placeholder flashcard question",
  // "Placeholder flashcard note content");
  // }

  private String normalizeQuestionText(String questionText) {
    if (questionText == null) {
      return null;
    }

    String trimmedQuestionText = questionText.trim();
    return trimmedQuestionText.isEmpty() ? null : trimmedQuestionText;
  }

  private CFCResponseDto toCFCResponseDto(CFC cfc) {
    return new CFCResponseDto(
        cfc.getId(),
        cfc.getModule().getId(),
        cfc.getModule().getCourseCode(),
        cfc.getModule().getSchoolSem(),
        cfc.getSourceType(),
        cfc.getSourceTitle(),
        cfc.getTitle(),
        cfc.getSummary(),
        cfc.getEntries().stream().map(this::toCFCEntryResponseDto).toList(),
        cfc.getCreatedAt());
  }

  private CFCEntryResponseDto toCFCEntryResponseDto(CFCEntry entry) {
    return new CFCEntryResponseDto(
        entry.getId(),
        entry.getRequestItemId(),
        entry.getTopic(),
        entry.getGeneratedCFCPage().getFlashcardQuestion(),
        entry.getGeneratedCFCPage().getFlashcardNoteContent(),
        new SourceMaterialDto(
            entry.getQuestionText(),
            entry.getRoughNote()));
  }

}
