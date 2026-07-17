package com.mindmesh.backend.dto.requests.marketplace;

import java.util.List;

import com.mindmesh.backend.enums.PublisherVisibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Purely meant for updating the metadata. For full entry updates, we support republishing
public class UpdateMarketplaceListingMetadataRequestDto {

  @NotBlank(message = "Public title is required.")
  @Size(min = 3, max = 120, message = "Public title must be between 3 and 120 characters.")
  private String publicTitle;

  @Size(max = 1000, message = "Description must not exceed 1000 characters.")
  private String description;

  @Size(max = 10, message = "At most 10 tags are allowed.")
  private List<@NotBlank(message = "Tags must not be blank.") @Size(max = 30, message = "Each tag must not exceed 30 characters.") String> tags;

  @Size(max = 120, message = "Institution must not exceed 120 characters.")
  private String institution;

  @NotNull(message = "Publisher visibility is required.")
  private PublisherVisibility publisherVisibility;

  public UpdateMarketplaceListingMetadataRequestDto() {
  }

  public String getPublicTitle() {
    return publicTitle;
  }

  public void setPublicTitle(String publicTitle) {
    this.publicTitle = publicTitle;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }

  public String getInstitution() {
    return institution;
  }

  public void setInstitution(String institution) {
    this.institution = institution;
  }

  public PublisherVisibility getPublisherVisibility() {
    return publisherVisibility;
  }

  public void setPublisherVisibility(PublisherVisibility publisherVisibility) {
    this.publisherVisibility = publisherVisibility;
  }
}
