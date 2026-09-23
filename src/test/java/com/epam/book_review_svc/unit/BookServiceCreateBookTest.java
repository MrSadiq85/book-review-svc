package com.epam.book_review_svc.unit;

import com.epam.book_review_svc.exception.DuplicateIsbnException;
import com.epam.book_review_svc.exception.InvalidBookException;
import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.model.dto.BookRequestDto;
import com.epam.book_review_svc.repository.JsonFileBookRepository;
import com.epam.book_review_svc.service.BookService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceCreateBookTest {

    @Mock
    private JsonFileBookRepository repository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        bookService = new BookService(repository, validator);
    }

    @Test
    void createBookNormalizesFieldsGeneratesIdAndPersistsBook() {
        when(repository.findAll()).thenReturn(List.of());
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        when(repository.create(bookCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        Book result = bookService.createBook(BookRequestDto.builder()
            .isbn(" 978-0134685991 ")
            .title("  Effective Java  ")
            .author("  Joshua Bloch  ")
            .build());

        assertThat(result.getId()).isNotBlank();
        assertThat(result.getIsbn()).isEqualTo("9780134685991");
        assertThat(result.getTitle()).isEqualTo("Effective Java");
        assertThat(result.getAuthor()).isEqualTo("Joshua Bloch");
        assertThat(bookCaptor.getValue()).isEqualTo(result);
        verify(repository).create(result);
    }

    @Test
    void createBookRejectsDuplicateIsbnBeforePersistence() {
        when(repository.findAll()).thenReturn(List.of(
            Book.builder()
                .id("existing-book")
                .isbn("9780134685991")
                .title("Effective Java")
                .author("Joshua Bloch")
                .build()));

        assertThatThrownBy(() -> bookService.createBook(BookRequestDto.builder()
            .isbn("978-0134685991")
            .title("Another Book")
            .author("Another Author")
            .build()))
            .isInstanceOf(DuplicateIsbnException.class)
            .hasMessage("isbn already exists for another book");

        verify(repository).findAll();
        verify(repository, never()).create(org.mockito.ArgumentMatchers.any(Book.class));
    }

    @Test
    void createBookRejectsInvalidIsbnBeforeReadingExistingBooks() {
        assertThatThrownBy(() -> bookService.createBook(BookRequestDto.builder()
            .isbn("invalid-isbn")
            .title("Effective Java")
            .author("Joshua Bloch")
            .build()))
            .isInstanceOf(InvalidBookException.class)
            .hasMessage("isbn is invalid");

        verifyNoInteractions(repository);
    }
}
