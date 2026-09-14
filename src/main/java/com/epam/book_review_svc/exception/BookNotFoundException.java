package com.epam.book_review_svc.exception;

import org.springframework.http.HttpStatus;

public class BookNotFoundException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.BAD_REQUEST.value();

    public BookNotFoundException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}
