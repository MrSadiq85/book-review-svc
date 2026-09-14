package com.epam.book_review_svc.controller;

import com.epam.book_review_svc.model.dto.BookResponse;
import com.epam.book_review_svc.model.dto.CreateBookRequest;
import com.epam.book_review_svc.model.dto.PagedResponse;
import com.epam.book_review_svc.model.dto.UpdateBookRequest;
import com.epam.book_review_svc.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Book Catalog Management APIs")
@Slf4j
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @Operation(
            summary = "Create a new book",
            description = "Create a new book in the catalog (authenticated users)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or missing required fields"),
            @ApiResponse(responseCode = "403", description = "User lacks permission to create books"),
            @ApiResponse(responseCode = "409", description = "ISBN already exists")
    })
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody CreateBookRequest request,
            Principal principal) {
        log.info("POST /api/books - Creating new book");

        String userId = principal != null ? principal.getName() : null;
        BookResponse response = bookService.createBook(request, userId);

        return ResponseEntity
                .created(URI.create("/api/books/" + response.getId()))
                .body(response);
    }

    @GetMapping
    @Operation(
            summary = "List books with pagination",
            description = "Retrieve a paginated list of all books in the catalog"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Books retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    public ResponseEntity<PagedResponse<BookResponse>> listBooks(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page (1-100)", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort criteria (field,direction)", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        log.info("GET /api/books - Listing books with page={}, size={}, sort={}", page, size, sort);

        PagedResponse<BookResponse> response = bookService.listBooks(page, size, sort);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get a specific book",
            description = "Retrieve detailed information about a specific book by ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book retrieved successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "400", description = "Invalid book ID format")
    })
    public ResponseEntity<BookResponse> getBook(
            @Parameter(description = "Book ID (UUID or numeric)", example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable String id) {
        log.info("GET /api/books/{} - Retrieving book", id);

        BookResponse response = bookService.getBook(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a book",
            description = "Update a book's metadata (creator only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed or invalid data"),
            @ApiResponse(responseCode = "403", description = "User is not the book creator"),
            @ApiResponse(responseCode = "404", description = "Book not found"),
            @ApiResponse(responseCode = "409", description = "ISBN conflict (already exists)")
    })
    public ResponseEntity<BookResponse> updateBook(
            @Parameter(description = "Book ID")
            @PathVariable String id,

            @Valid @RequestBody UpdateBookRequest request,
            Principal principal) {
        log.info("PUT /api/books/{} - Updating book", id);

        String userId = principal != null ? principal.getName() : null;
        BookResponse response = bookService.updateBook(id, request, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete a book",
            description = "Delete a book from the catalog (creator only)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "403", description = "User is not the book creator"),
            @ApiResponse(responseCode = "404", description = "Book not found")
    })
    public ResponseEntity<Void> deleteBook(
            @Parameter(description = "Book ID")
            @PathVariable String id,
            Principal principal) {
        log.info("DELETE /api/books/{} - Deleting book", id);

        String userId = principal != null ? principal.getName() : null;
        bookService.deleteBook(id, userId);
        return ResponseEntity.noContent().build();
    }
}
