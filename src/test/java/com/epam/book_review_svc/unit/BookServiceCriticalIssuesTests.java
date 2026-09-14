package com.epam.book_review_svc.unit;

import com.epam.book_review_svc.exception.BadRequestException;
import com.epam.book_review_svc.exception.ConflictException;
import com.epam.book_review_svc.exception.ForbiddenException;
import com.epam.book_review_svc.exception.NotFoundException;
import com.epam.book_review_svc.exception.ValidationException;
import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.model.dto.BookResponse;
import com.epam.book_review_svc.model.dto.CreateBookRequest;
import com.epam.book_review_svc.model.dto.PagedResponse;
import com.epam.book_review_svc.model.dto.UpdateBookRequest;
import com.epam.book_review_svc.repository.BookRepository;
import com.epam.book_review_svc.service.BookService;
import com.epam.book_review_svc.service.BookValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Critical issues test suite for BookService.
 * Focuses on:
 * 1. Authorization logic contradiction (permitAll vs ForbiddenException checks)
 * 2. Null-safety bugs in createdBy field
 * 3. Pagination edge cases (hasNextPage calculation bug)
 * 4. ISBN validation edge cases
 * 5. Exception handling coverage
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookService Critical Issues Tests")
public class BookServiceCriticalIssuesTests {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookValidator bookValidator;

    @InjectMocks
    private BookService bookService;

    private Book testBook;
    private Book testBookWithNullCreatedBy;

    @BeforeEach
    public void setUp() {
        testBook = Book.builder()
                .id(UUID.randomUUID().toString())
                .title("Test Book")
                .author("Test Author")
                .ISBN("978-0123456789")
                .genre("Fiction")
                .publicationDate(LocalDate.of(2000, 1, 1))
                .createdBy("user123")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        testBookWithNullCreatedBy = Book.builder()
                .id(UUID.randomUUID().toString())
                .title("Unsafe Book")
                .author("Unsafe Author")
                .ISBN("978-9999999999")
                .genre("Fiction")
                .publicationDate(LocalDate.of(2000, 1, 1))
                .createdBy(null)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @Nested
    @DisplayName("Authorization Logic Tests - Issue: permitAll configuration vs ForbiddenException checks")
    class AuthorizationLogicTests {

        @Test
        @DisplayName("Should reject null userId when creating book despite permitAll security")
        void testCreateBook_givenNullUserId_thenThrowForbiddenException() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "978-0123456789",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            when(bookValidator.validateCreateBookRequest(request)).thenReturn(List.of());

            assertThatThrownBy(() -> bookService.createBook(request, null))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("authenticated");
        }

        @Test
        @DisplayName("Should reject empty userId when creating book despite permitAll security")
        void testCreateBook_givenEmptyUserId_thenThrowForbiddenException() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "978-0123456789",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            when(bookValidator.validateCreateBookRequest(request)).thenReturn(List.of());

            assertThatThrownBy(() -> bookService.createBook(request, "   "))
                    .isInstanceOf(ForbiddenException.class);
        }

        @Test
        @DisplayName("Should accept valid userId for book creation")
        void testCreateBook_givenValidUserId_thenSucceed() {
            CreateBookRequest request = new CreateBookRequest(
                    "Test Book",
                    "Test Author",
                    "978-0123456789",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            when(bookValidator.validateCreateBookRequest(request)).thenReturn(List.of());
            when(bookRepository.findByISBN("978-0123456789")).thenReturn(Optional.empty());
            when(bookRepository.save(any(Book.class))).thenReturn(testBook);

            BookResponse response = bookService.createBook(request, "validUser");

            assertThat(response).isNotNull();
            assertThat(response.getCreatedBy()).isEqualTo("user123");
            verify(bookRepository, times(1)).save(any(Book.class));
        }
    }

    @Nested
    @DisplayName("Null-Safety Bug Tests - Issue: createdBy null check can cause NPE")
    class NullSafetyBugTests {

