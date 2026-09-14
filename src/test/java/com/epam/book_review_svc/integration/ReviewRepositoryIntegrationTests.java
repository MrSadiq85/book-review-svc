package com.epam.book_review_svc.integration;

import com.epam.book_review_svc.exception.DataAccessException;
import com.epam.book_review_svc.model.entity.Review;
import com.epam.book_review_svc.repository.ReviewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DisplayName("ReviewRepository Integration Tests")
class ReviewRepositoryIntegrationTests {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Review testReview;

    @BeforeEach
    void setUp() {
        testReview = new Review();
        testReview.setId("test-review-001");
        testReview.setBookId("550e8400-e29b-41d4-a716-446655440000");
        testReview.setUserId("user-123");
        testReview.setRating(4);
        testReview.setReviewText("This is a test review for integration testing.");
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Read All Reviews Tests")
    class ReadAllReviewsTests {

        @Test
        @DisplayName("Should read reviews from file when file exists")
        void testReadAllReviews_givenExistingFile_thenReturnReviews() {
            List<Review> reviews = reviewRepository.readAllReviews();

            assertThat(reviews).isNotNull();
            assertThat(reviews).isInstanceOf(List.class);
        }

        @Test
        @DisplayName("Should return empty list when no reviews exist")
        void testReadAllReviews_givenEmptyRepository_thenReturnEmptyList() {
            List<Review> reviews = reviewRepository.readAllReviews();

            assertThat(reviews).isNotNull();
            assertThat(reviews).isEmpty();
        }

        @Test
        @DisplayName("Should preserve review data after reading")
        void testReadAllReviews_givenWrittenReviews_thenPreserveData() {
            List<Review> reviewsToWrite = new ArrayList<>();
            reviewsToWrite.add(testReview);

            reviewRepository.writeReviews(reviewsToWrite);

            List<Review> readReviews = reviewRepository.readAllReviews();

            assertThat(readReviews).hasSize(1);
            assertThat(readReviews.get(0).getId()).isEqualTo(testReview.getId());
            assertThat(readReviews.get(0).getBookId()).isEqualTo(testReview.getBookId());
            assertThat(readReviews.get(0).getUserId()).isEqualTo(testReview.getUserId());
            assertThat(readReviews.get(0).getRating()).isEqualTo(testReview.getRating());
        }
    }

    @Nested
    @DisplayName("Write Reviews Tests")
    class WriteReviewsTests {

        @Test
        @DisplayName("Should write single review to file")
        void testWriteReviews_givenSingleReview_thenWriteSuccessfully() {
            List<Review> reviewsToWrite = new ArrayList<>();
            reviewsToWrite.add(testReview);

            reviewRepository.writeReviews(reviewsToWrite);

            List<Review> readReviews = reviewRepository.readAllReviews();

            assertThat(readReviews).hasSize(1);
            assertThat(readReviews.get(0).getId()).isEqualTo(testReview.getId());
        }

        @Test
        @DisplayName("Should write multiple reviews to file")
        void testWriteReviews_givenMultipleReviews_thenWriteAllSuccessfully() {
            Review review2 = new Review();
            review2.setId("test-review-002");
            review2.setBookId("550e8400-e29b-41d4-a716-446655440001");
            review2.setUserId("user-456");
            review2.setRating(5);
            review2.setReviewText("Another test review for testing.");
            review2.setCreatedAt(LocalDateTime.now());
            review2.setUpdatedAt(LocalDateTime.now());

            List<Review> reviewsToWrite = new ArrayList<>();
            reviewsToWrite.add(testReview);
            reviewsToWrite.add(review2);

            reviewRepository.writeReviews(reviewsToWrite);

            List<Review> readReviews = reviewRepository.readAllReviews();

            assertThat(readReviews).hasSize(2);
            assertThat(readReviews).extracting(Review::getId)
                    .contains(testReview.getId(), review2.getId());
        }

        @Test
        @DisplayName("Should handle empty list write")
        void testWriteReviews_givenEmptyList_thenWriteSuccessfully() {
            reviewRepository.writeReviews(new ArrayList<>());

            List<Review> readReviews = reviewRepository.readAllReviews();

            assertThat(readReviews).isEmpty();
        }

        @Test
        @DisplayName("Should preserve all review fields when writing and reading")
        void testWriteAndReadReviews_givenCompleteReviewData_thenPreserveAllFields() {
            List<Review> reviewsToWrite = new ArrayList<>();
            reviewsToWrite.add(testReview);

            reviewRepository.writeReviews(reviewsToWrite);

            List<Review> readReviews = reviewRepository.readAllReviews();
            Review readReview = readReviews.get(0);

            assertThat(readReview.getId()).isEqualTo(testReview.getId());
            assertThat(readReview.getBookId()).isEqualTo(testReview.getBookId());
            assertThat(readReview.getUserId()).isEqualTo(testReview.getUserId());
            assertThat(readReview.getRating()).isEqualTo(testReview.getRating());
            assertThat(readReview.getReviewText()).isEqualTo(testReview.getReviewText());
            assertThat(readReview.getCreatedAt()).isNotNull();
            assertThat(readReview.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should overwrite previous reviews when writing new list")
        void testWriteReviews_givenNewData_thenOverwritePreviousData() {
            // Write first set of reviews
            List<Review> firstSet = new ArrayList<>();
            firstSet.add(testReview);
            reviewRepository.writeReviews(firstSet);

            // Write second set of reviews
            Review review2 = new Review();
            review2.setId("test-review-002");
            review2.setBookId("550e8400-e29b-41d4-a716-446655440001");
            review2.setUserId("user-456");
            review2.setRating(5);

            List<Review> secondSet = new ArrayList<>();
            secondSet.add(review2);
            reviewRepository.writeReviews(secondSet);

            List<Review> readReviews = reviewRepository.readAllReviews();

            assertThat(readReviews).hasSize(1);
            assertThat(readReviews.get(0).getId()).isEqualTo(review2.getId());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle review with null optional fields")
        void testWriteAndReadReviews_givenReviewWithNullFields_thenHandleGracefully() {
            Review reviewWithNulls = new Review();
            reviewWithNulls.setId("test-review-003");
            reviewWithNulls.setBookId("550e8400-e29b-41d4-a716-446655440000");
            reviewWithNulls.setUserId("user-789");
            reviewWithNulls.setRating(3);
            reviewWithNulls.setReviewText("Test");
            // createdAt and updatedAt may be null

            List<Review> reviewsToWrite = new ArrayList<>();
            reviewsToWrite.add(reviewWithNulls);

            reviewRepository.writeReviews(reviewsToWrite);

            List<Review> readReviews = reviewRepository.readAllReviews();

            assertThat(readReviews).hasSize(1);
            assertThat(readReviews.get(0).getId()).isEqualTo(reviewWithNulls.getId());
        }

        @Test
        @DisplayName("Should maintain insertion order when reading reviews")
        void testWriteAndReadReviews_givenOrderedReviews_thenMaintainOrder() {
            Review review1 = new Review();
            review1.setId("review-first");
            review1.setBookId("book-1");
            review1.setUserId("user-1");
            review1.setRating(1);

            Review review2 = new Review();
            review2.setId("review-second");
            review2.setBookId("book-2");
            review2.setUserId("user-2");
            review2.setRating(2);

            Review review3 = new Review();
            review3.setId("review-third");
            review3.setBookId("book-3");
            review3.setUserId("user-3");
            review3.setRating(3);

            List<Review> reviewsToWrite = new ArrayList<>();
            reviewsToWrite.add(review1);
            reviewsToWrite.add(review2);
            reviewsToWrite.add(review3);

            reviewRepository.writeReviews(reviewsToWrite);

            List<Review> readReviews = reviewRepository.readAllReviews();

            assertThat(readReviews).extracting(Review::getId)
                    .containsExactly("review-first", "review-second", "review-third");
        }
    }
}
