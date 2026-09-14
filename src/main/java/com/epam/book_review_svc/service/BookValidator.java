package com.epam.book_review_svc.service;

import com.epam.book_review_svc.model.dto.CreateBookRequest;
import com.epam.book_review_svc.model.dto.FieldError;
import com.epam.book_review_svc.model.dto.UpdateBookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class BookValidator {

    private static final int TITLE_MIN_LENGTH = 1;
    private static final int TITLE_MAX_LENGTH = 500;
    private static final int AUTHOR_MIN_LENGTH = 1;
    private static final int AUTHOR_MAX_LENGTH = 200;

    private static final Set<String> VALID_GENRES = Set.of(
            "Fiction", "Non-Fiction", "Mystery", "Romance", "Science Fiction",
            "Fantasy", "Biography", "History", "Self-Help", "Children", "Young Adult"
    );

    public List<FieldError> validateCreateBookRequest(CreateBookRequest request) {
        List<FieldError> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new FieldError("request", "Request cannot be null"));
            return errors;
        }

        errors.addAll(validateTitle(request.getTitle()));
        errors.addAll(validateAuthor(request.getAuthor()));
        errors.addAll(validateISBN(request.getISBN()));
        errors.addAll(validateGenre(request.getGenre()));
        errors.addAll(validatePublicationDate(request.getPublicationDate()));

        return errors;
    }

    public List<FieldError> validateUpdateBookRequest(UpdateBookRequest request) {
        List<FieldError> errors = new ArrayList<>();

        if (request == null) {
            errors.add(new FieldError("request", "Request cannot be null"));
            return errors;
        }

        // For updates, all fields are optional - only validate if provided
        if (request.getTitle() != null) {
            errors.addAll(validateTitle(request.getTitle()));
        }
        if (request.getAuthor() != null) {
            errors.addAll(validateAuthor(request.getAuthor()));
        }
        if (request.getISBN() != null) {
            errors.addAll(validateISBN(request.getISBN()));
        }
        if (request.getGenre() != null) {
            errors.addAll(validateGenre(request.getGenre()));
        }
        if (request.getPublicationDate() != null) {
            errors.addAll(validatePublicationDate(request.getPublicationDate()));
        }

        return errors;
    }

    private List<FieldError> validateTitle(String title) {
        List<FieldError> errors = new ArrayList<>();

        if (title == null || title.trim().isEmpty()) {
            errors.add(new FieldError("title", "Title is required"));
            return errors;
        }

        String trimmed = title.trim();
        if (trimmed.length() < TITLE_MIN_LENGTH || trimmed.length() > TITLE_MAX_LENGTH) {
            errors.add(new FieldError("title",
                    String.format("Title must be between %d and %d characters",
                            TITLE_MIN_LENGTH, TITLE_MAX_LENGTH)));
        }

        return errors;
    }

    private List<FieldError> validateAuthor(String author) {
        List<FieldError> errors = new ArrayList<>();

        if (author == null || author.trim().isEmpty()) {
            errors.add(new FieldError("author", "Author is required"));
            return errors;
        }

        String trimmed = author.trim();
        if (trimmed.length() < AUTHOR_MIN_LENGTH || trimmed.length() > AUTHOR_MAX_LENGTH) {
            errors.add(new FieldError("author",
                    String.format("Author must be between %d and %d characters",
                            AUTHOR_MIN_LENGTH, AUTHOR_MAX_LENGTH)));
        }

        return errors;
    }

    private List<FieldError> validateISBN(String isbn) {
        List<FieldError> errors = new ArrayList<>();

        if (isbn == null || isbn.trim().isEmpty()) {
            errors.add(new FieldError("ISBN", "ISBN is required"));
            return errors;
        }

        // Remove hyphens for validation
        String cleanISBN = isbn.replaceAll("-", "");

        // Validate ISBN-10 (10 digits) or ISBN-13 (13 digits)
        if (!cleanISBN.matches("^\\d{10}$") && !cleanISBN.matches("^\\d{13}$")) {
            errors.add(new FieldError("ISBN",
                    "Invalid ISBN format. Expected ISBN-10 (10 digits) or ISBN-13 (13 digits)"));
        }

        return errors;
    }

    private List<FieldError> validateGenre(String genre) {
        List<FieldError> errors = new ArrayList<>();

        if (genre == null || genre.trim().isEmpty()) {
            errors.add(new FieldError("genre", "Genre is required"));
            return errors;
        }

        if (!VALID_GENRES.contains(genre)) {
            errors.add(new FieldError("genre",
                    String.format("Genre must be one of: %s", String.join(", ", VALID_GENRES))));
        }

        return errors;
    }

    private List<FieldError> validatePublicationDate(LocalDate date) {
        List<FieldError> errors = new ArrayList<>();

        if (date == null) {
            errors.add(new FieldError("publicationDate", "Publication date is required"));
            return errors;
        }

        // Check if date is in future
        if (date.isAfter(LocalDate.now())) {
            errors.add(new FieldError("publicationDate",
                    "Publication date cannot be in the future"));
        }

        return errors;
    }
}
