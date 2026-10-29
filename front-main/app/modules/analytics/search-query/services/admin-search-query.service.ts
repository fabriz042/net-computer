import api from "@/app/modules/api.service";
import type {
  AdminSearchQuery,
  AdminSearchQueryEmbeddingResponse,
} from "@/app/modules/analytics/search-query/types/admin-search-query.type";

export const listAdminSearchQueries = async (): Promise<AdminSearchQuery[]> => {
  const response = await api.get<AdminSearchQuery[]>("/admin/search-queries");
  return response.data;
};

export const generateAdminSearchQueryEmbeddings = async (): Promise<AdminSearchQueryEmbeddingResponse> => {
  const response = await api.post<AdminSearchQueryEmbeddingResponse>(
    "/admin/search-queries/embeddings",
    undefined,
    { timeout: 120_000 }
  );
  return response.data;
};
