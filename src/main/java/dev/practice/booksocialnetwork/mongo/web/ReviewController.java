package dev.practice.booksocialnetwork.mongo.web;

import dev.practice.booksocialnetwork.mongo.model.Review;
import dev.practice.booksocialnetwork.mongo.repository.ReviewRepository;
import dev.practice.booksocialnetwork.sentiment.SentimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Profile("mongo")
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final SentimentService sentimentService;

    @GetMapping
    public List<Review> list(@RequestParam("bookId") String bookId) {
        return reviewRepository.findByBookId(bookId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> get(@PathVariable String id) {
        return reviewRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Review> create(@RequestBody Review review) {
        if (review.getText() != null) {
            double score = sentimentService.score(review.getText());
            review.setSentimentScore(score);
        }
        if (review.getCreatedAt() == null) review.setCreatedAt(Instant.now());
        Review saved = reviewRepository.save(review);
        return ResponseEntity.created(URI.create("/reviews/" + saved.getId())).body(saved);
    }
}
