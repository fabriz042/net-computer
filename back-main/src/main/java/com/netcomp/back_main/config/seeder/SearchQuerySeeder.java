package com.netcomp.back_main.config.seeder;

import com.netcomp.back_main.analytics.searchquery.repository.SearchQueryRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Component
public class SearchQuerySeeder {

    private static final Logger log = LoggerFactory.getLogger(SearchQuerySeeder.class);

    private final SearchQueryRepository searchQueryRepository;
    private final JsonMapper jsonMapper;

    public SearchQuerySeeder(SearchQueryRepository searchQueryRepository, JsonMapper jsonMapper) {
        this.searchQueryRepository = searchQueryRepository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public void seed() {
        List<SearchQuerySeedData> searchTerms = readSearchTerms();
        int processedTerms = 0;

        for (SearchQuerySeedData seed : searchTerms) {
            String searchTerm = seed.searchTerm().trim();
            String normalizedQuery = searchTerm.toLowerCase(Locale.ROOT);
            String embedding = seed.embedding() == null ? null : Arrays.toString(seed.embedding());
            processedTerms += searchQueryRepository.upsertSeededSearchQuery(
                    normalizedQuery,
                    searchTerm,
                    seed.searchCount(),
                    embedding
            );
        }

        log.info("[SearchQuerySeeder] Search query seed completed: {} terms processed from {} definitions.",
                processedTerms, searchTerms.size());
    }

    private List<SearchQuerySeedData> readSearchTerms() {
        try (InputStream inputStream = new ClassPathResource("seed/search-queries.json").getInputStream()) {
            return jsonMapper.readValue(inputStream, new TypeReference<List<SearchQuerySeedData>>() {
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read search query seed data from seed/search-queries.json.", exception);
        }
    }

    private record SearchQuerySeedData(String searchTerm, long searchCount, float[] embedding) {
    }
}
