export interface AdminSearchQuery {
  searchTerm: string;
  searchCount: number;
  searchesWithClick: number;
  productsFound: number;
  clickThroughRate: number;
  hasEmbedding: boolean;
}

export interface SearchQueryEmbeddingGenerationResponse {
  generatedCount: number;
}