        @Test
        @DisplayName("Should handle NullPointerException when updating book with null createdBy")
        void testUpdateBook_givenBookWithNullCreatedBy_thenThrowNullPointerException() {
            UpdateBookRequest request = new UpdateBookRequest(
                    "Updated Title",
                    null,
                    null,
                    null,
                    null
            );

            when(bookRepository.findById("123")).thenReturn(Optional.of(testBookWithNullCreatedBy));

            assertThatThrownBy(() -> bookService.updateBook("123", request, "user123"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle NullPointerException when deleting book with null createdBy")
        void testDeleteBook_givenBookWithNullCreatedBy_thenThrowNullPointerException() {
            when(bookRepository.findById("123")).thenReturn(Optional.of(testBookWithNullCreatedBy));

            assertThatThrownBy(() -> bookService.deleteBook("123", "user123"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should properly check authorization when createdBy equals userId")
        void testUpdateBook_givenCorrectCreatedBy_thenSucceed() {
            UpdateBookRequest request = new UpdateBookRequest(
                    "Updated Title",
                    "Updated Author",
                    null,
                    null,
                    null
            );

            Book updatedBook = Book.builder()
                    .id("123")
                    .title("Updated Title")
                    .author("Updated Author")
                    .ISBN("978-0123456789")
                    .genre("Fiction")
                    .publicationDate(LocalDate.of(2000, 1, 1))
                    .createdBy("user123")
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();

            when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));
            when(bookValidator.validateUpdateBookRequest(request)).thenReturn(List.of());
            when(bookRepository.update(anyString(), any(Book.class))).thenReturn(updatedBook);

            BookResponse response = bookService.updateBook("123", request, "user123");

            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).update(anyString(), any(Book.class));
        }
    }

    @Nested
    @DisplayName("Pagination Edge Cases Tests - Issue: hasNextPage calculation off-by-one error")
    class PaginationEdgeCaseTests {

        @Test
        @DisplayName("Should correctly set hasNextPage=false when on last page")
        void testListBooks_givenLastPage_thenHasNextPageFalse() {
            List<Book> books = List.of(
                    testBook,
                    createBook("book2"),
                    createBook("book3")
            );

            when(bookRepository.findAll()).thenReturn(books);

            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "createdAt,desc");

            assertThat(response.isHasNextPage()).isFalse();
            assertThat(response.getTotalPages()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should correctly set hasNextPage=true when more pages available")
        void testListBooks_givenMultiplePages_thenHasNextPageTrue() {
            List<Book> books = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                books.add(createBook("book" + i));
            }

            when(bookRepository.findAll()).thenReturn(books);

            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "createdAt,desc");

            assertThat(response.isHasNextPage()).isTrue();
            assertThat(response.getTotalPages()).isEqualTo(2);
            assertThat(response.getContent()).hasSize(20);
        }

        @Test
        @DisplayName("Should detect bug: hasNextPage calculated as page < totalPages - 1 instead of <=")
        void testListBooks_givenPageBeforeLast_thenHasNextPageShouldBeCorrect() {
            List<Book> books = new ArrayList<>();
            for (int i = 0; i < 21; i++) {
                books.add(createBook("book" + i));
            }

            when(bookRepository.findAll()).thenReturn(books);

            // Request page 0, size 20 (should have 2 pages total: 0 and 1)
            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "createdAt,desc");

            // Bug: hasNextPage = page < totalPages - 1 evaluates to 0 < 2 - 1 = 0 < 1 = true (correct by luck)
            assertThat(response.getTotalPages()).isEqualTo(2);
            assertThat(response.isHasNextPage()).isTrue();
        }

        @Test
        @DisplayName("Should handle edge case: requesting page beyond available pages")
        void testListBooks_givenPageOutOfRange_thenReturnEmptyContent() {
            List<Book> books = List.of(testBook);

            when(bookRepository.findAll()).thenReturn(books);

            PagedResponse<BookResponse> response = bookService.listBooks(5, 20, "createdAt,desc");

            assertThat(response.getContent()).isEmpty();
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getPage()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle edge case: size = 1 with multiple books")
        void testListBooks_givenSize1_thenPaginateCorrectly() {
            List<Book> books = List.of(testBook, createBook("book2"), createBook("book3"));

            when(bookRepository.findAll()).thenReturn(books);

            PagedResponse<BookResponse> response = bookService.listBooks(0, 1, "createdAt,desc");

            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getTotalPages()).isEqualTo(3);
            assertThat(response.isHasNextPage()).isTrue();
        }

        @Test
        @DisplayName("Should handle edge case: size = 100 (maximum allowed)")
        void testListBooks_givenMaxSize_thenAccept() {
            List<Book> books = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                books.add(createBook("book" + i));
            }

            when(bookRepository.findAll()).thenReturn(books);

            PagedResponse<BookResponse> response = bookService.listBooks(0, 100, "createdAt,desc");

            assertThat(response.getContent()).hasSize(50);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.isHasNextPage()).isFalse();
        }

