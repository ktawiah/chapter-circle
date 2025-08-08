package dev.practice.booksocialnetwork.sentiment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@Profile("mongo")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sentiment.engine", havingValue = "nltk")
public class NltkSentimentService implements SentimentService {

    @Value("${sentiment.nltk.url:http://localhost:5001/sentiment}")
    private String nltkUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public double score(String text) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("text", text == null ? "" : text), headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(nltkUrl, entity, Map.class);
            Object compound = response.getBody() != null ? response.getBody().get("compound") : null;
            if (compound instanceof Number n) return n.doubleValue();
        } catch (Exception e) {
            log.warn("NLTK sentiment service call failed, fallback to neutral", e);
        }
        return 0.0;
    }
}
