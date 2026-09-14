package com.epam.book_review_svc.integration;

import com.epam.book_review_svc.model.dto.request.CreateReviewRequest;
import com.epam.book_review_svc.model.dto.request.UpdateReviewRequest;
import com.epam.book_review_svc.model.dto.response.ReviewResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
//@AutoConfigureMockMvc
@DisplayName("ReviewController Integration Tests")
class ReviewControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CreateReviewRequest validCreateRequest;
    private UpdateReviewRequest validUpdateRequest;
    private String testBookId;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testBookId = "550e8400-e29b-41d4-a716-446655440000";
        testUserId = "user-123";

        validCreateRequest = new CreateReviewRequest();
        validCreateRequest.setBookId(testBookId);
        validCreateRequest.setRating(4);
        validCreateRequest.setReviewText("This is an excellent book with great insights.");

        validUpdateRequest = new UpdateReviewRequest();
        validUpdateRequest.setRating(5);
        validUpdateRequest.setReviewText("Updated review text with more detailed feedback.");
    }

    @Nested
    @DisplayName("Create Review Endpoint Tests")
    class CreateReviewEndpointTests {

        @Test
        @DisplayName("Should return 201 when creating review with valid request")
        void testCreateReview_givenValidRequest_thenReturn201() throws Exception {
            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.reviewId", notNullValue()))
                    .andExpect(jsonPath("$.bookId").value(testBookId))
                    .andExpect(jsonPath("$.userId").value(testUserId))
                    .andExpect(jsonPath("$.rating").value(4));
        }

        @Test
        @DisplayName("Should return 400 when book does not exist")
        void testCreateReview_givenNonExistentBook_thenReturn400() throws Exception {
            validCreateRequest.setBookId("nonexistent-book-id");

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when rating is below minimum")
        void testCreateReview_givenRatingBelowMinimum_thenReturn400() throws Exception {
            validCreateRequest.setRating(0);

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when rating is above maximum")
        void testCreateReview_givenRatingAboveMaximum_thenReturn400() throws Exception {
            validCreateRequest.setRating(6);

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when review text is too short")
        void testCreateReview_givenShortReviewText_thenReturn400() throws Exception {
            validCreateRequest.setReviewText("Short");

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when review text is too long")
        void testCreateReview_givenLongReviewText_thenReturn400() throws Exception {
            validCreateRequest.setReviewText("a".repeat(101));

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when bookId is blank")
        void testCreateReview_givenBlankBookId_thenReturn400() throws Exception {
            validCreateRequest.setBookId("");

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when rating is null")
        void testCreateReview_givenNullRating_thenReturn400() throws Exception {
            validCreateRequest.setRating(null);

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Review Endpoint Tests")
    class GetReviewEndpointTests {

        @Test
        @DisplayName("Should return 200 with review when getting existing review")
        void testGetReviewById_givenValidId_thenReturn200() throws Exception {
            // First create a review
            MvcResult createResult = mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ReviewResponse created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    ReviewResponse.class
            );

            // Now retrieve it
            mockMvc.perform(get("/api/reviews/" + created.getReviewId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.reviewId").value(created.getReviewId()))
                    .andExpect(jsonPath("$.bookId").value(testBookId))
                    .andExpect(jsonPath("$.rating").value(4));
        }

        @Test
        @DisplayName("Should return 404 when review does not exist")
        void testGetReviewById_givenNonExistentId_thenReturn404() throws Exception {
            mockMvc.perform(get("/api/reviews/nonexistent-review-id"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 200 with empty list when no reviews exist")
        void testGetAllReviews_givenEmptyRepository_thenReturn200WithEmptyList() throws Exception {
            mockMvc.perform(get("/api/reviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return 200 with all reviews")
        void testGetAllReviews_givenMultipleReviews_thenReturn200WithAllReviews() throws Exception {
            // Create first review
            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", "user-1")
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated());

            // Create second review with different rating
            CreateReviewRequest secondRequest = new CreateReviewRequest();
            secondRequest.setBookId(testBookId);
            secondRequest.setRating(5);
            secondRequest.setReviewText("Another excellent review about the book.");

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", "user-2")
                    .content(objectMapper.writeValueAsString(secondRequest)))
                    .andExpect(status().isCreated());

            // Get all reviews
            mockMvc.perform(get("/api/reviews"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("Should filter reviews by book ID")
        void testGetAllReviews_givenBookIdFilter_thenReturnFilteredReviews() throws Exception {
            // Create review for first book
            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", "user-1")
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated());

            // Get reviews filtered by book ID
            mockMvc.perform(get("/api/reviews?bookId=" + testBookId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].bookId").value(testBookId));
        }

        @Test
        @DisplayName("Should return empty list when filtering by non-existent book ID")
        void testGetAllReviews_givenNonExistentBookIdFilter_thenReturnEmptyList() throws Exception {
            mockMvc.perform(get("/api/reviews?bookId=nonexistent-book"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("Update Review Endpoint Tests")
    class UpdateReviewEndpointTests {

        @Test
        @DisplayName("Should return 200 when updating review by owner")
        void testUpdateReview_givenValidRequestByOwner_thenReturn200() throws Exception {
            // Create a review
            MvcResult createResult = mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ReviewResponse created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    ReviewResponse.class
            );

            // Update the review
            mockMvc.perform(put("/api/reviews/" + created.getReviewId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.reviewText").value("Updated review text with more detailed feedback."));
        }

        @Test
        @DisplayName("Should return 403 when non-owner attempts update")
        void testUpdateReview_givenDifferentUser_thenReturn403() throws Exception {
            // Create a review
            MvcResult createResult = mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ReviewResponse created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    ReviewResponse.class
            );

            // Try to update with different user
            mockMvc.perform(put("/api/reviews/" + created.getReviewId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", "other-user")
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when review does not exist")
        void testUpdateReview_givenNonExistentReview_thenReturn404() throws Exception {
            mockMvc.perform(put("/api/reviews/nonexistent-id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when rating is invalid")
        void testUpdateReview_givenInvalidRating_thenReturn400() throws Exception {
            // Create a review
            MvcResult createResult = mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ReviewResponse created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    ReviewResponse.class
            );

            // Try to update with invalid rating
            validUpdateRequest.setRating(10);
            mockMvc.perform(put("/api/reviews/" + created.getReviewId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validUpdateRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Delete Review Endpoint Tests")
    class DeleteReviewEndpointTests {

        @Test
        @DisplayName("Should return 204 when deleting review by owner")
        void testDeleteReview_givenValidIdAndOwner_thenReturn204() throws Exception {
            // Create a review
            MvcResult createResult = mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ReviewResponse created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    ReviewResponse.class
            );

            // Delete the review
            mockMvc.perform(delete("/api/reviews/" + created.getReviewId())
                    .header("X-User-Id", testUserId))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 403 when non-owner attempts delete")
        void testDeleteReview_givenDifferentUser_thenReturn403() throws Exception {
            // Create a review
            MvcResult createResult = mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ReviewResponse created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    ReviewResponse.class
            );

            // Try to delete with different user
            mockMvc.perform(delete("/api/reviews/" + created.getReviewId())
                    .header("X-User-Id", "other-user"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when review does not exist")
        void testDeleteReview_givenNonExistentId_thenReturn404() throws Exception {
            mockMvc.perform(delete("/api/reviews/nonexistent-id")
                    .header("X-User-Id", testUserId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should remove review from database after successful delete")
        void testDeleteReview_givenValidRequest_thenRemoveFromDatabase() throws Exception {
            // Create a review
            MvcResult createResult = mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-User-Id", testUserId)
                    .content(objectMapper.writeValueAsString(validCreateRequest)))
                    .andExpect(status().isCreated())
                    .andReturn();

            ReviewResponse created = objectMapper.readValue(
                    createResult.getResponse().getContentAsString(),
                    ReviewResponse.class
            );

            // Delete the review
            mockMvc.perform(delete("/api/reviews/" + created.getReviewId())
                    .header("X-User-Id", testUserId))
                    .andExpect(status().isNoContent());

            // Verify review is deleted by trying to get it
            mockMvc.perform(get("/api/reviews/" + created.getReviewId()))
                    .andExpect(status().isNotFound());
        }
    }
}
