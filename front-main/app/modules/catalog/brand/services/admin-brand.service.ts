import api from "@/app/modules/api.service";
import type {
  AdminBrand,
  AdminBrandRequest,
} from "../types/admin-brand.type";

export const listAdminBrands = async (): Promise<AdminBrand[]> => {
  const response = await api.get<AdminBrand[]>("/admin/brands");

  return response.data;
};

export const createAdminBrand = async (
  request: AdminBrandRequest
): Promise<AdminBrand> => {
  const response = await api.post<AdminBrand>("/admin/brands", request);

  return response.data;
};

export const updateAdminBrand = async (
  id: string,
  request: AdminBrandRequest
): Promise<AdminBrand> => {
  const response = await api.put<AdminBrand>(`/admin/brands/${id}`, request);

  return response.data;
};

export const deleteAdminBrand = async (id: string): Promise<void> => {
  await api.delete(`/admin/brands/${id}`);
};
