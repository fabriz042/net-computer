export interface AdminBrand {
  id: string;
  slug: string;
  name: string;
}

export interface AdminBrandRequest {
  name: string;
  slug?: string;
}
