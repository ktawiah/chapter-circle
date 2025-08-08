package dev.practice.booksocialnetwork.googlebooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Lightweight client for Google Books API. No external dependencies besides Spring Web.
 */
@Slf4j
@Component
@Profile("mongo")
@RequiredArgsConstructor
public class GoogleBooksClient {

    @Value("${googlebooks.baseUrl:https://www.googleapis.com/books/v1}")
    private String baseUrl;

    @Value("${googlebooks.apiKey:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public VolumeResponse searchVolumes(String query, int startIndex, int maxResults) {
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            StringBuilder url = new StringBuilder(baseUrl)
                    .append("/volumes?q=").append(encodedQuery)
                    .append("&startIndex=").append(Math.max(0, startIndex))
                    .append("&maxResults=").append(Math.min(Math.max(1, maxResults), 40));
            if (apiKey != null && !apiKey.isBlank()) {
                url.append("&key=").append(apiKey);
            }
            URI uri = URI.create(url.toString());
            ResponseEntity<VolumeResponse> response = restTemplate.getForEntity(uri, VolumeResponse.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed calling Google Books API", e);
            return new VolumeResponse();
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeResponse {
        private int totalItems;
        private VolumeItem[] items;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeItem {
        private String id; // volumeId
        private VolumeInfo volumeInfo;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VolumeInfo {
        private String title;
        private String[] authors;
        private String publisher;
        private String publishedDate;
        private String description;
        private String[] categories;
        private IndustryIdentifier[] industryIdentifiers;
        private ImageLinks imageLinks;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IndustryIdentifier {
        private String type; // ISBN_10 or ISBN_13
        private String identifier;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageLinks {
        private String smallThumbnail;
        private String thumbnail;
    }
}
