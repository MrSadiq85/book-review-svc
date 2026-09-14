package com.epam.book_review_svc.integration;

import com.epam.book_review_svc.exception.DataAccessException;
import com.epam.book_review_svc.exception.NotFoundException;
import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.repository.JsonFileBookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for JsonFileBookRepository.
 * Tests file I/O operations, data persistence, and exception handling.
 */
@SpringBootTest
@DisplayName("JsonFileBookRepository Integration Tests")
public class JsonFileBookRepositoryIntegrationTests {

    @Autowired
    private JsonFileBookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ResourceLoader resourceLoader;

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
                .createdBy("testUser")
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();
    }

    @AfterEach
    public void tearDown() {
        // Clean up by removing all books from file
        try {
            List<Book> books = bookRepository.findAll();
            for (Book book : books) {
                bookRepository.delete(book.getId());
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }

    @Nested
    @DisplayName("Save and Retrieve Tests")
    class SaveAndRetrieveTests {

        @Test
        @DisplayName("Should save book and persist to file")
        void testSaveBook_givenValidBook_thenPersist() {
            Book saved = bookRepository.save(testBook);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isEqualTo(testBook.getId());
            assertThat(saved.getTitle()).isEqualTo(testBook.getTitle());
        }

        @Test
        @DisplayName("Should retrieve book by ID after save")
        void testFindBookById_givenSavedBook_thenRetrieve() {
            bookRepository.save(testBook);

            Optional<Book> found = bookRepository.findById(testBook.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(testBook.getId());
            assertThat(found.get().getTitle()).isEqualTo("Test Book");
        }

        @Test
        @DisplayName("Should not find book with non-existent ID")
        void testFindBookById_givenNonExistentId_thenReturnEmpty() {
            Optional<Book> found = bookRepository.findById("nonexistent-id");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should retrieve book by ISBN after save")
        void testFindBookByISBN_givenSavedBook_thenRetrieve() {
            bookRepository.save(testBook);

            Optional<Book> found = bookRepository.findByISBN("978-0123456789");

            assertThat(found).isPresent();
            assertThat(found.get().getISBN()).isEqualTo("978-0123456789");
        }

        @Test
        @DisplayName("Should not find book with non-existent ISBN")
        void testFindBookByISBN_givenNonExistentISBN_thenReturnEmpty() {
            Optional<Book> found = bookRepository.findByISBN("999-9999999999");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Find All Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return empty list when no books saved")
        void testFindAll_givenNoBooks_thenReturnEmpty() {
            List<Book> books = bookRepository.findAll();

            assertThat(books).isEmpty();
        }

        @Test
        @DisplayName("Should return all saved books")
        void testFindAll_givenMultipleBooks_thenReturnAll() {
            Book book1 = testBook;
            Book book2 = Book.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Book 2")
                    .author("Author 2")
                    .ISBN("978-9999999999")
                    .genre("Non-Fiction")
                    .publicationDate(LocalDate.of(2005, 1, 1))
                    .createdBy("user2")
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();

            bookRepository.save(book1);
            bookRepository.save(book2);

            List<Book> books = bookRepository.findAll();

            assertThat(books).hasSize(2);
            assertThat(books)
                    .extracting(Book::getId)
                    .containsExactlyInAnyOrder(book1.getId(), book2.getId());
        }

        @Test
        @DisplayName("Should maintain data consistency across multiple saves and retrievals")
        void testFindAll_givenMultipleSavesAndRetrievals_thenMaintainConsistency() {
            for (int i = 0; i < 5; i++) {
                Book book = Book.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Book " + i)
                        .author("Author " + i)
                        .ISBN("978-000000000" + i)
                        .genre("Fiction")
                        .publicationDate(LocalDate.of(2000, 1, 1))
                        .createdBy("user" + i)
                        .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build();
                bookRepository.save(book);
            }

            List<Book> books = bookRepository.findAll();

            assertThat(books).hasSize(5);
        }
    }

    @Nested
    @DisplayName("Update Tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update existing book")
        void testUpdateBook_givenExistingBook_thenUpdate() {
            bookRepository.save(testBook);

            testBook.setTitle("Updated Title");
            testBook.setAuthor("Updated Author");

            Book updated = bookRepository.update(testBook.getId(), testBook);

            assertThat(updated.getTitle()).isEqualTo("Updated Title");
            assertThat(updated.getAuthor()).isEqualTo("Updated Author");
        }

        @Test
        @DisplayName("Should throw NotFoundException when updating non-existent book")
        void testUpdateBook_givenNonExistentId_thenThrowNotFoundException() {
            assertThatThrownBy(() -> bookRepository.update("nonexistent", testBook))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should persist updated book to file")
        void testUpdateBook_givenExistingBook_thenPersistToFile() {
            bookRepository.save(testBook);

            testBook.setTitle("Updated Title");
            bookRepository.update(testBook.getId(), testBook);

            Optional<Book> retrieved = bookRepository.findById(testBook.getId());

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getTitle()).isEqualTo("Updated Title");
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete saved book")
        void testDeleteBook_givenExistingBook_thenDelete() {
            bookRepository.save(testBook);

            bookRepository.delete(testBook.getId());

            Optional<Book> retrieved = bookRepository.findById(testBook.getId());

            assertThat(retrieved).isEmpty();
        }

        @Test
        @DisplayName("Should throw NotFoundException when deleting non-existent book")
        void testDeleteBook_givenNonExistentId_thenThrowNotFoundException() {
            assertThatThrownBy(() -> bookRepository.delete("nonexistent"))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("Should maintain consistency when deleting one of multiple books")
        void testDeleteBook_givenMultipleBooks_thenDeleteOnlyOne() {
            Book book1 = testBook;
            Book book2 = Book.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Book 2")
                    .author("Author 2")
                    .ISBN("978-9999999999")
                    .genre("Non-Fiction")
                    .publicationDate(LocalDate.of(2005, 1, 1))
                    .createdBy("user2")
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();

            bookRepository.save(book1);
            bookRepository.save(book2);

            bookRepository.delete(book1.getId());

            List<Book> remaining = bookRepository.findAll();

            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getId()).isEqualTo(book2.getId());
        }
    }

    @Nested
    @DisplayName("Count Tests")
    class CountTests {

        @Test
        @DisplayName("Should return 0 count when no books saved")
        void testCount_givenNoBooks_thenReturnZero() {
            long count = bookRepository.count();

            assertThat(count).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return correct count for multiple books")
        void testCount_givenMultipleBooks_thenReturnCorrectCount() {
            for (int i = 0; i < 5; i++) {
                Book book = Book.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Book " + i)
                        .author("Author " + i)
                        .ISBN("978-000000000" + i)
                        .genre("Fiction")
                        .publicationDate(LocalDate.of(2000, 1, 1))
                        .createdBy("user" + i)
                        .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build();
                bookRepository.save(book);
            }

            long count = bookRepository.count();

            assertThat(count).isEqualTo(5);
        }

        @Test
        @DisplayName("Should update count after deletion")
        void testCount_givenMultipleBooksAfterDeletion_thenReturnUpdatedCount() {
            Book book1 = testBook;
            Book book2 = Book.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Book 2")
                    .author("Author 2")
                    .ISBN("978-9999999999")
                    .genre("Non-Fiction")
                    .publicationDate(LocalDate.of(2005, 1, 1))
                    .createdBy("user2")
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();

            bookRepository.save(book1);
            bookRepository.save(book2);

            long countBefore = bookRepository.count();
            assertThat(countBefore).isEqualTo(2);

            bookRepository.delete(book1.getId());

            long countAfter = bookRepository.count();
            assertThat(countAfter).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Data Integrity Tests")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should preserve all book fields when saving and retrieving")
        void testDataPreservation_givenBookWithAllFields_thenPreserveAll() {
            bookRepository.save(testBook);

            Optional<Book> retrieved = bookRepository.findById(testBook.getId());

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get())
                    .usingRecursiveComparison()
                    .isEqualTo(testBook);
        }

        @Test
        @DisplayName("Should handle special characters in title and author")
        void testDataPreservation_givenSpecialCharacters_thenPreserve() {
            testBook.setTitle("Title with \"quotes\" and 'apostrophes'");
            testBook.setAuthor("Author with special chars: @#$%^&*()");

            bookRepository.save(testBook);

            Optional<Book> retrieved = bookRepository.findById(testBook.getId());

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getTitle()).contains("quotes");
            assertThat(retrieved.get().getAuthor()).contains("@#$%");
        }

        @Test
        @DisplayName("Should handle Unicode characters in book data")
        void testDataPreservation_givenUnicodeCharacters_thenPreserve() {
            testBook.setTitle("Title with émojis 🎉 and Unicode ñ");
            testBook.setAuthor("作者 (Japanese Author)");

            bookRepository.save(testBook);

            Optional<Book> retrieved = bookRepository.findById(testBook.getId());

            assertThat(retrieved).isPresent();
            assertThat(retrieved.get().getTitle()).contains("émojis");
            assertThat(retrieved.get().getAuthor()).contains("作者");
        }
    }

    @Nested
    @DisplayName("Concurrent Operation Tests")
    class ConcurrentOperationTests {

        @Test
        @DisplayName("Should handle rapid successive saves")
        void testRapidSaves_givenMultipleBooks_thenPersistAll() {
            for (int i = 0; i < 10; i++) {
                Book book = Book.builder()
                        .id(UUID.randomUUID().toString())
                        .title("Book " + i)
                        .author("Author " + i)
                        .ISBN("978-000000000" + i)
                        .genre("Fiction")
                        .publicationDate(LocalDate.of(2000, 1, 1))
                        .createdBy("user" + i)
                        .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                        .build();
                bookRepository.save(book);
            }

            long count = bookRepository.count();

            assertThat(count).isEqualTo(10);
        }

        @Test
        @DisplayName("Should maintain data consistency after mixed operations")
        void testMixedOperations_givenSaveUpdateDelete_thenMaintainConsistency() {
            Book book1 = testBook;
            Book book2 = Book.builder()
                    .id(UUID.randomUUID().toString())
                    .title("Book 2")
                    .author("Author 2")
                    .ISBN("978-9999999999")
                    .genre("Non-Fiction")
                    .publicationDate(LocalDate.of(2005, 1, 1))
                    .createdBy("user2")
                    .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                    .build();

            bookRepository.save(book1);
            bookRepository.save(book2);

            book1.setTitle("Updated Book 1");
            bookRepository.update(book1.getId(), book1);

            bookRepository.delete(book2.getId());

            List<Book> remaining = bookRepository.findAll();

            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getTitle()).isEqualTo("Updated Book 1");
        }
    }
}
