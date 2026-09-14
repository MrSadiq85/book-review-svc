package com.epam.book_review_svc.exception;

import com.epam.book_review_svc.model.dto.FieldError;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidationException extends ApiException {

    private final List<FieldError> fieldErrors;

    public ValidationException(String message, List<FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    @Override
    public int getStatusCode() {
        return 400;
    }
}
