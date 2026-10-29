export type AdminProductOrder = "asc" | "desc";

export interface AdminProductListParams {
  order?: AdminProductOrder;
  page?: number;
  per_page?: number;
  search?: string;
}

export interface AdminProductImageCreateRequest {
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface AdminProductCreateRequest {
  name: string;
  slug?: string;
  price: number;
  stock: number;
  active: boolean;
  categorySlug: string;
  brandId: string;
  images: AdminProductImageCreateRequest[];
  specifications: Record<string, string | number | boolean>;
}

export interface AdminProduct {
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
  hasEmbedding: boolean;
  createdAt: string;
}

export interface AdminProductListResponse {
  meta: {
    total_items: number;
    current_page: number;
    per_page: number;
    total_pages: number;
    has_next: boolean;
    has_previous: boolean;
  };
  data: AdminProduct[];
}

export interface AdminProductEmbeddingResponse {
  generatedCount: number;
}
