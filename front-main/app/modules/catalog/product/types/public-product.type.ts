export type PublicProductOrder = "asc" | "desc";

export interface PublicProductListParams {
  order?: PublicProductOrder;
  page?: number;
  per_page?: number;
  search?: string;
  category_slug?: string;
}

export interface PublicProduct {
  id: string;
  name: string;
  slug: string;
  price: number;
  stock: number;
  active: boolean;
  brandId: string | null;
  brandSlug: string | null;
  brandName: string | null;
  categoryName: string | null;
  categorySlug: string | null;
  primaryImageUrl: string | null;
  images: string[];
  specifications: Record<string, unknown> | null;
  createdAt: string;
}

export interface PublicProductListResponse {
  meta: {
    total_items: number;
    current_page: number;
    per_page: number;
    total_pages: number;
    has_next: boolean;
    has_previous: boolean;
  };
  data: PublicProduct[];
}
