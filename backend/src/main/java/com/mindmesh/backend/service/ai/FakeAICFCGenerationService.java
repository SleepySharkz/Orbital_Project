package com.mindmesh.backend.service.ai;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.mindmesh.backend.dto.ai.AIGeneratedCFCEntry;
import com.mindmesh.backend.dto.ai.AIGeneratedCFCResponse;
import com.mindmesh.backend.dto.ai.AIImageInput;
import com.mindmesh.backend.dto.requests.cfc.CreateCFCRequestDto;
import com.mindmesh.backend.dto.requests.cfc.QnNotePairDto;
import com.mindmesh.backend.entity.CourseModule;

//Mainly used for testing to avoid excessive use of API tokens
@Service
@Profile("local-ai-fake")
public class FakeAICFCGenerationService implements AICFCGenerationService {
    @Override
    public AIGeneratedCFCResponse generateCFC(
        CourseModule module,
        CreateCFCRequestDto requestDto,
        Map<String, MultipartFile> imageFileMap
    ) {
        Map<String, MultipartFile> uploadedFiles = imageFileMap == null ? Map.of() : imageFileMap;

        List<AIGeneratedCFCEntry> generatedEntries = requestDto
            .getItems()
            .stream()
            .map(item -> {
                List<AIImageInput> images = buildImageInputs(item, uploadedFiles);

                return new AIGeneratedCFCEntry(
                    item.getItemId(),
                    "Fake learning point for " + item.getTopic(),
                    "Fake explanation generated from the submitted question, rough note, and "
                        + images.size() + " image(s).",
                    "Fake mistake pattern for " + item.getTopic(),
                    "Fake review prompt for " + item.getTopic());
            })
            .toList();

        return new AIGeneratedCFCResponse(
            "Test Title",
            "Placeholder Summary",
            generatedEntries);
    }

    private List<AIImageInput> buildImageInputs(
        QnNotePairDto item,
        Map<String, MultipartFile> imageFileMap
    ) {
        if (item.getImageKeys() == null || item.getImageKeys().isEmpty()) {
            return List.of();
        }

        return item
            .getImageKeys()
            .stream()
            .map(imageKey -> toAIImageInput(imageKey, imageFileMap.get(imageKey)))
            .toList();
    }

    private AIImageInput toAIImageInput(String imageKey, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Could not find uploaded image for AI generation: " + imageKey);
        }

        try {
            return AIImageInput.fromMultipartFile(imageKey, file);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Could not read uploaded image for AI generation: " + imageKey,
                exception);
        }
    }
}
