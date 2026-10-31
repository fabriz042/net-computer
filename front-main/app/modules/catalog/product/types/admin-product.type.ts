import type { CategorySpecificationDefinition } from "@/app/modules/catalog/category/types/admin-category.type";

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

export type AdminProductSpecificationValue = string | number | boolean | null;

export interface AdminProductCreateRequest {
  name: string;
  slug?: string;
  price: number;
  stock: number;
  active: boolean;
  categorySlug: string;
  brandId: string;
  images: AdminProductImageCreateRequest[];
  specifications: Record<string, AdminProductSpecificationValue>;
}

export interface AdminProductUpdateRequest {
  name: string;
  slug?: string;
  price: number;
  stock: number;
  active: boolean;
  categorySlug: string;
  brandId: string | null;
  images: AdminProductImageCreateRequest[];
  specifications: Record<string, AdminProductSpecificationValue>;
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

export interface AdminProductImageDetail {
  imageUrl: string;
  isPrimary: boolean;
  displayOrder: number;
}

export interface AdminProductDetail {
  id: string;
  name: string;
  slug: string;
  price: number;
  stock: number;
  active: boolean;
  brandId: string | null;
  brandName: string | null;
  categoryId: string;
  categoryName: string;
  categorySlug: string;
  categoryActive: boolean;
  specificationSchema: CategorySpecificationDefinition[];
  images: AdminProductImageDetail[];
  specifications: Record<string, unknown> | null;
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
