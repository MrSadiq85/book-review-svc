package com.epam.book_review_svc.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Error response object")
public class ErrorResponse {

    @Schema(description = "HTTP status code", example = "404")
    private int status;

    @Schema(description = "Error message",
            example = "Review not found")
    private String message;

    @Schema(description = "Error type",
            example = "ReviewNotFoundException")
    private String errorType;

    @Schema(description = "Timestamp of error",
            example = "2025-01-10T14:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Request path where error occurred",
            example = "/api/reviews/123")
    private String path;
}
