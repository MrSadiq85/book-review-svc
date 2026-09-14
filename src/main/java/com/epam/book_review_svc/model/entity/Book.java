package com.epam.book_review_svc.model.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private Integer publicationYear;
    private String genre;
}
