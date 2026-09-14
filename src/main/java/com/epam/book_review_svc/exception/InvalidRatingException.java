package com.epam.book_review_svc.exception;

import org.springframework.http.HttpStatus;

public class InvalidRatingException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.BAD_REQUEST.value();

    public InvalidRatingException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}
