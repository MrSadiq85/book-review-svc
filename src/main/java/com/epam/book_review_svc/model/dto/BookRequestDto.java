package com.epam.book_review_svc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequestDto {

    @JsonProperty("isbn")
    @NotBlank(message = "isbn is required")
    private String isbn;

    @JsonProperty("title")
    @NotBlank(message = "title is required")
    private String title;

    @JsonProperty("author")
    @NotBlank(message = "author is required")
    private String author;
}
