package com.epam.book_review_svc.exception;

public class InvalidSortException extends RuntimeException {
    public InvalidSortException(String message) {
        super(message);
    }
}
