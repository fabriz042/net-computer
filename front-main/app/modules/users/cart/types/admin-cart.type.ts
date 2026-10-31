export interface AdminCartListParams {
  page?: number;
  per_page?: number;
  search?: string;
}

export interface AdminCart {
  userId: string;
  userName: string;
  userEmail: string;
  lineCount: number;
  itemCount: number;
  total: number;
  createdAt: string;
  lastActivityAt: string;
}

export interface AdminCartListResponse {
  meta: {
    total_items: number;
    current_page: number;
    per_page: number;
    total_pages: number;
    has_next: boolean;
    has_previous: boolean;
  };
  data: AdminCart[];
}
