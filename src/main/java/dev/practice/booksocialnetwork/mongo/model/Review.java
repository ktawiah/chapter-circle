package dev.practice.booksocialnetwork.mongo.model;

import lombok.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Profile("mongo")
public class Review {
    @Id
    private String id;

    private String bookId;

    private String userId;

    private int rating;

    private String text;

    private Double sentimentScore;

    private Instant createdAt;
}
