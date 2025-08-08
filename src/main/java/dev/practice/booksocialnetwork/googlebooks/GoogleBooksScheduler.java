package dev.practice.booksocialnetwork.googlebooks;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("mongo")
@RequiredArgsConstructor
public class GoogleBooksScheduler {

    private final GoogleBooksIngestionService ingestionService;

    @Value("${googlebooks.scheduler.enabled:false}")
    private boolean enabled;

    @Value("${googlebooks.scheduler.query:subject:fiction}")
    private String query;

    @Value("${googlebooks.scheduler.batchSize:200}")
    private int batchSize;

    @Scheduled(cron = "${googlebooks.scheduler.cron:0 0 * * * *}")
    public void scheduledIngestion() {
        if (!enabled) return;
        try {
            ingestionService.ingestByQuery(query, batchSize);
        } catch (Exception e) {
            log.warn("Scheduled Google Books ingestion failed", e);
        }
    }
}
