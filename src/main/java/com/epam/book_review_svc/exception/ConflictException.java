package com.epam.book_review_svc.exception;

public class ConflictException extends ApiException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return 409;
    }
}
