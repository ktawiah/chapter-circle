package dev.practice.booksocialnetwork.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private LocalDateTime validateAt;

    @NotNull
    @ManyToOne
    private User user;
}
