package com.mindmesh.backend.dto.ai;

import java.util.List;

public class AICFCGenerationRequestItem {
    private Long requestItemId;
    private String topic;
    private String questionText;
    private String roughNotes;
    private List<AIImageInput> images;
    //idk how to store images bruh. imma jst leave this as this for now

    public AICFCGenerationRequestItem (
        Long requestItemId,
        String topic,
        String questionText,
        String roughNotes,
        List<AIImageInput> images) {
            this.requestItemId = requestItemId;
            this.topic = topic;
            this.questionText = questionText;
            this.roughNotes = roughNotes;
            this.images = images;
        }

    public Long getRequestItemId() {
        return this.requestItemId;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getQuestionText() {
        return this.questionText;
    }

    public String getRoughNotes() {
        return this.roughNotes;
    }

    public List<AIImageInput> getAIImageInput() {
        return this.images == null ? List.of() : this.images;
    }

    public List<AIImageInput> getImages() {
        return this.images == null ? List.of() : this.images;
    }
    //this is all for now since idw create the output style yet
}