        @Test
        @DisplayName("Should reject size > 100")
        void testListBooks_givenSizeGreaterThan100_thenThrowBadRequestException() {
            assertThatThrownBy(() -> bookService.listBooks(0, 101, "createdAt,desc"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("between 1 and 100");
        }

        @Test
        @DisplayName("Should reject size < 1")
        void testListBooks_givenSizeZero_thenThrowBadRequestException() {
            assertThatThrownBy(() -> bookService.listBooks(0, 0, "createdAt,desc"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("between 1 and 100");
        }
    }

    @Nested
    @DisplayName("ISBN Validation Edge Cases Tests")
    class ISBNValidationEdgeCaseTests {

        @Test
        @DisplayName("Should reject duplicate ISBN during create")
        void testCreateBook_givenDuplicateISBN_thenThrowConflictException() {
            CreateBookRequest request = new CreateBookRequest(
                    "New Title",
                    "New Author",
                    "978-0123456789",
                    "Fiction",
                    LocalDate.of(2000, 1, 1)
            );

            when(bookValidator.validateCreateBookRequest(request)).thenReturn(List.of());
            when(bookRepository.findByISBN("978-0123456789")).thenReturn(Optional.of(testBook));

            assertThatThrownBy(() -> bookService.createBook(request, "user456"))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("already exists");
        }

        @Test
        @DisplayName("Should allow updating ISBN when changing to unique ISBN")
        void testUpdateBook_givenUniqueNewISBN_thenSucceed() {
            UpdateBookRequest request = new UpdateBookRequest(
                    null,
                    null,
                    "978-9999999999",
                    null,
                    null
            );

            Book updatedBook = Book.builder()
                    .id("123")
                    .title("Test Book")
                    .author("Test Author")
                    .ISBN("978-9999999999")
                    .genre("Fiction")
                    .publicationDate(LocalDate.of(2000, 1, 1))
                    .createdBy("user123")
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();

            when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));
            when(bookValidator.validateUpdateBookRequest(request)).thenReturn(List.of());
            when(bookRepository.findByISBN("978-9999999999")).thenReturn(Optional.empty());
            when(bookRepository.update(anyString(), any(Book.class))).thenReturn(updatedBook);

            BookResponse response = bookService.updateBook("123", request, "user123");

            assertThat(response).isNotNull();
            verify(bookRepository, times(1)).update(anyString(), any(Book.class));
        }

        @Test
        @DisplayName("Should reject updating to ISBN that already exists in other book")
        void testUpdateBook_givenExistingISBN_thenThrowConflictException() {
            UpdateBookRequest request = new UpdateBookRequest(
                    null,
                    null,
                    "978-9999999999",
                    null,
                    null
            );

            Book otherBook = createBook("otherBook");
            otherBook.setISBN("978-9999999999");

            when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));
            when(bookValidator.validateUpdateBookRequest(request)).thenReturn(List.of());
            when(bookRepository.findByISBN("978-9999999999")).thenReturn(Optional.of(otherBook));

            assertThatThrownBy(() -> bookService.updateBook("123", request, "user123"))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        @DisplayName("Should allow keeping same ISBN when updating")
        void testUpdateBook_givenSameISBN_thenSucceed() {
            UpdateBookRequest request = new UpdateBookRequest(
                    "Updated Title",
                    null,
                    "978-0123456789",
                    null,
                    null
            );

            Book updatedBook = Book.builder()
                    .id("123")
                    .title("Updated Title")
                    .author("Test Author")
                    .ISBN("978-0123456789")
                    .genre("Fiction")
                    .publicationDate(LocalDate.of(2000, 1, 1))
                    .createdBy("user123")
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();

            when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));
            when(bookValidator.validateUpdateBookRequest(request)).thenReturn(List.of());
            when(bookRepository.update(anyString(), any(Book.class))).thenReturn(updatedBook);

            BookResponse response = bookService.updateBook("123", request, "user123");

