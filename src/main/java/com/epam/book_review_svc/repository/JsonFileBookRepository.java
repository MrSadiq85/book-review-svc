package com.epam.book_review_svc.repository;

import com.epam.book_review_svc.exception.BookNotFoundException;
import com.epam.book_review_svc.model.Book;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class JsonFileBookRepository implements BookRepository {

    private final Path storePath;
    private final ObjectMapper objectMapper;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile List<Book> cache = List.of();

    public JsonFileBookRepository(@Value("${book.storage.path:./data/books.json}") String storePath) {
        this.storePath = Paths.get(storePath);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        initializeStore();
    }

    @Override
    public List<Book> findAll() {
        lock.readLock().lock();
        try {
            return List.copyOf(cache);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Book> findById(String id) {
        lock.readLock().lock();
        try {
            return cache.stream()
                .filter(book -> id != null && id.equals(book.getId()))
                .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Book create(Book book) {
        lock.writeLock().lock();
        try {
            List<Book> current = new ArrayList<>(cache);
            current.add(book);
            try {
                writeSnapshot(current);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to persist books to " + storePath, ex);
            }
            cache = List.copyOf(current);
            return book;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Book replace(String id, Book updatedBook) {
        lock.writeLock().lock();
        try {
            List<Book> current = new ArrayList<>(cache);
            int index = -1;
            for (int i = 0; i < current.size(); i++) {
                if (id.equals(current.get(i).getId())) {
                    index = i;
                    break;
                }
            }
            if (index >= 0) {
                current.set(index, updatedBook);
            } else {
                current.add(updatedBook);
            }
            try {
                writeSnapshot(current);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to persist books to " + storePath, ex);
            }
            cache = List.copyOf(current);
            return updatedBook;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Book delete(String id) {
        lock.writeLock().lock();
        try {
            List<Book> current = new ArrayList<>(cache);
            Book existing = current.stream()
                .filter(book -> id.equals(book.getId()))
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));

            List<Book> updated = current.stream()
                .filter(book -> !id.equals(book.getId()))
                .toList();

            try {
                writeSnapshot(updated);
            } catch (IOException ex) {
                throw new IllegalStateException("Unable to persist books to " + storePath, ex);
            }
            cache = List.copyOf(updated);
            return existing;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void initializeStore() {
        try {
            Path parent = storePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (Files.notExists(storePath)) {
                writeSnapshot(List.of());
                return;
            }

            cache = readSnapshot();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to initialize book store at " + storePath, ex);
        }
    }

    private List<Book> readSnapshot() throws IOException {
        byte[] content = Files.readAllBytes(storePath);
        if (content == null || content.length == 0) {
            return List.of();
        }

        List<Book> books = objectMapper.readValue(content, new TypeReference<List<Book>>() {});
        return books == null ? List.of() : books;
    }

    private void writeSnapshot(List<Book> books) throws IOException {
        Path parent = storePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path tempFile = Files.createTempFile(parent == null ? Paths.get(".") : parent, "books-", ".tmp");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(tempFile.toFile(), books);
            try {
                Files.move(tempFile, storePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempFile, storePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
