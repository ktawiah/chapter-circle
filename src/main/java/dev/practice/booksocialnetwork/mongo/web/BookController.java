package dev.practice.booksocialnetwork.mongo.web;

import dev.practice.booksocialnetwork.googlebooks.GoogleBooksIngestionService;
import dev.practice.booksocialnetwork.mongo.model.Book;
import dev.practice.booksocialnetwork.mongo.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Profile("mongo")
public class BookController {

    private final BookRepository bookRepository;
    private final GoogleBooksIngestionService ingestionService;

    @GetMapping
    public List<Book> list(@RequestParam(value = "q", required = false) String q) {
        if (q == null || q.isBlank()) {
            Iterable<Book> all = bookRepository.findAll();
            java.util.List<Book> list = new java.util.ArrayList<>();
            for (Book b : all) { list.add(b); }
            return list;
        }
        return bookRepository.findByTitleContainingIgnoreCase(q);
    }

    @GetMapping("/search/google")
    public List<Book> searchFromGoogle(@RequestParam("q") String q,
                                       @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ingestionService.searchGoogle(q, limit);
    }

    @PostMapping("/ingest/google")
    public GoogleBooksIngestionService.IngestionResult ingestFromGoogle(@RequestParam("query") String query,
                                                                        @RequestParam(value = "max", defaultValue = "100") int max) {
        return ingestionService.ingestByQuery(query, max);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> get(@PathVariable String id) {
        return bookRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> create(@RequestBody Book book) {
        Book saved = bookRepository.save(book);
        return ResponseEntity.created(URI.create("/books/" + saved.getId())).body(saved);
    }
}
