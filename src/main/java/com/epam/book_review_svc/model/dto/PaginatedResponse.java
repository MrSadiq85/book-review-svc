package com.epam.book_review_svc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    @JsonProperty("data")
    private List<T> data;

    @JsonProperty("metadata")
    private PaginationMetadata metadata;
}
