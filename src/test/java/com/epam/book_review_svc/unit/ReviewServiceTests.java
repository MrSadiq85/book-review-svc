package com.epam.book_review_svc.unit;

import com.epam.book_review_svc.exception.BookNotFoundException;
import com.epam.book_review_svc.exception.ForbiddenException;
import com.epam.book_review_svc.exception.InvalidRatingException;
import com.epam.book_review_svc.exception.ReviewNotFoundException;
import com.epam.book_review_svc.model.dto.request.CreateReviewRequest;
import com.epam.book_review_svc.model.dto.request.UpdateReviewRequest;
import com.epam.book_review_svc.model.dto.response.ReviewResponse;
import com.epam.book_review_svc.model.entity.Review;
import com.epam.book_review_svc.repository.ReviewRepository;
import com.epam.book_review_svc.service.BookValidationService;
import com.epam.book_review_svc.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTests {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BookValidationService bookValidationService;

    @InjectMocks
    private ReviewService reviewService;

    private CreateReviewRequest validCreateRequest;
    private UpdateReviewRequest validUpdateRequest;
    private Review testReview;
    private String testBookId;
    private String testUserId;
    private String testReviewId;

    @BeforeEach
    void setUp() {
        testBookId = "550e8400-e29b-41d4-a716-446655440000";
        testUserId = "user-123";
        testReviewId = "review-001";

        validCreateRequest = new CreateReviewRequest();
        validCreateRequest.setBookId(testBookId);
        validCreateRequest.setRating(4);
        validCreateRequest.setReviewText("This is a great book with excellent content.");

        validUpdateRequest = new UpdateReviewRequest();
        validUpdateRequest.setRating(5);
        validUpdateRequest.setReviewText("Updated review text for the book here.");

        testReview = new Review();
        testReview.setId(testReviewId);
        testReview.setBookId(testBookId);
        testReview.setUserId(testUserId);
        testReview.setRating(4);
        testReview.setReviewText("This is a great book with excellent content.");
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Create Review Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review with valid request")
        void testCreateReview_givenValidRequest_thenCreateSuccessfully() {
            when(bookValidationService.isBookValid(testBookId)).thenReturn(true);
            when(reviewRepository.readAllReviews()).thenReturn(new ArrayList<>());
            //when(reviewRepository.writeReviews(anyList())).thenReturn(null);

            ReviewResponse response = reviewService.createReview(validCreateRequest, testUserId);

            assertThat(response).isNotNull();
            assertThat(response.getBookId()).isEqualTo(testBookId);
            assertThat(response.getUserId()).isEqualTo(testUserId);
            assertThat(response.getRating()).isEqualTo(4);
            assertThat(response.getReviewText()).isEqualTo("This is a great book with excellent content.");
            verify(reviewRepository, times(1)).writeReviews(anyList());
        }

        @Test
        @DisplayName("Should throw BookNotFoundException when book does not exist")
        void testCreateReview_givenNonExistentBook_thenThrowBookNotFoundException() {
            when(bookValidationService.isBookValid(testBookId)).thenReturn(false);

            assertThatThrownBy(() -> reviewService.createReview(validCreateRequest, testUserId))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("does not exist");
        }

        @Test
        @DisplayName("Should throw InvalidRatingException when rating is below minimum")
        void testCreateReview_givenRatingBelowMinimum_thenThrowInvalidRatingException() {
            validCreateRequest.setRating(0);
            when(bookValidationService.isBookValid(testBookId)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(validCreateRequest, testUserId))
                    .isInstanceOf(InvalidRatingException.class)
                    .hasMessageContaining("between 1 and 5");
        }

        @Test
        @DisplayName("Should throw InvalidRatingException when rating is above maximum")
        void testCreateReview_givenRatingAboveMaximum_thenThrowInvalidRatingException() {
            validCreateRequest.setRating(6);
            when(bookValidationService.isBookValid(testBookId)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(validCreateRequest, testUserId))
                    .isInstanceOf(InvalidRatingException.class)
                    .hasMessageContaining("between 1 and 5");
        }

        @Test
        @DisplayName("Should throw InvalidRatingException when rating is null")
        void testCreateReview_givenNullRating_thenThrowInvalidRatingException() {
            validCreateRequest.setRating(null);
            when(bookValidationService.isBookValid(testBookId)).thenReturn(true);

            assertThatThrownBy(() -> reviewService.createReview(validCreateRequest, testUserId))
                    .isInstanceOf(InvalidRatingException.class);
        }

        @Test
        @DisplayName("Should append review to existing reviews list")
        void testCreateReview_givenExistingReviews_thenAppendToList() {
            List<Review> existingReviews = new ArrayList<>();
            existingReviews.add(testReview);

            when(bookValidationService.isBookValid(testBookId)).thenReturn(true);
            when(reviewRepository.readAllReviews()).thenReturn(existingReviews);

            ReviewResponse response = reviewService.createReview(validCreateRequest, testUserId);

            assertThat(response).isNotNull();
            verify(reviewRepository, times(1)).writeReviews(anyList());
        }
    }

    @Nested
    @DisplayName("Update Review Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update review with valid request by owner")
        void testUpdateReview_givenValidRequestByOwner_thenUpdateSuccessfully() {
            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            ReviewResponse response = reviewService.updateReview(testReviewId, validUpdateRequest, testUserId);

            assertThat(response).isNotNull();
            assertThat(response.getRating()).isEqualTo(5);
            assertThat(response.getReviewText()).isEqualTo("Updated review text for the book here.");
            verify(reviewRepository, times(1)).writeReviews(anyList());
        }

        @Test
        @DisplayName("Should throw ReviewNotFoundException when review does not exist")
        void testUpdateReview_givenNonExistentReview_thenThrowReviewNotFoundException() {
            when(reviewRepository.readAllReviews()).thenReturn(new ArrayList<>());

            assertThatThrownBy(() -> reviewService.updateReview("nonexistent-id", validUpdateRequest, testUserId))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-owner attempts update")
        void testUpdateReview_givenDifferentUser_thenThrowForbiddenException() {
            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            assertThatThrownBy(() -> reviewService.updateReview(testReviewId, validUpdateRequest, "other-user"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("Should throw InvalidRatingException when rating is invalid")
        void testUpdateReview_givenInvalidRating_thenThrowInvalidRatingException() {
            validUpdateRequest.setRating(10);
            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            assertThatThrownBy(() -> reviewService.updateReview(testReviewId, validUpdateRequest, testUserId))
                    .isInstanceOf(InvalidRatingException.class);
        }

        @Test
        @DisplayName("Should update only rating when review text is unchanged")
        void testUpdateReview_givenOnlyRatingChange_thenUpdateRatingOnly() {
            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);
            validUpdateRequest.setReviewText("This is a great book with excellent content.");

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            ReviewResponse response = reviewService.updateReview(testReviewId, validUpdateRequest, testUserId);

            assertThat(response.getRating()).isEqualTo(5);
            verify(reviewRepository, times(1)).writeReviews(anyList());
        }
    }

    @Nested
    @DisplayName("Delete Review Tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Should delete review by owner")
        void testDeleteReview_givenValidIdAndOwner_thenDeleteSuccessfully() {
            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            reviewService.deleteReview(testReviewId, testUserId);

            verify(reviewRepository, times(1)).writeReviews(anyList());
        }

        @Test
        @DisplayName("Should throw ReviewNotFoundException when review does not exist")
        void testDeleteReview_givenNonExistentReview_thenThrowReviewNotFoundException() {
            when(reviewRepository.readAllReviews()).thenReturn(new ArrayList<>());

            assertThatThrownBy(() -> reviewService.deleteReview("nonexistent-id", testUserId))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-owner attempts delete")
        void testDeleteReview_givenDifferentUser_thenThrowForbiddenException() {
            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            assertThatThrownBy(() -> reviewService.deleteReview(testReviewId, "other-user"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("not authorized");
        }

        @Test
        @DisplayName("Should remove review from list on successful delete")
        void testDeleteReview_givenValidRequest_thenRemoveFromRepository() {
            Review review1 = new Review();
            review1.setId("review-001");
            review1.setUserId("user-123");
            review1.setBookId(testBookId);

            Review review2 = new Review();
            review2.setId("review-002");
            review2.setUserId("user-456");
            review2.setBookId(testBookId);

            List<Review> reviews = new ArrayList<>();
            reviews.add(review1);
            reviews.add(review2);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            reviewService.deleteReview("review-001", "user-123");

            verify(reviewRepository, times(1)).writeReviews(anyList());
        }
    }

    @Nested
    @DisplayName("Get Review Tests")
    class GetReviewTests {

        @Test
        @DisplayName("Should retrieve review by ID")
        void testGetReviewById_givenValidId_thenReturnReview() {
            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            ReviewResponse response = reviewService.getReviewById(testReviewId);

            assertThat(response).isNotNull();
            assertThat(response.getReviewId()).isEqualTo(testReviewId);
            assertThat(response.getBookId()).isEqualTo(testBookId);
            assertThat(response.getUserId()).isEqualTo(testUserId);
        }

        @Test
        @DisplayName("Should throw ReviewNotFoundException when review not found by ID")
        void testGetReviewById_givenNonExistentId_thenThrowReviewNotFoundException() {
            when(reviewRepository.readAllReviews()).thenReturn(new ArrayList<>());

            assertThatThrownBy(() -> reviewService.getReviewById("nonexistent-id"))
                    .isInstanceOf(ReviewNotFoundException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Should retrieve all reviews when database is not empty")
        void testGetAllReviews_givenMultipleReviews_thenReturnAllReviews() {
            Review review2 = new Review();
            review2.setId("review-002");
            review2.setBookId(testBookId);
            review2.setUserId("user-456");
            review2.setRating(5);

            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);
            reviews.add(review2);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            List<ReviewResponse> responses = reviewService.getAllReviews();

            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getReviewId()).isEqualTo(testReviewId);
            assertThat(responses.get(1).getReviewId()).isEqualTo("review-002");
        }

        @Test
        @DisplayName("Should return empty list when no reviews exist")
        void testGetAllReviews_givenEmptyRepository_thenReturnEmptyList() {
            when(reviewRepository.readAllReviews()).thenReturn(new ArrayList<>());

            List<ReviewResponse> responses = reviewService.getAllReviews();

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should retrieve reviews filtered by book ID")
        void testGetReviewsByBookId_givenValidBookId_thenReturnFilteredReviews() {
            Review review2 = new Review();
            review2.setId("review-002");
            review2.setBookId("550e8400-e29b-41d4-a716-446655440001");
            review2.setUserId("user-456");
            review2.setRating(3);

            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);
            reviews.add(review2);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            List<ReviewResponse> responses = reviewService.getReviewsByBookId(testBookId);

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getBookId()).isEqualTo(testBookId);
        }

        @Test
        @DisplayName("Should return empty list when no reviews for given book ID")
        void testGetReviewsByBookId_givenNonExistentBookId_thenReturnEmptyList() {
            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            List<ReviewResponse> responses = reviewService.getReviewsByBookId("nonexistent-book-id");

            assertThat(responses).isEmpty();
        }

        @Test
        @DisplayName("Should return all reviews for book when multiple reviews exist")
        void testGetReviewsByBookId_givenMultipleReviewsForSameBook_thenReturnAll() {
            Review review2 = new Review();
            review2.setId("review-002");
            review2.setBookId(testBookId);
            review2.setUserId("user-456");
            review2.setRating(5);

            List<Review> reviews = new ArrayList<>();
            reviews.add(testReview);
            reviews.add(review2);

            when(reviewRepository.readAllReviews()).thenReturn(reviews);

            List<ReviewResponse> responses = reviewService.getReviewsByBookId(testBookId);

            assertThat(responses).hasSize(2);
            assertThat(responses).allMatch(r -> r.getBookId().equals(testBookId));
        }
    }
}
