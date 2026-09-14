package com.epam.book_review_svc.unit;

import com.epam.book_review_svc.model.dto.CreateBookRequest;
import com.epam.book_review_svc.model.dto.FieldError;
import com.epam.book_review_svc.model.dto.UpdateBookRequest;
import com.epam.book_review_svc.service.BookValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive validator tests focusing on edge cases and ISBN validation.
 * Tests ISBN validation with various formats and edge cases.
 */
@DisplayName("BookValidator Edge Cases Tests")
public class BookValidatorEdgeCasesTests {

    private BookValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new BookValidator();
    }

    @Nested
    @DisplayName("ISBN Validation Edge Cases")
    class ISBNValidationEdgeCases {

        @Test
        @DisplayName("Should accept valid ISBN-10 format")
        void testValidateISBN_givenValidISBN10_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "0143127993",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).isEmpty();
        }

        @Test
        @DisplayName("Should accept valid ISBN-13 format")
        void testValidateISBN_givenValidISBN13_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).isEmpty();
        }

        @Test
        @DisplayName("Should accept ISBN-10 with hyphens")
        void testValidateISBN_givenISBN10WithHyphens_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "0-143-12799-3",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).isEmpty();
        }

        @Test
        @DisplayName("Should accept ISBN-13 with hyphens")
        void testValidateISBN_givenISBN13WithHyphens_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "978-0-143-127-99-4",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).isEmpty();
        }

        @Test
        @DisplayName("Should reject ISBN with only 9 digits")
        void testValidateISBN_givenISBN9Digits_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "014312799",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1)
                    .extracting(FieldError::getMessage)
                    .contains("Invalid ISBN format. Expected ISBN-10 (10 digits) or ISBN-13 (13 digits)");
        }

        @Test
        @DisplayName("Should reject ISBN with 11 digits")
        void testValidateISBN_givenISBN11Digits_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "01431279930",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject ISBN with 12 digits")
        void testValidateISBN_givenISBN12Digits_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "978014312799",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject ISBN with 14 digits")
        void testValidateISBN_givenISBN14Digits_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "97801431279940",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject ISBN with non-numeric characters after hyphen removal")
        void testValidateISBN_givenISBNWithLetters_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "978-X14-3127-99-4",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject null ISBN")
        void testValidateISBN_givenNullISBN_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    null,
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1)
                    .extracting(FieldError::getMessage)
                    .contains("ISBN is required");
        }

        @Test
        @DisplayName("Should reject empty ISBN")
        void testValidateISBN_givenEmptyISBN_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject ISBN with only spaces")
        void testValidateISBN_givenWhitespaceISBN_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "   ",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should accept ISBN-10 with leading zeros")
        void testValidateISBN_givenISBN10WithLeadingZeros_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "0012345678",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Title Validation Edge Cases")
    class TitleValidationEdgeCases {

        @Test
        @DisplayName("Should accept title with minimum length (1 character)")
        void testValidateTitle_givenMinLengthTitle_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "A",
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("title")).isEmpty();
        }

        @Test
        @DisplayName("Should accept title with maximum length (500 characters)")
        void testValidateTitle_givenMaxLengthTitle_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "A".repeat(500),
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("title")).isEmpty();
        }

        @Test
        @DisplayName("Should reject title exceeding maximum length")
        void testValidateTitle_givenExceedingLengthTitle_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "A".repeat(501),
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("title"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject null title")
        void testValidateTitle_givenNullTitle_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    null,
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("title"))
                    .hasSize(1)
                    .extracting(FieldError::getMessage)
                    .contains("Title is required");
        }

        @Test
        @DisplayName("Should reject empty title")
        void testValidateTitle_givenEmptyTitle_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "",
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("title"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject title with only whitespace")
        void testValidateTitle_givenWhitespaceTitle_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "   ",
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("title"))
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("Author Validation Edge Cases")
    class AuthorValidationEdgeCases {

        @Test
        @DisplayName("Should accept author with minimum length (1 character)")
        void testValidateAuthor_givenMinLengthAuthor_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "A",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("author")).isEmpty();
        }

        @Test
        @DisplayName("Should accept author with maximum length (200 characters)")
        void testValidateAuthor_givenMaxLengthAuthor_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "A".repeat(200),
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("author")).isEmpty();
        }

        @Test
        @DisplayName("Should reject author exceeding maximum length")
        void testValidateAuthor_givenExceedingLengthAuthor_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "A".repeat(201),
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("author"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject null author")
        void testValidateAuthor_givenNullAuthor_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    null,
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("author"))
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("Publication Date Validation Edge Cases")
    class PublicationDateValidationEdgeCases {

        @Test
        @DisplayName("Should accept publication date equal to today")
        void testValidatePublicationDate_givenTodayDate_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.now()
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("publicationDate")).isEmpty();
        }

        @Test
        @DisplayName("Should accept old publication dates")
        void testValidatePublicationDate_givenOldDate_thenNoErrors() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.of(1900, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("publicationDate")).isEmpty();
        }

        @Test
        @DisplayName("Should reject future publication date")
        void testValidatePublicationDate_givenFutureDate_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Author",
                    "9780143127994",
                    "Fiction",
                    LocalDate.now().plusDays(1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("publicationDate"))
                    .hasSize(1)
                    .extracting(FieldError::getMessage)
                    .contains("Publication date cannot be in the future");
        }

        @Test
        @DisplayName("Should reject null publication date")
        void testValidatePublicationDate_givenNullDate_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Author",
                    "9780143127994",
                    "Fiction",
                    null
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("publicationDate"))
                    .hasSize(1)
                    .extracting(FieldError::getMessage)
                    .contains("Publication date is required");
        }
    }

    @Nested
    @DisplayName("Genre Validation Edge Cases")
    class GenreValidationEdgeCases {

        @Test
        @DisplayName("Should accept all valid genres")
        void testValidateGenre_givenAllValidGenres_thenNoErrors() {
            String[] validGenres = {
                    "Fiction", "Non-Fiction", "Mystery", "Romance", "Science Fiction",
                    "Fantasy", "Biography", "History", "Self-Help", "Children", "Young Adult"
            };

            for (String genre : validGenres) {
                CreateBookRequest request = new CreateBookRequest(
                        "Test Book",
                        "Author",
                        "9780143127994",
                        genre,
                        LocalDate.of(2000, 1, 1)
                );

                List<FieldError> errors = validator.validateCreateBookRequest(request);

                assertThat(errors).filteredOn(e -> e.getField().equals("genre"))
                        .as("Genre: " + genre)
                        .isEmpty();
            }
        }

        @Test
        @DisplayName("Should reject invalid genre")
        void testValidateGenre_givenInvalidGenre_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Author",
                    "9780143127994",
                    "InvalidGenre",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("genre"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should reject null genre")
        void testValidateGenre_givenNullGenre_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Author",
                    "9780143127994",
                    null,
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("genre"))
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should be case-sensitive for genre matching")
        void testValidateGenre_givenLowercaseGenre_thenError() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Author",
                    "9780143127994",
                    "fiction",
                    LocalDate.of(2000, 1, 1)
            );

            List<FieldError> errors = validator.validateCreateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("genre"))
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("Update Request Validation Edge Cases")
    class UpdateRequestValidationEdgeCases {

        @Test
        @DisplayName("Should allow all fields null in update request")
        void testValidateUpdateRequest_givenAllFieldsNull_thenNoErrors() {
            UpdateBookRequest request = new UpdateBookRequest(
                    null,
                    null,
                    null,
                    null,
                    null
            );

            List<FieldError> errors = validator.validateUpdateBookRequest(request);

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("Should validate only provided fields in update request")
        void testValidateUpdateRequest_givenPartialFields_thenValidateOnlyProvided() {
            UpdateBookRequest request = new UpdateBookRequest(
                    "New Title",
                    null,
                    null,
                    null,
                    null
            );

            List<FieldError> errors = validator.validateUpdateBookRequest(request);

            assertThat(errors).isEmpty();
        }

        @Test
        @DisplayName("Should reject invalid ISBN in update request")
        void testValidateUpdateRequest_givenInvalidISBN_thenError() {
            UpdateBookRequest request = new UpdateBookRequest(
                    null,
                    null,
                    "invalid-isbn",
                    null,
                    null
            );

            List<FieldError> errors = validator.validateUpdateBookRequest(request);

            assertThat(errors).filteredOn(e -> e.getField().equals("ISBN"))
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("Null Request Validation")
    class NullRequestValidation {

        @Test
        @DisplayName("Should handle null CreateBookRequest")
        void testValidateCreateBookRequest_givenNullRequest_thenError() {
            List<FieldError> errors = validator.validateCreateBookRequest(null);

            assertThat(errors)
                    .hasSize(1)
                    .extracting(FieldError::getField)
                    .contains("request");
        }

        @Test
        @DisplayName("Should handle null UpdateBookRequest")
        void testValidateUpdateBookRequest_givenNullRequest_thenError() {
            List<FieldError> errors = validator.validateUpdateBookRequest(null);

            assertThat(errors)
                    .hasSize(1)
                    .extracting(FieldError::getField)
                    .contains("request");
        }
    }
}
