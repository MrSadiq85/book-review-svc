package com.epam.book_review_svc.exception;

import org.springframework.http.HttpStatus;

public class DataAccessException extends BusinessException {
    private static final int HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public int getHttpStatusCode() {
        return HTTP_STATUS;
    }
}

