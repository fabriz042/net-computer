import api from "@/app/modules/api.service";
import type { AdminCategory } from "../types/admin-category.type";

export const listAdminCategories = async (): Promise<AdminCategory[]> => {
  const response = await api.get<AdminCategory[]>("/admin/categories");

  return response.data;
};
