package com.mindmesh.backend.dto.responses.tc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.mindmesh.backend.enums.CFCEntryOrigin;
import com.mindmesh.backend.enums.SourceType;

public class TcContentResponse {

  private Long id;
  private Long moduleId;
  private String courseCode;
  private String schoolSem;
  private String topic;
  private Boolean isStale;
  private LocalDateTime updatedAt;
  private List<TcEntryView> entries;

  public TcContentResponse(
      Long id,
      Long moduleId,
      String courseCode,
      String schoolSem,
      String topic,
      Boolean isStale,
      LocalDateTime updatedAt,
      List<TcEntryView> entries) {
    this.id = id;
    this.moduleId = moduleId;
    this.courseCode = courseCode;
    this.schoolSem = schoolSem;
    this.topic = topic;
    this.isStale = isStale;
    this.updatedAt = updatedAt;
    this.entries = entries;
  }

  public Long getId() {
    return id;
  }

  public Long getModuleId() {
    return moduleId;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public String getSchoolSem() {
    return schoolSem;
  }

  public String getTopic() {
    return topic;
  }

  public Boolean getIsStale() {
    return isStale;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public List<TcEntryView> getEntries() {
    return entries;
  }

  public static class TcEntryView {
    private Long entryId;
    private String topic;
    private String flashcardQuestion;
    private String flashcardNoteContent;
    private String questionText;
    private String roughNote;
    private LocalDateTime createdAt;
    private CFCEntryOrigin origin;
    private String sourceOwnerUsername;
    private SourceType sourceTypeAtShare;
    private String sourceTitleAtShare;
    private Long sourceSharedTcId;
    private Long sourceSharedEntryId;
    private LocalDateTime sourceEntryCreatedAt;
    private Instant mergedAt;

    public TcEntryView(
        Long entryId,
        String topic,
        String flashcardQuestion,
        String flashcardNoteContent,
        String questionText,
        String roughNote,
        LocalDateTime createdAt,
        CFCEntryOrigin origin,
        String sourceOwnerUsername,
        SourceType sourceTypeAtShare,
        String sourceTitleAtShare,
        Long sourceSharedTcId,
        Long sourceSharedEntryId,
        LocalDateTime sourceEntryCreatedAt,
        Instant mergedAt) {
      this.entryId = entryId;
      this.topic = topic;
      this.flashcardQuestion = flashcardQuestion;
      this.flashcardNoteContent = flashcardNoteContent;
      this.questionText = questionText;
      this.roughNote = roughNote;
      this.createdAt = createdAt;
      this.origin = origin;
      this.sourceOwnerUsername = sourceOwnerUsername;
      this.sourceTypeAtShare = sourceTypeAtShare;
      this.sourceTitleAtShare = sourceTitleAtShare;
      this.sourceSharedTcId = sourceSharedTcId;
      this.sourceSharedEntryId = sourceSharedEntryId;
      this.sourceEntryCreatedAt = sourceEntryCreatedAt;
      this.mergedAt = mergedAt;
    }

    public Long getEntryId() {
      return entryId;
    }

    public String getTopic() {
      return topic;
    }

    public String getFlashcardQuestion() {
      return flashcardQuestion;
    }

    public String getFlashcardNoteContent() {
      return flashcardNoteContent;
    }

    public String getQuestionText() {
      return questionText;
    }

    public String getRoughNote() {
      return roughNote;
    }

    public LocalDateTime getCreatedAt() {
      return createdAt;
    }

    public CFCEntryOrigin getOrigin() { return origin; }
    public String getSourceOwnerUsername() { return sourceOwnerUsername; }
    public SourceType getSourceTypeAtShare() { return sourceTypeAtShare; }
    public String getSourceTitleAtShare() { return sourceTitleAtShare; }
    public Long getSourceSharedTcId() { return sourceSharedTcId; }
    public Long getSourceSharedEntryId() { return sourceSharedEntryId; }
    public LocalDateTime getSourceEntryCreatedAt() { return sourceEntryCreatedAt; }
    public Instant getMergedAt() { return mergedAt; }
  }
}
