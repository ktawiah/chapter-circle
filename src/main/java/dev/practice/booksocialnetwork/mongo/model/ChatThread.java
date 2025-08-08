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
public class ChatThread {
    @Id
    private String id;

    private String name;

    private List<String> participantUserIds;

    private Instant createdAt;
}
