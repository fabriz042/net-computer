import api from "@/app/modules/api.service";
import type {
  AdminProduct,
  AdminProductCreateRequest,
  AdminProductListParams,
  AdminProductListResponse,
  AdminProductEmbeddingResponse,
} from "../types/admin-product.type";

export const createAdminProduct = async (
  request: AdminProductCreateRequest
): Promise<AdminProduct> => {
  const response = await api.post<AdminProduct>("/admin/products", request);

  return response.data;
};

export const listAdminProducts = async (
  params: AdminProductListParams = {}
): Promise<AdminProductListResponse> => {
  const response = await api.get<AdminProductListResponse>("/admin/products", {
    params,
  });

  return response.data;
};

export const generateAdminProductEmbeddings = async (): Promise<AdminProductEmbeddingResponse> => {
  const response = await api.post<AdminProductEmbeddingResponse>(
    "/admin/products/embeddings",
    null
  );

  return response.data;
};
