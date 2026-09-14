package com.epam.book_review_svc.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookValidator bookValidator;

    @InjectMocks
    private BookService bookService;

    private Book testBook;

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
    }

    @Test
    public void testCreateBook_Success() {
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

        BookResponse response = bookService.createBook(request, "user123");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Test Book");
        assertThat(response.getCreatedBy()).isEqualTo("user123");
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void testCreateBook_NoUser() {
        CreateBookRequest request = new CreateBookRequest(
                "Test Book",
                "Test Author",
                "978-0123456789",
                "Fiction",
                LocalDate.of(2000, 1, 1)
        );

        assertThatThrownBy(() -> bookService.createBook(request, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("authenticated");
    }

    @Test
    public void testCreateBook_ValidationFailed() {
        CreateBookRequest request = new CreateBookRequest(
                "",
                "Author",
                "isbn",
                "InvalidGenre",
                LocalDate.now().plusDays(1)
        );

        when(bookValidator.validateCreateBookRequest(request))
                .thenReturn(List.of(
                        new com.epam.book_review_svc.model.dto.FieldError("title", "Title is required"),
                        new com.epam.book_review_svc.model.dto.FieldError("ISBN", "Invalid format"),
                        new com.epam.book_review_svc.model.dto.FieldError("genre", "Invalid genre"),
                        new com.epam.book_review_svc.model.dto.FieldError("publicationDate", "Cannot be future")
                ));

        assertThatThrownBy(() -> bookService.createBook(request, "user123"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    public void testCreateBook_DuplicateISBN() {
        CreateBookRequest request = new CreateBookRequest(
                "Test Book",
                "Test Author",
                "978-0123456789",
                "Fiction",
                LocalDate.of(2000, 1, 1)
        );

        when(bookValidator.validateCreateBookRequest(request)).thenReturn(List.of());
        when(bookRepository.findByISBN("978-0123456789")).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> bookService.createBook(request, "user123"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    public void testListBooks_Success() {
        when(bookRepository.findAll()).thenReturn(List.of(testBook));

        PagedResponse<BookResponse> response = bookService.listBooks(0, 20, "createdAt,desc");

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.isHasNextPage()).isFalse();
        assertThat(response.isHasPreviousPage()).isFalse();
    }

    @Test
    public void testListBooks_InvalidPage() {
        assertThatThrownBy(() -> bookService.listBooks(-1, 20, "createdAt,desc"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testListBooks_InvalidSize() {
        assertThatThrownBy(() -> bookService.listBooks(0, 101, "createdAt,desc"))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    public void testGetBook_Success() {
        when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));

        BookResponse response = bookService.getBook("123");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(testBook.getId());
    }

    @Test
    public void testGetBook_NotFound() {
        when(bookRepository.findById("123")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBook("123"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void testUpdateBook_Success() {
        UpdateBookRequest request = new UpdateBookRequest(
                "Updated Title",
                "Updated Author",
                "978-0123456789",
                "Non-Fiction",
                LocalDate.of(2000, 1, 1)
        );

        Book updatedBook = Book.builder()
                .id("123")
                .title("Updated Title")
                .author("Updated Author")
                .ISBN("978-0123456789")
                .genre("Non-Fiction")
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

    @Test
    public void testUpdateBook_NotCreator() {
        UpdateBookRequest request = new UpdateBookRequest(
                "Updated Title",
                "Updated Author",
                "978-0123456789",
                "Fiction",
                LocalDate.of(2000, 1, 1)
        );

        when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> bookService.updateBook("123", request, "otherUser"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("creator");
    }

    @Test
    public void testDeleteBook_Success() {
        when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete("123");

        bookService.deleteBook("123", "user123");

        verify(bookRepository, times(1)).delete("123");
    }

    @Test
    public void testDeleteBook_NotCreator() {
        when(bookRepository.findById("123")).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> bookService.deleteBook("123", "otherUser"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("creator");
    }
}
