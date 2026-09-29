package com.epam.book_review_svc.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"id", "title", "author", "ISBN", "genre", "publicationDate", "createdBy", "createdAt"})
public class Book {

    @JsonProperty("id")
    private String id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("author")
    private String author;

    @JsonProperty("ISBN")
    private String ISBN;

    @JsonProperty("genre")
    private String genre;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("publicationDate")
    private LocalDate publicationDate;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;
}
