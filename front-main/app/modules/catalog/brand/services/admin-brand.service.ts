import api from "@/app/modules/api.service";
import type { AdminBrand } from "../types/admin-brand.type";

export const listAdminBrands = async (): Promise<AdminBrand[]> => {
  const response = await api.get<AdminBrand[]>("/admin/brands");

  return response.data;
};
