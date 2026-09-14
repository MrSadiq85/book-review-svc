package com.epam.book_review_svc.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for updating an existing review")
public class UpdateReviewRequest {

    @NotNull(message = "Rating must not be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Updated rating from 1 to 5", example = "5")
    private Integer rating;

    @NotBlank(message = "Review text must not be blank")
    @Size(min = 10, max = 100, message = "Review text must be between 10 and 100 characters")
    @Schema(description = "Updated review text",
            example = "After further reflection, this book is absolutely outstanding!")
    private String reviewText;
}
