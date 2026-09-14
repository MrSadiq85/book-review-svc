package com.epam.book_review_svc.controller;

import com.epam.book_review_svc.model.dto.request.CreateReviewRequest;
import com.epam.book_review_svc.model.dto.request.UpdateReviewRequest;
import com.epam.book_review_svc.model.dto.response.ReviewResponse;
import com.epam.book_review_svc.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Book review management APIs")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @Operation(summary = "Create a new review",
            description = "Add a new book review with validation")
    @ApiResponse(responseCode = "201", description = "Review created successfully",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - validation failed or book not found")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader(value = "X-User-Id") String userId
    ) {
        ReviewResponse response = reviewService.createReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{reviewId}")
    @Operation(summary = "Update an existing review",
            description = "Modify review details by ID (owner-only)")
    @ApiResponse(responseCode = "200", description = "Review updated successfully",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class)))
    @ApiResponse(responseCode = "400", description = "Bad Request - validation failed")
    @ApiResponse(responseCode = "403", description = "Forbidden - not the review owner")
    @ApiResponse(responseCode = "404", description = "Not Found - review does not exist")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody UpdateReviewRequest request,
            @RequestHeader(value = "X-User-Id") String userId
    ) {
        ReviewResponse response = reviewService.updateReview(reviewId, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review",
            description = "Remove a review from the system (owner-only)")
    @ApiResponse(responseCode = "204", description = "Review deleted successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - not the review owner")
    @ApiResponse(responseCode = "404", description = "Not Found - review does not exist")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<Void> deleteReview(
            @PathVariable String reviewId,
            @RequestHeader(value = "X-User-Id") String userId
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{reviewId}")
    @Operation(summary = "Get review by ID",
            description = "Retrieve a specific review by its ID")
    @ApiResponse(responseCode = "200", description = "Review found",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class)))
    @ApiResponse(responseCode = "404", description = "Not Found - review does not exist")
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<ReviewResponse> getReviewById(
            @PathVariable String reviewId
    ) {
        ReviewResponse response = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all reviews",
            description = "List all reviews in the system with optional filtering by book ID")
    @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully",
            content = @Content(schema = @Schema(implementation = ReviewResponse.class)))
    @ApiResponse(responseCode = "500", description = "Internal Server Error")
    public ResponseEntity<List<ReviewResponse>> getAllReviews(
            @RequestParam(value = "bookId", required = false) String bookId
    ) {
        List<ReviewResponse> reviews;
        if (bookId != null && !bookId.isBlank()) {
            reviews = reviewService.getReviewsByBookId(bookId);
        } else {
            reviews = reviewService.getAllReviews();
        }
        return ResponseEntity.ok(reviews);
    }
}
