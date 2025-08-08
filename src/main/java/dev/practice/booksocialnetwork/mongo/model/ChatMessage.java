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
public class ChatMessage {
    @Id
    private String id;

    private String threadId;

    private String senderUserId;

    private String content;

    private Instant sentAt;
}
