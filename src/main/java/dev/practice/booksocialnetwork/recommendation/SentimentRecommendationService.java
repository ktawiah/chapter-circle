package dev.practice.booksocialnetwork.recommendation;

import dev.practice.booksocialnetwork.mongo.model.Book;
import dev.practice.booksocialnetwork.mongo.model.Review;
import dev.practice.booksocialnetwork.mongo.repository.BookRepository;
import dev.practice.booksocialnetwork.mongo.repository.ReviewRepository;
import dev.practice.booksocialnetwork.sentiment.SentimentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Profile("mongo")
@RequiredArgsConstructor
public class SentimentRecommendationService {

    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final SentimentService sentimentService;

    public List<Book> recommendForUser(String userId, int limit) {
        if (userId == null || userId.isBlank()) return List.of();
        List<Review> reviews = reviewRepository.findByUserId(userId);
        if (reviews == null || reviews.isEmpty()) return List.of();

        Map<String, Double> categoryScore = new HashMap<>();
        Set<String> reviewed = new HashSet<>();

        for (Review r : reviews) {
            if (r == null) continue;
            reviewed.add(r.getBookId());
            double score = r.getSentimentScore() != null ? r.getSentimentScore() : 0.0;
            if ((r.getText() != null) && (r.getSentimentScore() == null)) {
                score = sentimentService.score(r.getText());
            }
            if (score < 0.1) continue; // consider only positive leaning reviews
            double ratingFactor = Math.max(0, Math.min(5, r.getRating())) / 5.0; // 0..1
            double weight = score * (0.5 + 0.5 * ratingFactor);
            bookRepository.findById(r.getBookId()).ifPresent(b -> {
                if (b.getCategories() != null) {
                    for (String c : b.getCategories()) {
                        if (c == null || c.isBlank()) continue;
                        categoryScore.merge(c, weight, Double::sum);
                    }
                }
            });
        }

        if (categoryScore.isEmpty()) return List.of();
        // rank categories
        List<String> topCategories = categoryScore.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        List<Book> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (String cat : topCategories) {
            if (results.size() >= limit) break;
            List<Book> inCat = bookRepository.findByCategoriesContaining(cat);
            for (Book b : inCat) {
                if (b == null || seen.contains(b.getId())) continue;
                if (reviewed.contains(b.getId())) continue;
                results.add(b);
                seen.add(b.getId());
                if (results.size() >= limit) break;
            }
        }
        return results;
    }
}
