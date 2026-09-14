package com.epam.book_review_svc.repository;

import com.epam.book_review_svc.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    Book save(Book book);

    Optional<Book> findById(String id);

    Optional<Book> findByISBN(String isbn);

    List<Book> findAll();

    Book update(String id, Book book);

    void delete(String id);

    long count();
}
