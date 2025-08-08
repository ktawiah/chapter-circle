package dev.practice.booksocialnetwork.googlebooks;

import dev.practice.booksocialnetwork.googlebooks.GoogleBooksClient.VolumeItem;
import dev.practice.booksocialnetwork.googlebooks.GoogleBooksClient.VolumeResponse;
import dev.practice.booksocialnetwork.mongo.model.Book;
import dev.practice.booksocialnetwork.mongo.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("mongo")
@RequiredArgsConstructor
public class GoogleBooksIngestionService {

    private final GoogleBooksClient client;
    private final BookRepository bookRepository;

    public List<Book> searchGoogle(String query, int maxResults) {
        int pageSize = Math.min(40, Math.max(1, maxResults));
        VolumeResponse resp = client.searchVolumes(query, 0, pageSize);
        return mapItems(resp);
    }

    public IngestionResult ingestByQuery(String query, int maxToIngest) {
        int startIndex = 0;
        int pageSize = 40;
        int ingested = 0;
        int updated = 0;
        Set<String> seenVolumeIds = new HashSet<>();
        while (ingested + updated < maxToIngest) {
            int remaining = maxToIngest - (ingested + updated);
            int fetch = Math.min(pageSize, Math.max(1, remaining));
            VolumeResponse resp = client.searchVolumes(query, startIndex, fetch);
            if (resp == null || resp.getItems() == null || resp.getItems().length == 0) {
                break;
            }
            for (VolumeItem item : resp.getItems()) {
                if (item == null || item.getVolumeInfo() == null) continue;
                String volumeId = item.getId();
                if (volumeId != null && !seenVolumeIds.add(volumeId)) {
                    continue;
                }
                Book mapped = map(item);
                if (mapped == null) continue;
                boolean savedOrUpdated = saveOrUpdate(mapped);
                if (savedOrUpdated) {
                    ingested++;
                } else {
                    updated++;
                }
                if (ingested + updated >= maxToIngest) break;
            }
            startIndex += fetch;
        }
        return new IngestionResult(ingested, updated);
    }

    private boolean saveOrUpdate(Book incoming) {
        // Prefer matching by googleVolumeId, then ISBN13
        Optional<Book> byVolume = (incoming.getGoogleVolumeId() != null)
                ? findByGoogleVolumeId(incoming.getGoogleVolumeId())
                : Optional.empty();
        Optional<Book> byIsbn13 = (incoming.getIsbn13() != null && byVolume.isEmpty())
                ? findByIsbn13(incoming.getIsbn13())
                : Optional.empty();

        Book toSave = byVolume.or(() -> byIsbn13).orElse(null);
        if (toSave == null) {
            if (incoming.getCreatedAt() == null) incoming.setCreatedAt(Instant.now());
            bookRepository.save(incoming);
            return true; // inserted
        }
        // Merge missing fields
        boolean changed = false;
        if (toSave.getGoogleVolumeId() == null && incoming.getGoogleVolumeId() != null) { toSave.setGoogleVolumeId(incoming.getGoogleVolumeId()); changed = true; }
        if ((toSave.getTitle() == null || toSave.getTitle().isBlank()) && incoming.getTitle() != null) { toSave.setTitle(incoming.getTitle()); changed = true; }
        if ((toSave.getAuthors() == null || toSave.getAuthors().isEmpty()) && incoming.getAuthors() != null) { toSave.setAuthors(incoming.getAuthors()); changed = true; }
        if (toSave.getIsbn10() == null && incoming.getIsbn10() != null) { toSave.setIsbn10(incoming.getIsbn10()); changed = true; }
        if (toSave.getIsbn13() == null && incoming.getIsbn13() != null) { toSave.setIsbn13(incoming.getIsbn13()); changed = true; }
        if (toSave.getPublisher() == null && incoming.getPublisher() != null) { toSave.setPublisher(incoming.getPublisher()); changed = true; }
        if (toSave.getPublishedYear() == null && incoming.getPublishedYear() != null) { toSave.setPublishedYear(incoming.getPublishedYear()); changed = true; }
        if (toSave.getDescription() == null && incoming.getDescription() != null) { toSave.setDescription(incoming.getDescription()); changed = true; }
        if ((toSave.getCategories() == null || toSave.getCategories().isEmpty()) && incoming.getCategories() != null) { toSave.setCategories(incoming.getCategories()); changed = true; }
        if (toSave.getCoverImageUrl() == null && incoming.getCoverImageUrl() != null) { toSave.setCoverImageUrl(incoming.getCoverImageUrl()); changed = true; }
        if (changed) {
            bookRepository.save(toSave);
        }
        return false;
    }

    private Optional<Book> findByGoogleVolumeId(String volumeId) {
        try {
            return Optional.ofNullable(bookRepository.findByGoogleVolumeId(volumeId));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Book> findByIsbn13(String isbn13) {
        try {
            return Optional.ofNullable(bookRepository.findByIsbn13(isbn13));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private List<Book> mapItems(VolumeResponse resp) {
        if (resp == null || resp.getItems() == null) return List.of();
        return Arrays.stream(resp.getItems())
                .filter(Objects::nonNull)
                .map(this::map)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Book map(VolumeItem item) {
        if (item == null || item.getVolumeInfo() == null) return null;
        var info = item.getVolumeInfo();
        Book.BookBuilder b = Book.builder();
        b.googleVolumeId(item.getId());
        b.title(info.getTitle());
        if (info.getAuthors() != null) {
            b.authors(Arrays.asList(info.getAuthors()));
        }
        if (info.getIndustryIdentifiers() != null) {
            for (var id : info.getIndustryIdentifiers()) {
                if (id == null) continue;
                if ("ISBN_10".equalsIgnoreCase(id.getType())) b.isbn10(cleanIsbn(id.getIdentifier()));
                if ("ISBN_13".equalsIgnoreCase(id.getType())) b.isbn13(cleanIsbn(id.getIdentifier()));
            }
        }
        b.publisher(info.getPublisher());
        Integer year = parseYear(info.getPublishedDate());
        b.publishedYear(year);
        b.description(info.getDescription());
        if (info.getCategories() != null) {
            b.categories(Arrays.asList(info.getCategories()));
        }
        if (info.getImageLinks() != null) {
            String cover = info.getImageLinks().getThumbnail();
            if (cover == null) cover = info.getImageLinks().getSmallThumbnail();
            b.coverImageUrl(cover);
        }
        b.createdAt(Instant.now());
        return b.build();
    }

    private Integer parseYear(String publishedDate) {
        if (publishedDate == null || publishedDate.isBlank()) return null;
        // Google may return YYYY-MM-DD or YYYY
        try {
            if (publishedDate.length() >= 4) {
                return Integer.parseInt(publishedDate.substring(0, 4));
            }
        } catch (NumberFormatException ignored) {}
        return null;
    }

    private String cleanIsbn(String s) {
        if (s == null) return null;
        return s.replaceAll("[^0-9Xx]", "");
    }

    public record IngestionResult(int inserted, int updated) {}
}
