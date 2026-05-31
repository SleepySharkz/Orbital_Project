package com.mindmesh.backend.service;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.mindmesh.backend.dto.requests.cfc.CreateCFCRequestDto;
import com.mindmesh.backend.entity.CourseModule;

public interface AICFCGenerationService {
    AIGeneratedCFCResponse generateCFC(
        CourseModule module, 
        CreateCFCRequestDto requestDto, 
        Map<String, MultipartFile> imageFileMap);
}
