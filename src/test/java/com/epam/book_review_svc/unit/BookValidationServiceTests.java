package com.epam.book_review_svc.unit;

import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.repository.JsonFileBookRepository;
import com.epam.book_review_svc.service.BookValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookValidationService Unit Tests")
class BookValidationServiceTests {

    @Mock
    private JsonFileBookRepository bookRepository;

    @InjectMocks
    private BookValidationService bookValidationService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId("550e8400-e29b-41d4-a716-446655440000");
        testBook.setTitle("Clean Code");
        testBook.setAuthor("Robert C. Martin");
        testBook.setISBN("978-0132350884");
        testBook.setGenre("Software Engineering");
        testBook.setPublicationDate(LocalDate.of(2008, 8, 11));
    }

    @Nested
    @DisplayName("Book Validation Tests")
    class BookValidationTests {

        @Test
        @DisplayName("Should return true when book ID exists")
        void testIsBookValid_givenExistingBookId_thenReturnTrue() {
            List<Book> books = new ArrayList<>();
            books.add(testBook);

            when(bookRepository.findAll()).thenReturn(books);

            boolean result = bookValidationService.isBookValid("550e8400-e29b-41d4-a716-446655440000");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when book ID does not exist")
        void testIsBookValid_givenNonExistentBookId_thenReturnFalse() {
            List<Book> books = new ArrayList<>();
            books.add(testBook);

            when(bookRepository.findAll()).thenReturn(books);

            boolean result = bookValidationService.isBookValid("nonexistent-id");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when book ID is null")
        void testIsBookValid_givenNullBookId_thenReturnFalse() {
            boolean result = bookValidationService.isBookValid(null);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when book ID is blank")
        void testIsBookValid_givenBlankBookId_thenReturnFalse() {
            boolean result = bookValidationService.isBookValid("   ");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when no books exist")
        void testIsBookValid_givenEmptyRepository_thenReturnFalse() {
            when(bookRepository.findAll()).thenReturn(new ArrayList<>());

            boolean result = bookValidationService.isBookValid("550e8400-e29b-41d4-a716-446655440000");

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when multiple books exist and ID matches")
        void testIsBookValid_givenMultipleBooksAndMatchingId_thenReturnTrue() {
            Book book2 = new Book();
            book2.setId("550e8400-e29b-41d4-a716-446655440001");
            book2.setTitle("Design Patterns");

            List<Book> books = new ArrayList<>();
            books.add(testBook);
            books.add(book2);

            when(bookRepository.findAll()).thenReturn(books);

            boolean result = bookValidationService.isBookValid("550e8400-e29b-41d4-a716-446655440001");

            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Get All Books Tests")
    class GetAllBooksTests {

        @Test
        @DisplayName("Should return all books when repository contains books")
        void testGetAllBooks_givenMultipleBooks_thenReturnAllBooks() {
            Book book2 = new Book();
            book2.setId("550e8400-e29b-41d4-a716-446655440001");
            book2.setTitle("Design Patterns");

            List<Book> books = new ArrayList<>();
            books.add(testBook);
            books.add(book2);

            when(bookRepository.findAll()).thenReturn(books);

            List<Book> result = bookValidationService.getAllBooks();

            assertThat(result).hasSize(2);
            assertThat(result).contains(testBook, book2);
        }

        @Test
        @DisplayName("Should return empty list when repository is empty")
        void testGetAllBooks_givenEmptyRepository_thenReturnEmptyList() {
            when(bookRepository.findAll()).thenReturn(new ArrayList<>());

            List<Book> result = bookValidationService.getAllBooks();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return single book when only one book exists")
        void testGetAllBooks_givenSingleBook_thenReturnSingleBook() {
            List<Book> books = new ArrayList<>();
            books.add(testBook);

            when(bookRepository.findAll()).thenReturn(books);

            List<Book> result = bookValidationService.getAllBooks();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testBook);
        }
    }
}
