package com.epam.book_review_svc.service;

import com.epam.book_review_svc.exception.BookNotFoundException;
import com.epam.book_review_svc.exception.InvalidRatingException;
import com.epam.book_review_svc.exception.ReviewNotFoundException;
import com.epam.book_review_svc.model.dto.request.CreateReviewRequest;
import com.epam.book_review_svc.model.dto.request.UpdateReviewRequest;
import com.epam.book_review_svc.model.dto.response.ReviewResponse;
import com.epam.book_review_svc.model.entity.Review;
import com.epam.book_review_svc.repository.ReviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReviewService {
    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private final ReviewRepository reviewRepository;
    private final BookValidationService bookValidationService;

    public ReviewService(ReviewRepository reviewRepository,
                        BookValidationService bookValidationService) {
        this.reviewRepository = reviewRepository;
        this.bookValidationService = bookValidationService;
    }

    public ReviewResponse createReview(CreateReviewRequest request, String userId) {
        log.debug("Creating review for book: {}, user: {}", request.getBookId(), userId);

        // Validate book exists
        if (!bookValidationService.isBookValid(request.getBookId())) {
            throw new BookNotFoundException("Referenced book with ID " + request.getBookId() + " does not exist");
        }

        // Validate rating
        validateRating(request.getRating());

        // Create new review entity
        Review review = new Review();
        review.setId(UUID.randomUUID().toString());
        review.setBookId(request.getBookId());
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        LocalDateTime now = LocalDateTime.now();
        review.setCreatedAt(now);
        review.setUpdatedAt(now);

        // Persist to repository
        List<Review> reviews = reviewRepository.readAllReviews();
        reviews.add(review);
        reviewRepository.writeReviews(reviews);

        log.info("Review created with ID: {}", review.getId());
        return mapToResponse(review);
    }

    public ReviewResponse updateReview(String id, UpdateReviewRequest request, String userId) {
        log.debug("Updating review: {} by user: {}", id, userId);

        // Find existing review
        Review review = findReviewById(id);

        // Check ownership
        if (!review.getUserId().equals(userId)) {
            throw new com.epam.book_review_svc.exception.ForbiddenException(
                    "You are not authorized to modify this review (owner-only operation)");
        }

        // Validate rating
        validateRating(request.getRating());

        // Update fields
        review.setRating(request.getRating());
        review.setReviewText(request.getReviewText());
        review.setUpdatedAt(LocalDateTime.now());

        // Persist updated review
        List<Review> reviews = reviewRepository.readAllReviews();
        int index = reviews.stream()
                .map(Review::getId)
                .collect(Collectors.toList())
                .indexOf(id);
        reviews.set(index, review);
        reviewRepository.writeReviews(reviews);

        log.info("Review updated: {}", id);
        return mapToResponse(review);
    }

    public void deleteReview(String id, String userId) {
        log.debug("Deleting review: {} by user: {}", id, userId);

        // Verify review exists
        Review review = findReviewById(id);

        // Check ownership
        if (!review.getUserId().equals(userId)) {
            throw new com.epam.book_review_svc.exception.ForbiddenException(
                    "You are not authorized to modify this review (owner-only operation)");
        }

        // Remove from list
        List<Review> reviews = reviewRepository.readAllReviews();
        boolean removed = reviews.removeIf(r -> r.getId().equals(id));

        if (!removed) {
            throw new ReviewNotFoundException("Review with ID " + id + " not found");
        }

        // Persist updated list
        reviewRepository.writeReviews(reviews);

        log.info("Review deleted: {}", id);
    }

    public ReviewResponse getReviewById(String id) {
        log.debug("Retrieving review: {}", id);
        Review review = findReviewById(id);
        return mapToResponse(review);
    }

    public List<ReviewResponse> getAllReviews() {
        log.debug("Retrieving all reviews");
        List<Review> reviews = reviewRepository.readAllReviews();
        return reviews.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ReviewResponse> getReviewsByBookId(String bookId) {
        log.debug("Retrieving reviews for book: {}", bookId);
        List<Review> reviews = reviewRepository.readAllReviews();
        return reviews.stream()
                .filter(r -> r.getBookId().equals(bookId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Review findReviewById(String id) {
        return reviewRepository.readAllReviews().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ReviewNotFoundException("Review with ID " + id + " not found"));
    }

    private void validateRating(Integer rating) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new InvalidRatingException("Rating must be between 1 and 5");
        }
    }

    private ReviewResponse mapToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getId());
        response.setBookId(review.getBookId());
        response.setUserId(review.getUserId());
        response.setRating(review.getRating());
        response.setReviewText(review.getReviewText());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        return response;
    }
}
