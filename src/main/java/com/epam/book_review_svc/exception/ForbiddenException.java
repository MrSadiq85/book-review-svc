package com.epam.book_review_svc.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.FORBIDDEN.value();

    public ForbiddenException(String message) {
        super(message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}
