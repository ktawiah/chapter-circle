package dev.practice.booksocialnetwork.mongo.repository;

import dev.practice.booksocialnetwork.mongo.model.Book;
import org.springframework.context.annotation.Profile;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@Profile("mongo")
public interface BookRepository extends CrudRepository<Book, String> {
    List<Book> findByTitleContainingIgnoreCase(String q);
    Book findByGoogleVolumeId(String googleVolumeId);
    Book findByIsbn13(String isbn13);
    List<Book> findByCategoriesContaining(String category);
}
