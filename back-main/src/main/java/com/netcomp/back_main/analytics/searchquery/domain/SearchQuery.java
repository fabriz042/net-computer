package com.netcomp.back_main.analytics.searchquery.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "search_queries")
public class SearchQuery {

    @Id
    @Column(name = "normalized_query", nullable = false, length = 100)
    private String normalizedQuery;

    @Column(name = "search_term", nullable = false, length = 100)
    private String searchTerm;

    @Column(name = "search_count", nullable = false)
    private long searchCount;

    @Column(name = "searches_with_click", nullable = false, columnDefinition = "bigint default 0")
    private long searchesWithClick;

    @Column(name = "products_found", nullable = false, columnDefinition = "bigint default 0")
    private long productsFound;

    @Column(name = "first_searched_at", nullable = false)
    private Instant firstSearchedAt;

    @Column(name = "last_searched_at", nullable = false)
    private Instant lastSearchedAt;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 1024)
    @Column(columnDefinition = "vector(1024)")
    private float[] embedding;

    public SearchQuery() {
    }

    public String getNormalizedQuery() {
        return normalizedQuery;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public long getSearchCount() {
        return searchCount;
    }

    public long getSearchesWithClick() {
        return searchesWithClick;
    }

    public long getProductsFound() {
        return productsFound;
    }

    public Instant getFirstSearchedAt() {
        return firstSearchedAt;
    }

    public Instant getLastSearchedAt() {
        return lastSearchedAt;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}
