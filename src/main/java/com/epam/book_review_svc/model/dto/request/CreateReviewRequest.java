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
@Schema(description = "Request object for creating a new review")
public class CreateReviewRequest {

    @NotNull(message = "Book ID must not be null")
    @NotBlank(message = "Book ID must not be blank")
    @Schema(description = "UUID of the book being reviewed",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private String bookId;

    @NotNull(message = "Rating must not be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Schema(description = "Rating from 1 to 5", example = "4")
    private Integer rating;

    @NotBlank(message = "Review text must not be blank")
    @Size(min = 10, max = 100, message = "Review text must be between 10 and 100 characters")
    @Schema(description = "Detailed review text",
            example = "This book provides excellent insights into...")
    private String reviewText;
}
