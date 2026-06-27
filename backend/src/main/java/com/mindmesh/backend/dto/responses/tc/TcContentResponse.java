package com.mindmesh.backend.dto.responses.tc;

import java.time.LocalDateTime;
import java.util.List;

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

    public TcEntryView(
        Long entryId,
        String topic,
        String flashcardQuestion,
        String flashcardNoteContent,
        String questionText,
        String roughNote,
        LocalDateTime createdAt) {
      this.entryId = entryId;
      this.topic = topic;
      this.flashcardQuestion = flashcardQuestion;
      this.flashcardNoteContent = flashcardNoteContent;
      this.questionText = questionText;
      this.roughNote = roughNote;
      this.createdAt = createdAt;
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
  }
}
