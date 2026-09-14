package com.epam.book_review_svc.repository;

import com.epam.book_review_svc.exception.DataAccessException;
import com.epam.book_review_svc.model.entity.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class ReviewRepository {
    private static final Logger log = LoggerFactory.getLogger(ReviewRepository.class);
    private final ObjectMapper objectMapper;

    public ReviewRepository(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Review> readAllReviews() {
        try {
            Resource resource = new ClassPathResource("data/reviews.json");
            File file = resource.getFile();

            if (!file.exists()) {
                log.warn("Reviews file does not exist. Returning empty list.");
                return new ArrayList<>();
            }

            Review[] reviews = objectMapper.readValue(file, Review[].class);
            return Arrays.asList(reviews);
        } catch (IOException e) {
            log.error("Error reading reviews from file", e);
            throw new DataAccessException("Failed to read reviews from file", e);
        }
    }

    public void writeReviews(List<Review> reviews) {
        try {
            Resource resource = new ClassPathResource("data/reviews.json");
            String filePath = resource.getFile().getAbsolutePath();

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(filePath), reviews);
            log.info("Successfully wrote {} reviews to file", reviews.size());
        } catch (IOException e) {
            log.error("Error writing reviews to file", e);
            throw new DataAccessException("Failed to write reviews to file", e);
        }
    }
}
