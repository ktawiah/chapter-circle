package dev.practice.booksocialnetwork.mongo.model;

import lombok.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Profile("mongo")
public class Book {
    @Id
    private String id;

    // Google Books volume identifier for de-duplication
    private String googleVolumeId;

    private String title;

    private List<String> authors;

    private String isbn10;
    private String isbn13;

    private String publisher;
    private Integer publishedYear;

    private String description;
    private List<String> categories;

    // Optional cover image
    private String coverImageUrl;

    private Instant createdAt;
}
