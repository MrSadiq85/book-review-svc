package com.epam.book_review_svc.service;

import com.epam.book_review_svc.exception.BadRequestException;
import com.epam.book_review_svc.exception.ConflictException;
import com.epam.book_review_svc.exception.ForbiddenException;
import com.epam.book_review_svc.exception.NotFoundException;
import com.epam.book_review_svc.exception.ValidationException;
import com.epam.book_review_svc.model.Book;
import com.epam.book_review_svc.model.dto.BookResponse;
import com.epam.book_review_svc.model.dto.CreateBookRequest;
import com.epam.book_review_svc.model.dto.FieldError;
import com.epam.book_review_svc.model.dto.PagedResponse;
import com.epam.book_review_svc.model.dto.UpdateBookRequest;
import com.epam.book_review_svc.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookService {

    private final BookRepository bookRepository;
    private final BookValidator bookValidator;

    public BookService(BookRepository bookRepository, BookValidator bookValidator) {
        this.bookRepository = bookRepository;
        this.bookValidator = bookValidator;
    }

    public BookResponse createBook(CreateBookRequest request, String userId) {
        log.debug("Creating new book, userId: {}", userId);

        // Step 1: Validate authorization (Author role)
        // Note: Authorization check will be implemented in Phase 2 with Spring Security
        // For now, assuming all authenticated users can create books (will change)
        if (userId == null || userId.trim().isEmpty()) {
            throw new ForbiddenException("Only authenticated users can create books");
        }

        // Step 2: Validate input fields
        List<FieldError> errors = bookValidator.validateCreateBookRequest(request);
        if (!errors.isEmpty()) {
            log.warn("Validation failed for create book request with {} errors", errors.size());
            throw new ValidationException("Validation failed", errors);
        }

        // Step 3: Check ISBN uniqueness
        if (bookRepository.findByISBN(request.getISBN()).isPresent()) {
            throw new ConflictException(String.format("Book with ISBN %s already exists", request.getISBN()));
        }

        // Step 4: Create domain entity with auto-populated fields
        Book book = Book.builder()
                .id(UUID.randomUUID().toString())
                .title(request.getTitle())
                .author(request.getAuthor())
                .ISBN(request.getISBN())
                .genre(request.getGenre())
                .publicationDate(request.getPublicationDate())
                .createdBy(userId)
                .createdAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        // Step 5: Persist to storage
        Book saved = bookRepository.save(book);

        // Step 6: Convert to response DTO
        BookResponse response = convertToResponse(saved);
        log.info("Book created with ID: {}, Title: {}", response.getId(), response.getTitle());
        return response;
    }

    public PagedResponse<BookResponse> listBooks(int page, int size, String sort) {
        log.debug("Listing books with page: {}, size: {}, sort: {}", page, size, sort);

        // Validate pagination parameters
        if (page < 0) {
            throw new BadRequestException("Page must be >= 0");
        }
        if (size < 1 || size > 100) {
            throw new BadRequestException("Size must be between 1 and 100");
        }

        // Retrieve all books
        List<Book> allBooks = bookRepository.findAll();

        // Apply sorting
        List<Book> sorted = applySort(allBooks, sort);

        // Calculate pagination metadata
        long totalElements = sorted.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, (int) totalElements);

        // Handle out-of-range page gracefully
        if (startIndex >= totalElements && totalElements > 0) {
            log.debug("Page {} is out of range (total pages: {})", page, totalPages);
        }

        List<Book> pageContent = startIndex < sorted.size() ?
                sorted.subList(startIndex, endIndex) : List.of();

        // Convert to response DTOs
        List<BookResponse> content = pageContent.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        // Build paged response
        PagedResponse<BookResponse> response = PagedResponse.<BookResponse>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .hasNextPage(page < totalPages - 1)
                .hasPreviousPage(page > 0)
                .build();

        log.debug("Listed books: page={}, size={}, totalElements={}, totalPages={}",
                page, size, totalElements, totalPages);
        return response;
    }

    public BookResponse getBook(String id) {
        log.debug("Retrieving book with ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        return convertToResponse(book);
    }

    public BookResponse updateBook(String id, UpdateBookRequest request, String userId) {
        log.debug("Updating book with ID: {}, userId: {}", id, userId);

        // Step 1: Retrieve existing book
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        // Step 2: Check authorization (creator only)
        if (!existing.getCreatedBy().equals(userId)) {
            throw new ForbiddenException("Only the creator can update this book");
        }

        // Step 3: Validate update request
        List<FieldError> errors = bookValidator.validateUpdateBookRequest(request);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        // Step 4: Check ISBN uniqueness (if changed, excluding self)
        if (request.getISBN() != null && !request.getISBN().equals(existing.getISBN())) {
            if (bookRepository.findByISBN(request.getISBN()).isPresent()) {
                throw new ConflictException(String.format("Book with ISBN %s already exists", request.getISBN()));
            }
        }

        // Step 5: Update mutable fields only
        if (request.getTitle() != null) {
            existing.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            existing.setAuthor(request.getAuthor());
        }
        if (request.getISBN() != null) {
            existing.setISBN(request.getISBN());
        }
        if (request.getGenre() != null) {
            existing.setGenre(request.getGenre());
        }
        if (request.getPublicationDate() != null) {
            existing.setPublicationDate(request.getPublicationDate());
        }
        // Do NOT update: createdBy, createdAt (immutable fields)

        // Step 6: Persist changes
        Book updated = bookRepository.update(id, existing);

        // Step 7: Return response
        BookResponse response = convertToResponse(updated);
        log.info("Book updated with ID: {}", id);
        return response;
    }

    public void deleteBook(String id, String userId) {
        log.debug("Deleting book with ID: {}, userId: {}", id, userId);

        // Step 1: Retrieve existing book
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found"));

        // Step 2: Check authorization (creator only)
        if (!book.getCreatedBy().equals(userId)) {
            throw new ForbiddenException("Only the creator can delete this book");
        }

        // Step 3: Delete from storage
        bookRepository.delete(id);
        log.info("Book deleted with ID: {}", id);
    }

    // Helper methods

    private BookResponse convertToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .ISBN(book.getISBN())
                .genre(book.getGenre())
                .publicationDate(book.getPublicationDate())
                .createdBy(book.getCreatedBy())
                .createdAt(book.getCreatedAt())
                .build();
    }

    private List<Book> applySort(List<Book> books, String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            sort = "createdAt,desc";
        }

        String[] parts = sort.split(",");
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim().toLowerCase() : "desc";

        Comparator<Book> comparator = getComparator(field);

        if ("asc".equalsIgnoreCase(direction)) {
            return books.stream()
                    .sorted(comparator)
                    .collect(Collectors.toList());
        } else {
            return books.stream()
                    .sorted(comparator.reversed())
                    .collect(Collectors.toList());
        }
    }

    private Comparator<Book> getComparator(String field) {
        return switch (field.toLowerCase()) {
            case "title" -> Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "author" -> Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER);
            case "id" -> Comparator.comparing(Book::getId);
            default -> Comparator.comparing(Book::getCreatedAt); // Default to createdAt
        };
    }
}
