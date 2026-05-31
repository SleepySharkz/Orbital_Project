package com.mindmesh.backend.service;

import java.util.List;

public class AIGeneratedCFCResponse {
    private String title;
    private String summary;
    private List<AIGeneratedCFCEntry> entries;

    public AIGeneratedCFCResponse() {
    }

    public AIGeneratedCFCResponse(
        String title,
        String summary,
        List<AIGeneratedCFCEntry> entries) {
            this.title = title;
            this.summary = summary;
            this.entries = entries;
        }
    
    //Getters
    public String getTitle() {
        return this.title;
    }

    public String getSummary() {
        return this.summary;
    }

    public List<AIGeneratedCFCEntry> getEntries() {
        return this.entries;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setEntries(List<AIGeneratedCFCEntry> entries) {
        this.entries = entries;
    }
}
