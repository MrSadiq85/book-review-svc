package com.epam.book_review_svc.controller;

import com.epam.book_review_svc.controller.BookController;
import com.epam.book_review_svc.exception.GlobalExceptionHandler;
import com.epam.book_review_svc.model.dto.BookRequestDto;
import com.epam.book_review_svc.repository.JsonFileBookRepository;
import com.epam.book_review_svc.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookControllerTest {

    private final Path storePath = Path.of(System.getProperty("java.io.tmpdir"), "book-controller-test-" + UUID.randomUUID() + ".json");

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private JsonFileBookRepository repository;
    private BookService bookService;

    @BeforeEach
    void setUp() throws Exception {
        Files.deleteIfExists(storePath);
        Files.writeString(storePath, "[]");

        repository = new JsonFileBookRepository(storePath.toString());
        bookService = new BookService(repository, Validation.buildDefaultValidatorFactory().getValidator());
        BookController bookController = new BookController(bookService);
        objectMapper = new ObjectMapper();
        this.mockMvc = MockMvcBuilders.standaloneSetup(bookController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void getBooksReturnsEmptyListInitially() throws Exception {
        mockMvc.perform(get("/api/v1/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    void postBookCreatesAndPersistsBook() throws Exception {
        BookRequestDto request = BookRequestDto.builder()
            .isbn("978-0134685991")
            .title("  Effective Java  ")
            .author("  Joshua Bloch  ")
            .build();

        String location = mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isString())
            .andExpect(jsonPath("$.isbn").value("9780134685991"))
            .andExpect(jsonPath("$.title").value("Effective Java"))
            .andExpect(jsonPath("$.author").value("Joshua Bloch"))
            .andReturn()
            .getResponse()
            .getHeader("Location");

        mockMvc.perform(get(location))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isbn").value("9780134685991"));
    }

    @Test
    void postBookRejectsInvalidRequest() throws Exception {
        BookRequestDto request = BookRequestDto.builder()
            .isbn("not-an-isbn")
            .title("Effective Java")
            .author("Joshua Bloch")
            .build();

        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void postBookRejectsDuplicateIsbn() throws Exception {
        repository.create(new com.epam.book_review_svc.model.Book(
            "book-123", "9780134685991", "Effective Java", "Joshua Bloch"));

        BookRequestDto request = BookRequestDto.builder()
            .isbn("978-0134685991")
            .title("Another Book")
            .author("Another Author")
            .build();

        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("DUPLICATE_ISBN"));
    }

    @Test
    void putAndDeleteBookUsesExpectedEndpoints() throws Exception {
        repository.replace("book-123", new com.epam.book_review_svc.model.Book("book-123", "9780134685991", "Old Book", "Old Author"));

        BookRequestDto request = BookRequestDto.builder()
            .isbn("9780134685991")
            .title("Effective Java")
            .author("Joshua Bloch")
            .build();

        mockMvc.perform(put("/api/v1/books/book-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("book-123"))
            .andExpect(jsonPath("$.isbn").value("9780134685991"));

        mockMvc.perform(get("/api/v1/books/book-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Effective Java"));

        mockMvc.perform(delete("/api/v1/books/book-123"))
            .andExpect(status().isNoContent());
    }

    @Test
    void returns404ForMissingBook() throws Exception {
        mockMvc.perform(get("/api/v1/books/missing-id"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("BOOK_NOT_FOUND"));
    }
}
