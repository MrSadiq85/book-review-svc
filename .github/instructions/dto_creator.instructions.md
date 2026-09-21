---
name: "DTO & POJO Generator"
description: "Coding standards and annotations for model, request, and response DTOs"
applyTo: "src/main/java/com/epam/book_review_svc/model/**/*.java"
---

# DTO & POJO Guidelines

When generating DTOs or POJOs in `src/main/java/com/epam/book_review_svc/model/`, enforce the following:

- **Package:** `com.epam.book_review_svc.model.dto` (or relevant subpackage)
- **Naming:** `*CreateRequestDto`, `*UpdateRequestDto`, `*ResponseDto`
- **Lombok:** `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Jackson:** `@JsonProperty`, `@JsonFormat`
- **Validation:** `@NotBlank`, `@NotNull`, `@Positive`, `@Email` (as needed)