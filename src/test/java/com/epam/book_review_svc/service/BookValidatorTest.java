package com.epam.book_review_svc.service;

import com.epam.book_review_svc.model.dto.CreateBookRequest;
import com.epam.book_review_svc.model.dto.FieldError;
import com.epam.book_review_svc.model.dto.UpdateBookRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BookValidatorTest {

    private BookValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new BookValidator();
    }

    @Test
    public void testValidateCreateBookRequest_Success() {
        CreateBookRequest request = new CreateBookRequest(
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "978-0-7432-7356-5",
                "Fiction",
                LocalDate.of(1925, 4, 10)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).isEmpty();
    }

    @Test
    public void testValidateCreateBookRequest_MissingTitle() {
        CreateBookRequest request = new CreateBookRequest(
                "",
                "Author",
                "978-0123456789",
                "Fiction",
                LocalDate.of(2000, 1, 1)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getField()).isEqualTo("title");
    }

    @Test
    public void testValidateCreateBookRequest_InvalidISBN_Format() {
        CreateBookRequest request = new CreateBookRequest(
                "Test Book",
                "Author",
                "invalid-isbn",
                "Fiction",
                LocalDate.of(2000, 1, 1)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).hasSize(1);
    }

    @Test
    public void testValidateCreateBookRequest_InvalidGenre() {
        CreateBookRequest request = new CreateBookRequest(
                "Test Book",
                "Author",
                "978-0123456789",
                "InvalidGenre",
                LocalDate.of(2000, 1, 1)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).filteredOn(e -> e.getField().equals("genre")).hasSize(1);
    }

    @Test
    public void testValidateCreateBookRequest_FuturePublicationDate() {
        CreateBookRequest request = new CreateBookRequest(
                "Test Book",
                "Author",
                "978-0123456789",
                "Fiction",
                LocalDate.now().plusDays(1)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).filteredOn(e -> e.getField().equals("publicationDate")).hasSize(1);
    }

    @Test
    public void testValidateCreateBookRequest_MultipleErrors() {
        CreateBookRequest request = new CreateBookRequest(
                "",
                "",
                "invalid",
                "InvalidGenre",
                LocalDate.now().plusDays(1)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).hasSize(5); // title, author, ISBN, genre, publicationDate
    }

    @Test
    public void testValidateISBN_Format_ISBN13() {
        CreateBookRequest request = new CreateBookRequest(
                "Test Book",
                "Author",
                "9780143127994",
                "Fiction",
                LocalDate.of(2000, 1, 1)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).isEmpty();
    }

    @Test
    public void testValidateISBN_Format_ISBN10() {
        CreateBookRequest request = new CreateBookRequest(
                "Test Book",
                "Author",
                "0143127993",
                "Fiction",
                LocalDate.of(2000, 1, 1)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).isEmpty();
    }

    @Test
    public void testValidateISBN_Format_ISBN13WithHyphens() {
        CreateBookRequest request = new CreateBookRequest(
                "Test Book",
                "Author",
                "978-0-143127-99-4",
                "Fiction",
                LocalDate.of(2000, 1, 1)
        );

        List<FieldError> errors = validator.validateCreateBookRequest(request);

        assertThat(errors).filteredOn(e -> e.getField().equals("ISBN")).isEmpty();
    }

    @Test
    public void testValidateUpdateBookRequest_PartialUpdate() {
        UpdateBookRequest request = new UpdateBookRequest(
                "Updated Title",
                null,
                null,
                null,
                null
        );

        List<FieldError> errors = validator.validateUpdateBookRequest(request);

        assertThat(errors).isEmpty();
    }

    @Test
    public void testValidateUpdateBookRequest_InvalidTitle() {
        UpdateBookRequest request = new UpdateBookRequest(
                "a".repeat(501), // exceeds max length
                null,
                null,
                null,
                null
        );

        List<FieldError> errors = validator.validateUpdateBookRequest(request);

        assertThat(errors).hasSize(1);
        assertThat(errors.get(0).getField()).isEqualTo("title");
    }
}
