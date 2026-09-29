package com.epam.book_review_svc.repository;

import com.epam.book_review_svc.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    List<Book> findAll();

    Optional<Book> findById(String id);

    Book create(Book book);

    Book replace(String id, Book updatedBook);

    Book delete(String id);
}
