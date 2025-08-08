package dev.practice.booksocialnetwork.recommendation;

import dev.practice.booksocialnetwork.mongo.model.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@Profile("mongo")
public class RecommendationController {

    private final SentimentRecommendationService recommendationService;

    @GetMapping("/sentiment")
    public List<Book> recommendBySentiment(@RequestParam("userId") String userId,
                                           @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return recommendationService.recommendForUser(userId, Math.max(1, Math.min(100, limit)));
    }
}
