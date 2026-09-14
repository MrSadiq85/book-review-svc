package com.epam.book_review_svc.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Review {
    private String id;
    private String bookId;
    private String userId;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
