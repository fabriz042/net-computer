package com.netcomp.back_main.analytics.searchquery.repository;

import com.netcomp.back_main.analytics.searchquery.domain.SearchQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SearchQueryRepository extends JpaRepository<SearchQuery, String> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Transactional
    @Query(value = """
            INSERT INTO search_queries (
                normalized_query,
                search_term,
                search_count,
                searches_with_click,
                first_searched_at,
                last_searched_at,
                embedding
            ) VALUES (
                :normalizedQuery,
                :searchTerm,
                :searchCount,
                0,
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP,
                CAST(:embedding AS vector(1024))
            )
            ON CONFLICT (normalized_query) DO UPDATE
            SET search_term = EXCLUDED.search_term,
                embedding = COALESCE(search_queries.embedding, EXCLUDED.embedding)
            """, nativeQuery = true)
    int upsertSeededSearchQuery(
            @Param("normalizedQuery") String normalizedQuery,
            @Param("searchTerm") String searchTerm,
            @Param("searchCount") long searchCount,
            @Param("embedding") String embedding
    );

    List<SearchQuery> findTop20ByOrderBySearchCountDescLastSearchedAtDesc();

    List<SearchQuery> findTop20ByEmbeddingIsNullOrderBySearchCountDescLastSearchedAtDesc();
}