            assertThat(response).isNotNull();
            // findByISBN should not be called since ISBN is not changing
            verify(bookRepository, times(0)).findByISBN(anyString());
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should throw NotFoundException when book not found during update")
        void testUpdateBook_givenNonExistentBook_thenThrowNotFoundException() {
            UpdateBookRequest request = new UpdateBookRequest(
                    "Updated Title",
                    null,
                    null,
                    null,
                    null
            );

            when(bookRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.updateBook("nonexistent", request, "user123"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw NotFoundException when book not found during delete")
        void testDeleteBook_givenNonExistentBook_thenThrowNotFoundException() {
            when(bookRepository.findById("nonexistent")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookService.deleteBook("nonexistent", "user123"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-creator attempts update")
        void testUpdateBook_givenDifferentUser_thenThrowForbiddenException() {
            UpdateBookRequest request = new UpdateBookRequest(
                    "Updated Title",
                    null,
                    null,
                    null,
                    null
            );

            when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));

            assertThatThrownBy(() -> bookService.updateBook("123", request, "otherUser"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("creator");
        }

        @Test
        @DisplayName("Should throw ForbiddenException when non-creator attempts delete")
        void testDeleteBook_givenDifferentUser_thenThrowForbiddenException() {
            when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));

            assertThatThrownBy(() -> bookService.deleteBook("123", "otherUser"))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("creator");
        }

        @Test
        @DisplayName("Should throw ValidationException when validation fails")
        void testCreateBook_givenValidationErrors_thenThrowValidationException() {
            CreateBookRequest request = new CreateBookRequest(
                    "",
                    "",
                    "invalid",
                    "InvalidGenre",
                    LocalDate.now().plusDays(1)
            );

            var errors = List.of(
                    new com.epam.book_review_svc.model.dto.FieldError("title", "Title is required"),
                    new com.epam.book_review_svc.model.dto.FieldError("author", "Author is required")
            );

            when(bookValidator.validateCreateBookRequest(request)).thenReturn(errors);

            assertThatThrownBy(() -> bookService.createBook(request, "user123"))
                    .isInstanceOf(ValidationException.class);
        }
    }

    @Nested
    @DisplayName("Sorting Tests")
    class SortingTests {

        @Test
        @DisplayName("Should sort by title ascending")
        void testListBooks_givenSortByTitleAsc_thenSortCorrectly() {
            Book book1 = createBook("book1");
            book1.setTitle("Zebra");
            Book book2 = createBook("book2");
            book2.setTitle("Apple");
            Book book3 = createBook("book3");
            book3.setTitle("Banana");

            when(bookRepository.findAll()).thenReturn(List.of(book1, book2, book3));

            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "title,asc");

            assertThat(response.getContent())
                    .hasSize(3)
                    .extracting(BookResponse::getTitle)
                    .containsExactly("Apple", "Banana", "Zebra");
        }

        @Test
        @DisplayName("Should sort by title descending")
        void testListBooks_givenSortByTitleDesc_thenSortCorrectly() {
            Book book1 = createBook("book1");
            book1.setTitle("Zebra");
            Book book2 = createBook("book2");
            book2.setTitle("Apple");
            Book book3 = createBook("book3");
            book3.setTitle("Banana");

            when(bookRepository.findAll()).thenReturn(List.of(book1, book2, book3));

            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "title,desc");

            assertThat(response.getContent())
                    .hasSize(3)
                    .extracting(BookResponse::getTitle)
                    .containsExactly("Zebra", "Banana", "Apple");
        }

        @Test
        @DisplayName("Should sort by author ascending")
        void testListBooks_givenSortByAuthorAsc_thenSortCorrectly() {
            Book book1 = createBook("book1");
            book1.setAuthor("Zane");
            Book book2 = createBook("book2");
            book2.setAuthor("Alice");
            Book book3 = createBook("book3");
            book3.setAuthor("Bob");

            when(bookRepository.findAll()).thenReturn(List.of(book1, book2, book3));

            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "author,asc");

            assertThat(response.getContent())
                    .hasSize(3)
                    .extracting(BookResponse::getAuthor)
                    .containsExactly("Alice", "Bob", "Zane");
        }

        @Test
        @DisplayName("Should default to createdAt descending when sort is null")
        void testListBooks_givenNullSort_thenDefaultToCreatedAtDesc() {
            when(bookRepository.findAll()).thenReturn(List.of(testBook));

            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, null);

            assertThat(response.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should default to descending when direction is invalid")
        void testListBooks_givenInvalidDirection_thenDefaultToDesc() {
            when(bookRepository.findAll()).thenReturn(List.of(testBook));

            PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "title,invalid");

            assertThat(response.getContent()).hasSize(1);
        }
    }

    // Helper method to create test books
    private Book createBook(String id) {
        return Book.builder()
                .id(id)
                .title("Book " + id)
                .author("Author " + id)
                .ISBN("978-" + id + "0000000")
                .genre("Fiction")
                .publicationDate(LocalDate.of(2000, 1, 1))
                .createdBy("user123")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }
}
