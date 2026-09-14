package com.epam.book_review_svc.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookRequest {

    private String title;
    private String author;
    private String ISBN;
    private String genre;
    private LocalDate publicationDate;
}
