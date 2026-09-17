---
name: dto-generator
description: Generate DTOs with Lombok, Jackson, and Bean Validation annotations
allowed-tools:
  - Write
  - Read
context: main
argument-hint: "Entity name and fields (e.g. Book title:string author:string isbn:string)"
---

## Usage
```
/dto-generator Book title:string author:string isbn:string publicationYear:int
```

## Output
Generates three DTOs in `src/main/java/com/epam/book_review_svc/model/dto/`:
- `BookCreateRequestDto.java`
- `BookUpdateRequestDto.java`
- `BookResponseDto.java`

## Annotations Applied
- **Lombok:** @Data, @Builder, @NoArgsConstructor, @AllArgsConstructor
- **Jackson:** @JsonProperty, @JsonFormat
- **Validation:** @NotBlank, @NotNull, @Positive, @Email (as needed)

All DTOs use package: `com.epam.book_review_svc.model.dto`
