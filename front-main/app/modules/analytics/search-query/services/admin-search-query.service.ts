import api from "@/app/modules/api.service";
import type {
  AdminSearchQuery,
  SearchQueryEmbeddingGenerationResponse,
} from "@/app/modules/analytics/search-query/types/admin-search-query.type";

export const listAdminSearchQueries = async (): Promise<AdminSearchQuery[]> => {
  const response = await api.get<AdminSearchQuery[]>("/admin/search-queries");
  return response.data;
};

export const generateAdminSearchQueryEmbeddings = async (): Promise<SearchQueryEmbeddingGenerationResponse> => {
  const response = await api.post<SearchQueryEmbeddingGenerationResponse>(
    "/admin/search-queries/embeddings",
    undefined,
    { timeout: 120_000 }
  );
  return response.data;
};
