package com.epam.book_review_svc.exception;

public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getStatusCode() {
        return 403;
    }
}
