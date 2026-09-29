package com.epam.book_review_svc.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for a review")
public class ReviewResponse {

    @Schema(description = "Review ID (UUID)",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String reviewId;

    @Schema(description = "Book ID (UUID)",
            example = "123e4567-e89b-12d3-a456-426614174000")
    private String bookId;

    @Schema(description = "User ID",
            example = "user-123")
    private String userId;

    @Schema(description = "Rating (1-5)", example = "4")
    private Integer rating;

    @Schema(description = "Review text",
            example = "An excellent book with deep insights...")
    private String reviewText;

    @Schema(description = "Review creation timestamp",
            example = "2025-01-10T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Review last update timestamp",
            example = "2025-01-10T14:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
