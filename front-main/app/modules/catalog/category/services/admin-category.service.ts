import api from "@/app/modules/api.service";
import type {
  AdminCategory,
  AdminCategoryListParams,
  AdminCategoryRequest,
} from "../types/admin-category.type";

export const listAdminCategories = async (
  params: AdminCategoryListParams = {}
): Promise<AdminCategory[]> => {
  const response = await api.get<AdminCategory[]>("/admin/categories", {
    params,
  });

  return response.data;
};

export const createAdminCategory = async (
  request: AdminCategoryRequest
): Promise<AdminCategory> => {
  const response = await api.post<AdminCategory>("/admin/categories", request);

  return response.data;
};

export const updateAdminCategory = async (
  id: string,
  request: AdminCategoryRequest
): Promise<AdminCategory> => {
  const response = await api.put<AdminCategory>(
    `/admin/categories/${id}`,
    request
  );

  return response.data;
};

export const deleteAdminCategory = async (id: string): Promise<void> => {
  await api.delete(`/admin/categories/${id}`);
};
