package com.epam.book_review_svc.repository;

import com.epam.book_review_svc.exception.DataAccessException;
import com.epam.book_review_svc.exception.NotFoundException;
import com.epam.book_review_svc.model.Book;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class JsonFileBookRepository implements BookRepository {

    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final String dataFile;

    public JsonFileBookRepository(ObjectMapper objectMapper, ResourceLoader resourceLoader,
                                  @Value("${app.data.file.location:classpath:data/books.json}") String dataFile) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
        this.dataFile = dataFile;
    }

    @Override
    public Book save(Book book) {
        try {
            List<Book> books = loadBooksFromFile();
            books.add(book);
            saveBooksToFile(books);
            log.info("Book saved with ID: {}, Title: {}", book.getId(), book.getTitle());
            return book;
        } catch (IOException e) {
            log.error("Error saving book", e);
            throw new DataAccessException("Failed to save book", e);
        }
    }

    @Override
    public Optional<Book> findById(String id) {
        try {
            List<Book> books = loadBooksFromFile();
            return books.stream()
                    .filter(b -> b.getId().equals(id))
                    .findFirst();
        } catch (IOException e) {
            log.error("Error finding book by ID: {}", id, e);
            throw new DataAccessException("Failed to retrieve book", e);
        }
    }

    @Override
    public Optional<Book> findByISBN(String isbn) {
        try {
            List<Book> books = loadBooksFromFile();
            return books.stream()
                    .filter(b -> b.getISBN().equals(isbn))
                    .findFirst();
        } catch (IOException e) {
            log.error("Error finding book by ISBN: {}", isbn, e);
            throw new DataAccessException("Failed to check ISBN uniqueness", e);
        }
    }

    @Override
    public List<Book> findAll() {
        try {
            return loadBooksFromFile();
        } catch (IOException e) {
            log.error("Error loading all books", e);
            throw new DataAccessException("Failed to load books", e);
        }
    }

    @Override
    public Book update(String id, Book book) {
        try {
            List<Book> books = loadBooksFromFile();

            int index = -1;
            for (int i = 0; i < books.size(); i++) {
                if (books.get(i).getId().equals(id)) {
                    index = i;
                    break;
                }
            }

            if (index == -1) {
                throw new NotFoundException("Book not found for update");
            }

            books.set(index, book);
            saveBooksToFile(books);
            log.info("Book updated with ID: {}", id);
            return book;
        } catch (IOException e) {
            log.error("Error updating book with ID: {}", id, e);
            throw new DataAccessException("Failed to update book", e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            List<Book> books = loadBooksFromFile();

            boolean removed = books.removeIf(b -> b.getId().equals(id));

            if (!removed) {
                throw new NotFoundException("Book not found for deletion");
            }

            saveBooksToFile(books);
            log.info("Book deleted with ID: {}", id);
        } catch (IOException e) {
            log.error("Error deleting book with ID: {}", id, e);
            throw new DataAccessException("Failed to delete book", e);
        }
    }

    @Override
    public long count() {
        try {
            return loadBooksFromFile().size();
        } catch (IOException e) {
            log.error("Error counting books", e);
            throw new DataAccessException("Failed to count books", e);
        }
    }

    // Private helper methods

    private List<Book> loadBooksFromFile() throws IOException {
        try {
            Resource resource = resourceLoader.getResource(dataFile);

            if (!resource.exists()) {
                log.warn("Books file does not exist, returning empty list");
                return new ArrayList<>();
            }

            File file = resource.getFile();
            List<Book> books = Arrays.asList(objectMapper.readValue(file, Book[].class));
            return new ArrayList<>(books);
        } catch (FileNotFoundException e) {
            log.warn("Books file not found, returning empty list");
            return new ArrayList<>();
        }
    }

    private void saveBooksToFile(List<Book> books) throws IOException {
        try {
            Resource resource = resourceLoader.getResource(dataFile);
            File file = resource.getFile();

            // Ensure parent directory exists
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // Write with pretty printing
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, books);

            log.debug("Books persisted to file, total count: {}", books.size());
        } catch (IOException e) {
            log.error("Error writing books to file", e);
            throw e;
        }
    }
}
