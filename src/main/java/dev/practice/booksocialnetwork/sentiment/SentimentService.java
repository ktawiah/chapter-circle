package dev.practice.booksocialnetwork.sentiment;

public interface SentimentService {
    // Returns sentiment compound score in range [-1.0, 1.0].
    double score(String text);
}
