import api from "@/app/modules/api.service";
import type { PublicProduct } from "@/app/modules/catalog/product/types/public-product.type";

export const listPublicRecommendations = async (
  productId: string
): Promise<PublicProduct[]> => {
  const response = await api.get<PublicProduct[]>(`/products/${productId}/recommendations`);

  return response.data;
};
