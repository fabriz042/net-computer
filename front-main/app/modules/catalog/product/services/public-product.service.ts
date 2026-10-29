import api from "@/app/modules/api.service";
import type {
  PublicProduct,
  PublicProductListParams,
  PublicProductListResponse,
} from "../types/public-product.type";

export const getPublicProductBySlug = async (slug: string): Promise<PublicProduct> => {
  const response = await api.get<PublicProduct>(`/products/${encodeURIComponent(slug)}`);

  return response.data;
};

export const listPublicProducts = async (
  params: PublicProductListParams = {}
): Promise<PublicProductListResponse> => {
  const response = await api.get<PublicProductListResponse>("/products", {
    params,
  });

  return response.data;
};
