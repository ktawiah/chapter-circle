package dev.practice.booksocialnetwork.sentiment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@Primary
@Profile("mongo")
@ConditionalOnProperty(name = "sentiment.engine", havingValue = "simple", matchIfMissing = true)
public class SimpleSentimentService implements SentimentService {

    private static final Set<String> POS = new HashSet<>();
    private static final Set<String> NEG = new HashSet<>();

    static {
        String[] pos = {
                "amazing","awesome","great","love","loved","like","liked","fantastic","excellent","good","wonderful","enjoy","enjoyed","favorite","best","incredible","beautiful","moving","inspiring","fun","funny","hilarious","smart","clever","brilliant","masterpiece"
        };
        String[] neg = {
                "bad","worst","boring","hate","hated","dislike","disliked","terrible","awful","poor","ugly","dull","slow","confusing","predictable","mediocre","flat","forgettable","disappointing","disappointed"
        };
        for (String s : pos) POS.add(s);
        for (String s : neg) NEG.add(s);
    }

    @Override
    public double score(String text) {
        if (text == null || text.isBlank()) return 0.0;
        String[] tokens = text.toLowerCase(Locale.ROOT).split("[^a-z0-9']+");
        int pos = 0, neg = 0;
        for (String t : tokens) {
            if (t.isBlank()) continue;
            if (POS.contains(t)) pos++;
            if (NEG.contains(t)) neg++;
        }
        double total = pos + neg;
        if (total == 0) return 0.0;
        double compound = (pos - neg) / total;
        // clamp
        if (compound > 1.0) compound = 1.0;
        if (compound < -1.0) compound = -1.0;
        return compound;
    }
}
