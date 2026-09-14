package com.epam.book_review_svc.integration;

import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.model.dto.BookResponse;
import com.epam.book_review_svc.model.dto.CreateBookRequest;
import com.epam.book_review_svc.model.dto.PagedResponse;
import com.epam.book_review_svc.model.dto.UpdateBookRequest;
import com.epam.book_review_svc.service.BookService;
import com.epam.book_review_svc.service.BookValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for BookService and BookValidator with Spring context.
 * Tests the complete service layer with Spring dependency injection.
 */
@SpringBootTest
@DisplayName("BookService Spring Integration Tests")
public class BookServiceSpringIntegrationTests {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookValidator bookValidator;

    private CreateBookRequest validCreateRequest;

    @BeforeEach
    public void setUp() {
        validCreateRequest = new CreateBookRequest(
                "Test Book " + System.nanoTime(),
                "Test Author",
                "978-0" + System.nanoTime() % 1000000000,
                "Fiction",
                LocalDate.of(1925, 4, 10)
        );
    }

    @Nested
    @DisplayName("Create Book Integration Tests")
    class CreateBookIntegrationTests {

        @Test
        @DisplayName("Should create book with valid request and user")
        void testCreateBook_givenValidRequest_thenSuccess() {
            BookResponse response = bookService.createBook(validCreateRequest, "testUser");

            assertThat(response).isNotNull();
            assertThat(response.getId()).isNotNull();
            assertThat(response.getTitle()).isEqualTo(validCreateRequest.getTitle());
            assertThat(response.getAuthor()).isEqualTo(validCreateRequest.getAuthor());
            assertThat(response.getISBN()).isEqualTo(validCreateRequest.getISBN());
            assertThat(response.getCreatedBy()).isEqualTo("testUser");
        }

        @Test
        @DisplayName("Should persist book to storage and retrieve it")
        void testCreateBook_thenRetrieveCreatedBook() {
            BookResponse created = bookService.createBook(validCreateRequest, "testUser");

            BookResponse retrieved = bookService.getBook(created.getId());

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getId()).isEqualTo(created.getId());
            assertThat(retrieved.getTitle()).isEqualTo(created.getTitle());
        }
    }

    @Nested
    @DisplayName("List Books Integration Tests")
    class ListBooksIntegrationTests {

        @Test
        @DisplayName("Should list books with pagination")
        void testListBooks_givenValidParams_thenReturnPaginatedList() {
            // Create multiple books
            for (int i = 0; i < 3; i++) {
                CreateBookRequest request = new CreateBookRequest(
                        "Book " + i,
                        "Author " + i,
                        "978-000000000" + i,
                        "Fiction",
                        LocalDate.of(2000, 1, 1)
                );
                bookService.createBook(request, "user" + i);
            }

            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "createdAt,desc");

            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getTotalElements()).isGreaterThanOrEqualTo(3);
            assertThat(response.getPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle pagination with multiple pages")
        void testListBooks_givenMultiplePages_thenCorrectPagination() {
            // Create 25 books to ensure multiple pages with size=20
            List<String> bookIds = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                CreateBookRequest request = new CreateBookRequest(
                        "Book " + i + System.nanoTime(),
                        "Author " + i,
                        "978-" + i + (System.nanoTime() % 9999),
                        "Fiction",
                        LocalDate.of(2000, 1, 1)
                );
                BookResponse response = bookService.createBook(request, "user" + i);
                bookIds.add(response.getId());
            }

            // Get first page
            PagedResponse<BookResponse> page1 = bookService.listBooks(0, 20, "createdAt,desc");
            assertThat(page1.getContent()).hasSize(20);
            assertThat(page1.isHasNextPage()).isTrue();

            // Get second page
            PagedResponse<BookResponse> page2 = bookService.listBooks(1, 20, "createdAt,desc");
            assertThat(page2.getContent()).hasSize(5);
            assertThat(page2.isHasPreviousPage()).isTrue();

            // Clean up
            for (String id : bookIds) {
                try {
                    bookService.deleteBook(id, "user0");
                } catch (Exception e) {
                    // Ignore cleanup errors
                }
            }
        }
    }

    @Nested
    @DisplayName("Update Book Integration Tests")
    class UpdateBookIntegrationTests {

        @Test
        @DisplayName("Should update book successfully")
        void testUpdateBook_givenValidUpdate_thenSuccess() {
            BookResponse created = bookService.createBook(validCreateRequest, "creator");

            UpdateBookRequest updateRequest = new UpdateBookRequest(
                    "Updated Title",
                    "Updated Author",
                    null,
                    null,
                    null
            );

            BookResponse updated = bookService.updateBook(created.getId(), updateRequest, "creator");

            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getAuthor()).isEqualTo("Updated Author");
            assertThat(updated.getCreatedBy()).isEqualTo("creator");
        }

        @Test
        @DisplayName("Should persist update to storage")
        void testUpdateBook_thenRetrieveUpdatedBook() {
            BookResponse created = bookService.createBook(validCreateRequest, "creator");

            UpdateBookRequest updateRequest = new UpdateBookRequest(
                    "New Title",
                    null,
                    null,
                    null,
                    null
            );

            bookService.updateBook(created.getId(), updateRequest, "creator");

            BookResponse retrieved = bookService.getBook(created.getId());

            assertThat(retrieved.getTitle()).isEqualTo("New Title");
        }
    }

    @Nested
    @DisplayName("Delete Book Integration Tests")
    class DeleteBookIntegrationTests {

        @Test
        @DisplayName("Should delete book successfully")
        void testDeleteBook_givenValidId_thenSuccess() {
            BookResponse created = bookService.createBook(validCreateRequest, "creator");

            bookService.deleteBook(created.getId(), "creator");

            assertThatThrownBy(() -> bookService.getBook(created.getId()))
                    .isInstanceOf(com.epam.book_review_svc.exception.NotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Validator Integration Tests")
    class ValidatorIntegrationTests {

        @Test
        @DisplayName("Should validate ISBN in all formats")
        void testValidateISBN_givenVariousFormats_thenValidateCorrectly() {
            String[] validISBNs = {
                    "9780143127994",
                    "978-0-143127-99-4",
                    "0143127993",
                    "0-143-12799-3"
            };

            for (String isbn : validISBNs) {
                CreateBookRequest request = new CreateBookRequest(
                        "Test Book",
                        "Test Author",
                        isbn,
                        "Fiction",
                        LocalDate.of(2000, 1, 1)
                );

                var errors = bookValidator.validateCreateBookRequest(request);
                assertThat(errors)
                        .filteredOn(e -> e.getField().equals("ISBN"))
                        .as("ISBN: " + isbn)
                        .isEmpty();
            }
        }

        @Test
        @DisplayName("Should reject invalid ISBNs")
        void testValidateISBN_givenInvalidFormats_thenRejectAll() {
            String[] invalidISBNs = {
                    "123",
                    "invalid-isbn",
                    "978-0-143127-99-4-extra",
                    "abc0143127994"
            };

            for (String isbn : invalidISBNs) {
                CreateBookRequest request = new CreateBookRequest(
                        "Test Book",
                        "Test Author",
                        isbn,
                        "Fiction",
                        LocalDate.of(2000, 1, 1)
                );

                var errors = bookValidator.validateCreateBookRequest(request);
                assertThat(errors)
                        .filteredOn(e -> e.getField().equals("ISBN"))
                        .as("ISBN: " + isbn)
                        .isNotEmpty();
            }
        }
    }
}
