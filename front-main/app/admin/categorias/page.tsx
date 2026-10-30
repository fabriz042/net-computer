"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { FiEdit2, FiPlus, FiTrash2 } from "react-icons/fi";
import CategoryForm from "@/app/admin/categorias/components/category-form";
import Button from "@/app/components/ui/button";
import DataTable, {
  type DataTableColumn,
} from "@/app/components/ui/data-table";
import Dialog from "@/app/components/ui/dialog";
import SearchInput from "@/app/components/ui/search-input";
import Snackbar, { type SnackbarType } from "@/app/components/ui/snackbar";
import {
  deleteAdminCategory,
  listAdminCategories,
} from "@/app/modules/catalog/category/services/admin-category.service";
import type { AdminCategory } from "@/app/modules/catalog/category/types/admin-category.type";
import { getApiErrorMessage } from "@/app/modules/catalog/utils/api-error-message";

const normalizeText = (value: string): string =>
  value
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase();

const getCategoryColumns = (
  onEdit: (category: AdminCategory) => void,
  onDelete: (category: AdminCategory) => void
): DataTableColumn<AdminCategory>[] => [
  {
    id: "name",
    label: "Nombre",
    width: "wide",
    render: (category) => (
      <span className="line-clamp-2 font-medium leading-snug">
        {category.name}
      </span>
    ),
  },
  {
    id: "slug",
    label: "Slug",
    width: "medium",
    render: (category) => (
      <span className="text-[#647895]">{category.slug}</span>
    ),
  },
  {
    id: "specifications",
    label: "Especif.",
    width: "normal",
    align: "center",
    render: (category) => (
      <span className="font-semibold">
        {category.specificationSchema.length}
      </span>
    ),
  },
  {
    id: "status",
    label: "Estado",
    width: "narrow",
    render: (category) => (
      <span
        className={`
          inline-flex min-w-[76px] items-center justify-center px-2.5 py-1
          text-xs font-semibold ${category.active ? "text-[#166534]" : "text-[#475569]"}
          rounded-full ${category.active ? "bg-[#dcfce7]" : "bg-[#f1f5f9]"}
        `}
      >
        {category.active ? "Activa" : "Inactiva"}
      </span>
    ),
  },
  {
    id: "actions",
    label: "Acciones",
    width: "compact",
    align: "center",
    render: (category) => (
      <div className="flex items-center justify-center gap-2">
        <button
          className="
            inline-flex h-7 w-7 items-center justify-center
            text-[#647895]
            rounded transition hover:bg-slate-100 hover:text-[#111827]
          "
          type="button"
          aria-label={`Editar ${category.name}`}
          onClick={() => onEdit(category)}
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
          aria-label={`Eliminar ${category.name}`}
          onClick={() => onDelete(category)}
        >
          <FiTrash2 aria-hidden="true" className="h-4 w-4" />
        </button>
      </div>
    ),
  },
];

export default function AdminCategoriesPage() {
  const [categories, setCategories] = useState<AdminCategory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [searchValue, setSearchValue] = useState("");
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [categoryToEdit, setCategoryToEdit] = useState<AdminCategory | null>(null);
  const [categoryToDelete, setCategoryToDelete] = useState<AdminCategory | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState("");
  const [snackbar, setSnackbar] = useState<{
    type: SnackbarType;
    text: string;
  } | null>(null);

  const loadCategories = useCallback(async () => {
    setIsLoading(true);
    setHasError(false);

    try {
      setCategories(await listAdminCategories({ includeInactive: true }));
    } catch {
      setCategories([]);
      setHasError(true);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadCategories();
  }, [loadCategories]);

  const openCreateDialog = useCallback(() => {
    setCategoryToEdit(null);
    setIsFormOpen(true);
  }, []);

  const openEditDialog = useCallback((category: AdminCategory) => {
    setCategoryToEdit(category);
    setIsFormOpen(true);
  }, []);

  const closeFormDialog = useCallback(() => {
    setIsFormOpen(false);
  }, []);

  const openDeleteDialog = useCallback((category: AdminCategory) => {
    setDeleteError("");
    setCategoryToDelete(category);
  }, []);

  const closeDeleteDialog = useCallback(() => {
    setCategoryToDelete(null);
  }, []);

  const handleSaved = useCallback(
    (message: string) => {
      setIsFormOpen(false);
      setSnackbar({ type: "success", text: message });
      void loadCategories();
    },
    [loadCategories]
  );

  const handleConfirmDelete = useCallback(async () => {
    if (!categoryToDelete) return;

    setIsDeleting(true);
    setDeleteError("");

    try {
      await deleteAdminCategory(categoryToDelete.id);
      setCategoryToDelete(null);
      setSnackbar({ type: "success", text: "Categoría eliminada correctamente." });
      await loadCategories();
    } catch (error) {
      setDeleteError(
        getApiErrorMessage(
          error,
          "No se pudo eliminar la categoría. Inténtalo de nuevo.",
          "No se puede eliminar la categoría porque tiene productos asociados. Desactívala en su lugar."
        )
      );
    } finally {
      setIsDeleting(false);
    }
  }, [categoryToDelete, loadCategories]);

  const columns = useMemo(
    () => getCategoryColumns(openEditDialog, openDeleteDialog),
    [openEditDialog, openDeleteDialog]
  );

  const filteredCategories = useMemo(() => {
    const search = normalizeText(searchValue.trim());
    if (!search) return categories;

    return categories.filter(
      (category) =>
        normalizeText(category.name).includes(search) ||
        normalizeText(category.slug).includes(search)
    );
  }, [categories, searchValue]);

  const summary = isLoading
    ? "Cargando categorías..."
    : hasError
      ? "No se pudieron cargar las categorías"
      : `Mostrando ${filteredCategories.length} de ${categories.length} categorías registradas`;

  return (
    <div className="
      mx-auto flex min-h-[calc(100vh-126px)] w-full max-w-[1200px] flex-col
    ">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
        <h1 className="
          m-0
          text-2xl font-bold leading-tight tracking-tight text-[#111827]
        ">
          Gestión de Categorías
        </h1>
        <div className="flex flex-wrap items-center gap-2 self-start">
          <Button type="button" onClick={openCreateDialog}>
            <FiPlus aria-hidden="true" className="h-3.5 w-3.5" />
            Nueva Categoría
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
          id="category-search"
          label="Buscar categorías por nombre o slug"
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
          <span>No se pudieron cargar las categorías.</span>
          <button
            className="cursor-pointer font-semibold underline"
            type="button"
            onClick={() => void loadCategories()}
          >
            Reintentar
          </button>
        </div>
      )}

      <DataTable
        columns={columns}
        rows={isLoading ? [] : filteredCategories}
        getRowKey={(category) => category.id}
        summary={summary}
        currentPage={1}
        pageCount={1}
        emptyMessage={
          hasError
            ? "Error al consultar las categorías."
            : searchValue.trim()
              ? "No hay categorías que coincidan con la búsqueda."
              : "No hay categorías registradas."
        }
        isLoading={isLoading}
        loadingMessage="Cargando categorías..."
        showPagination={false}
      />

      <Dialog
        open={isFormOpen}
        title={categoryToEdit ? "Editar Categoría" : "Nueva Categoría"}
        onClose={closeFormDialog}
      >
        <CategoryForm
          category={categoryToEdit}
          onCancel={closeFormDialog}
          onSaved={handleSaved}
        />
      </Dialog>

      <Dialog
        open={categoryToDelete !== null}
        title="Eliminar Categoría"
        onClose={closeDeleteDialog}
      >
        <div className="mt-4 space-y-4">
          <p className="m-0 text-sm text-[#344054]">
            ¿Seguro que deseas eliminar la categoría{" "}
            <strong>{categoryToDelete?.name}</strong>? Esta acción no se puede
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
