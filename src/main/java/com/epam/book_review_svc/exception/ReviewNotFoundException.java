package com.epam.book_review_svc.exception;

import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.NOT_FOUND.value();

    public ReviewNotFoundException(String message) {
        super(message);
    }

    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}
