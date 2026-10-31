"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { FiEdit2, FiPlus, FiTrash2 } from "react-icons/fi";
import BrandForm from "@/app/admin/marcas/components/brand-form";
import Button from "@/app/components/ui/button";
import DataTable, {
  type DataTableColumn,
} from "@/app/components/ui/data-table";
import Dialog from "@/app/components/ui/dialog";
import SearchInput from "@/app/components/ui/search-input";
import Snackbar, { type SnackbarType } from "@/app/components/ui/snackbar";
import {
  deleteAdminBrand,
  listAdminBrands,
} from "@/app/modules/catalog/brand/services/admin-brand.service";
import type { AdminBrand } from "@/app/modules/catalog/brand/types/admin-brand.type";
import { getApiErrorMessage } from "@/app/modules/catalog/utils/api-error-message";

const normalizeText = (value: string): string =>
  value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();

const getBrandColumns = (
  onEdit: (brand: AdminBrand) => void,
  onDelete: (brand: AdminBrand) => void
): DataTableColumn<AdminBrand>[] => [
  {
    id: "name",
    label: "Nombre",
    width: "wide",
    render: (brand) => (
      <span className="line-clamp-2 font-medium leading-snug">
        {brand.name}
      </span>
    ),
  },
  {
    id: "slug",
    label: "Slug",
    width: "medium",
    render: (brand) => <span className="text-[#647895]">{brand.slug}</span>,
  },
  {
    id: "actions",
    label: "Acciones",
    width: "compact",
    align: "center",
    render: (brand) => (
      <div className="flex items-center justify-center gap-2">
        <button
          className="
            inline-flex h-7 w-7 items-center justify-center
            text-[#647895]
            rounded transition hover:bg-slate-100 hover:text-[#111827]
          "
          type="button"
          aria-label={`Editar ${brand.name}`}
          onClick={() => onEdit(brand)}
        >
          <FiEdit2 aria-hidden="true" className="h-4 w-4" />
        </button>
        <button
          className="
            inline-flex h-7 w-7 items-center justify-center
            text-red-500
            rounded transition hover:bg-red-50 hover:text-red-700
          "
          type="button"
          aria-label={`Eliminar ${brand.name}`}
          onClick={() => onDelete(brand)}
        >
          <FiTrash2 aria-hidden="true" className="h-4 w-4" />
        </button>
      </div>
    ),
  },
];

export default function AdminBrandsPage() {
  const [brands, setBrands] = useState<AdminBrand[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [searchValue, setSearchValue] = useState("");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [brandToEdit, setBrandToEdit] = useState<AdminBrand | null>(null);
  const [brandToDelete, setBrandToDelete] = useState<AdminBrand | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");
  const [snackbar, setSnackbar] = useState<{
    type: SnackbarType;
    text: string;
  } | null>(null);

  const loadBrands = useCallback(async () => {
    setIsLoading(true);
    setHasError(false);

    try {
      setBrands(await listAdminBrands());
    } catch {
      setBrands([]);
      setHasError(true);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadBrands();
  }, [loadBrands]);

  const openCreateDialog = useCallback(() => {
    setBrandToEdit(null);
    setIsFormOpen(true);
  }, []);

  const openEditDialog = useCallback((brand: AdminBrand) => {
    setBrandToEdit(brand);
    setIsFormOpen(true);
  }, []);

  const closeFormDialog = useCallback(() => {
    setIsFormOpen(false);
  }, []);

  const openDeleteDialog = useCallback((brand: AdminBrand) => {
    setDeleteError("");
    setBrandToDelete(brand);
  }, []);

  const closeDeleteDialog = useCallback(() => {
    setBrandToDelete(null);
  }, []);

  const handleSaved = useCallback(
    (message: string) => {
      setIsFormOpen(false);
      setSnackbar({ type: "success", text: message });
      void loadBrands();
    },
    [loadBrands]
  );

  const handleConfirmDelete = useCallback(async () => {
    if (!brandToDelete) return;

    setIsDeleting(true);
    setDeleteError("");

    try {
      await deleteAdminBrand(brandToDelete.id);
      setBrandToDelete(null);
      setSnackbar({ type: "success", text: "Marca eliminada correctamente." });
      await loadBrands();
    } catch (error) {
      setDeleteError(
        getApiErrorMessage(
          error,
          "No se pudo eliminar la marca. Inténtalo de nuevo.",
          "No se puede eliminar la marca porque tiene productos asociados."
        )
      );
    } finally {
      setIsDeleting(false);
    }
  }, [brandToDelete, loadBrands]);

  const columns = useMemo(
    () => getBrandColumns(openEditDialog, openDeleteDialog),
    [openEditDialog, openDeleteDialog]
  );

  const filteredBrands = useMemo(() => {
    const search = normalizeText(searchValue.trim());
    if (!search) return brands;

    return brands.filter(
      (brand) =>
        normalizeText(brand.name).includes(search) ||
        normalizeText(brand.slug).includes(search)
    );
  }, [brands, searchValue]);

  const summary = isLoading
    ? "Cargando marcas..."
    : hasError
      ? "No se pudieron cargar las marcas"
      : `Mostrando ${filteredBrands.length} de ${brands.length} marcas registradas`;

  return (
    <div className="
      mx-auto flex min-h-[calc(100vh-126px)] w-full max-w-[1200px] flex-col
    ">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
        <h1 className="
          m-0
          text-2xl font-bold leading-tight tracking-tight text-[#111827]
        ">
          Gestión de Marcas
        </h1>
        <div className="flex flex-wrap items-center gap-2 self-start">
          <Button type="button" onClick={openCreateDialog}>
            <FiPlus aria-hidden="true" className="h-3.5 w-3.5" />
            Nueva Marca
          </Button>
        </div>
      </div>

      {snackbar && (
        <Snackbar
          key={`${snackbar.type}-${snackbar.text}`}
          type={snackbar.type}
          text={snackbar.text}
        />
      )}

      <div className="mt-5">
        <SearchInput
          id="brand-search"
          label="Buscar marcas por nombre o slug"
          placeholder="Buscar por nombre o slug..."
          value={searchValue}
          onValueChange={setSearchValue}
        />
      </div>

      {hasError && (
        <div className="
          mt-4 flex flex-wrap items-center justify-between gap-3 px-4 py-3
          text-sm text-red-700
          rounded-md border border-red-200 bg-red-50
        " role="alert">
          <span>No se pudieron cargar las marcas.</span>
          <button
            className="cursor-pointer font-semibold underline"
            type="button"
            onClick={() => void loadBrands()}
          >
            Reintentar
          </button>
        </div>
      )}

      <DataTable
        columns={columns}
        rows={isLoading ? [] : filteredBrands}
        getRowKey={(brand) => brand.id}
        summary={summary}
        currentPage={1}
        pageCount={1}
        emptyMessage={
          hasError
            ? "Error al consultar las marcas."
            : searchValue.trim()
              ? "No hay marcas que coincidan con la búsqueda."
              : "No hay marcas registradas."
        }
        isLoading={isLoading}
        loadingMessage="Cargando marcas..."
        showPagination={false}
      />

      <Dialog
        open={isFormOpen}
        title={brandToEdit ? "Editar Marca" : "Nueva Marca"}
        onClose={closeFormDialog}
      >
        <BrandForm
          brand={brandToEdit}
          onCancel={closeFormDialog}
          onSaved={handleSaved}
        />
      </Dialog>

      <Dialog
        open={brandToDelete !== null}
        title="Eliminar Marca"
        onClose={closeDeleteDialog}
      >
        <div className="mt-4 space-y-4">
          <p className="m-0 text-sm text-[#344054]">
            ¿Seguro que deseas eliminar la marca{" "}
            <strong>{brandToDelete?.name}</strong>? Esta acción no se puede
            deshacer.
          </p>
          {deleteError && (
            <p className="m-0 text-sm text-red-700" role="alert">
              {deleteError}
            </p>
          )}
          <div className="flex justify-end gap-2 border-t border-[#e0e6ef] pt-4">
            <Button
              type="button"
              size="small"
              variant="outline"
              disabled={isDeleting}
              onClick={closeDeleteDialog}
            >
              Cancelar
            </Button>
            <Button
              type="button"
              size="small"
              disabled={isDeleting}
              onClick={() => void handleConfirmDelete()}
            >
              {isDeleting ? "Eliminando..." : "Eliminar"}
            </Button>
          </div>
        </div>
      </Dialog>
    </div>
  );
}
