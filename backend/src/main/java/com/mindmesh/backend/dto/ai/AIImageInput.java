package com.mindmesh.backend.dto.ai;

import java.io.IOException;
import java.util.Base64;

import org.springframework.web.multipart.MultipartFile;

public class AIImageInput {

  private final String imageKey;
  private final String fileName;
  private final String contentType;
  private final long sizeBytes;
  private final String base64Data;

  private AIImageInput(
      String imageKey,
      String fileName,
      String contentType,
      long sizeBytes,
      String base64Data) {
    this.imageKey = imageKey;
    this.fileName = fileName;
    this.contentType = contentType;
    this.sizeBytes = sizeBytes;
    this.base64Data = base64Data;
  }

  public static AIImageInput fromMultipartFile(String imageKey, MultipartFile file) throws IOException {
    return new AIImageInput(
        imageKey,
        file.getOriginalFilename(),
        file.getContentType(),
        file.getSize(),
        Base64.getEncoder().encodeToString(file.getBytes()));
  }

  public String getImageKey() {
    return imageKey;
  }

  public String getFileName() {
    return fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public String getBase64Data() {
    return base64Data;
  }

  public String getDataUrl() {
    String mediaType = contentType == null || contentType.isBlank()
        ? "application/octet-stream"
        : contentType;

    return "data:" + mediaType + ";base64," + base64Data;
  }
}
