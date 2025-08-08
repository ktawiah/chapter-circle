package dev.practice.booksocialnetwork.mongo.repository;

import dev.practice.booksocialnetwork.mongo.model.Review;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@Profile("mongo")
public interface ReviewRepository extends CrudRepository<Review, String> {
    List<Review> findByBookId(String bookId);
    List<Review> findByUserId(String userId);
}
