"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { FiCpu, FiEdit2, FiImage, FiPlus, FiTrash2 } from "react-icons/fi";
import DataTable, {
  type DataTableColumn,
} from "@/app/components/ui/data-table";
import Button from "@/app/components/ui/button";
import Dialog from "@/app/components/ui/dialog";
import ProductForm from "@/app/admin/productos/components/product-form";
import FilterSelect, {
  type FilterOption,
} from "@/app/components/ui/filter-select";
import SearchInput from "@/app/components/ui/search-input";
import Snackbar, { type SnackbarType } from "@/app/components/ui/snackbar";
import useDebounce from "@/app/hooks/use-debounce";
import {
  generateAdminProductEmbeddings,
  listAdminProducts,
} from "@/app/modules/catalog/product/services/admin-product.service";
import type {
  AdminProduct,
  AdminProductListResponse,
} from "@/app/modules/catalog/product/types/admin-product.type";

type ProductStatus = "active" | "inactive" | "low-stock" | "out-of-stock";

const pageSize = 8;

const statusLabels: Record<ProductStatus, string> = {
  active: "Activo",
  inactive: "Inactivo",
  "low-stock": "Bajo stock",
  "out-of-stock": "Agotado",
};

const statusTextStyles: Record<ProductStatus, string> = {
  active: "text-[#166534]",
  inactive: "text-[#475569]",
  "low-stock": "text-[#9a3412]",
  "out-of-stock": "text-[#475569]",
};

const statusSurfaceStyles: Record<ProductStatus, string> = {
  active: "bg-[#dcfce7]",
  inactive: "bg-[#f1f5f9]",
  "low-stock": "bg-[#ffedd5]",
  "out-of-stock": "bg-[#f1f5f9]",
};

const getProductStatus = (product: AdminProduct): ProductStatus => {
  if (!product.active) return "inactive";
  if (product.stock === 0) return "out-of-stock";
  if (product.stock <= 3) return "low-stock";
  return "active";
};

const formatPrice = (price: number): string =>
  price.toLocaleString("es-PE", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

const categoryOptions: FilterOption[] = [
  { value: "all-categories", label: "Todas las Categorías" },
  { value: "laptops", label: "Laptops" },
  { value: "tarjetas-de-video", label: "Tarjetas de Video" },
  { value: "procesadores", label: "Procesadores" },
  { value: "monitores", label: "Monitores" },
  { value: "teclados", label: "Teclados" },
  { value: "mouse", label: "Mouse" },
  { value: "audifonos", label: "Audífonos" },
];

const statusOptions: FilterOption[] = [
  { value: "all-statuses", label: "Todos los Estados" },
  { value: "active", label: "Activo" },
  { value: "low-stock", label: "Bajo stock" },
  { value: "out-of-stock", label: "Agotado" },
];

const productColumns: DataTableColumn<AdminProduct>[] = [
  {
    id: "product",
    label: "Producto",
    render: (product) => (
      <div className="flex min-w-0 items-center gap-2.5">
        <span
          className={`
            block h-9 w-9 shrink-0 overflow-hidden rounded-md
            bg-[#f1f5f9] bg-cover bg-center
          `}
          style={product.primaryImageUrl
            ? { backgroundImage: `url("${product.primaryImageUrl}")` }
            : undefined}
          aria-hidden="true"
        >
          {!product.primaryImageUrl && (
            <FiImage className="m-auto h-4 w-4 text-slate-400" aria-hidden="true" />
          )}
        </span>
        <span className="line-clamp-2 font-medium leading-snug">
          {product.name}
        </span>
      </div>
    ),
  },
  {
    id: "brand",
    label: "Marca",
    width: "normal",
    render: (product) => (
      <span className="text-[#647895]">{product.brandName ?? "Sin marca"}</span>
    ),
  },
  {
    id: "category",
    label: "Categoría",
    width: "medium",
    render: (product) => (
      <span className="text-[#647895]">{product.categoryName ?? "Sin categoría"}</span>
    ),
  },
  {
    id: "price",
    label: "Precio (S/.)",
    width: "normal",
    render: (product) => (
      <span className="font-semibold">S/. {formatPrice(product.price)}</span>
    ),
  },
  {
    id: "stock",
    label: "Stock",
    width: "compact",
    align: "center",
    render: (product) => (
      <span className="font-semibold">{product.stock}</span>
    ),
  },
  {
    id: "status",
    label: "Estado",
    width: "narrow",
    render: (product) => (
      <span className={`
        inline-flex min-w-[76px] items-center justify-center px-2.5 py-1
        text-xs font-semibold ${statusTextStyles[getProductStatus(product)]}
        rounded-full ${statusSurfaceStyles[getProductStatus(product)]}
      `}>
        {statusLabels[getProductStatus(product)]}
      </span>
    ),
  },
  {
    id: "embedding",
    label: "Embedding",
    width: "compact",
    align: "center",
    render: (product) => (
      <span className={product.hasEmbedding ? "font-medium text-green-700" : "text-slate-500"}>
        {product.hasEmbedding ? "Generado" : "Pendiente"}
      </span>
    ),
  },
  {
    id: "actions",
    label: "Acciones",
    width: "compact",
    align: "center",
    render: (product) => (
      <div className="flex items-center justify-center gap-2">
        <button
          className="
            inline-flex h-7 w-7 items-center justify-center
            text-[#647895]
            rounded transition hover:bg-slate-100 hover:text-[#111827]
          "
          type="button"
          aria-label={`Editar ${product.name}`}
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
          aria-label={`Eliminar ${product.name}`}
        >
          <FiTrash2 aria-hidden="true" className="h-4 w-4" />
        </button>
      </div>
    ),
  },
];

export default function AdminProductsPage() {
  const [page, setPage] = useState(1);
  const [searchValue, setSearchValue] = useState("");
  const debouncedSearch = useDebounce(searchValue.trim(), 200);
  const [isNewProductDialogOpen, setIsNewProductDialogOpen] = useState(false);
  const [isGeneratingEmbeddings, setIsGeneratingEmbeddings] = useState(false);
  const [embeddingSnackbar, setEmbeddingSnackbar] = useState<{
    type: SnackbarType;
    text: string;
  } | null>(null);
  const [response, setResponse] = useState<AdminProductListResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const requestIdRef = useRef(0);
  const previousSearchRef = useRef(debouncedSearch);

  const loadProducts = useCallback(async (pageToLoad: number, search: string) => {
    const requestId = ++requestIdRef.current;
    setIsLoading(true);
    setHasError(false);

    try {
      const result = await listAdminProducts({
        order: "desc",
        page: pageToLoad,
        per_page: pageSize,
        search: search || undefined,
      });
      if (requestId === requestIdRef.current) setResponse(result);
    } catch {
      if (requestId === requestIdRef.current) {
        setResponse(null);
        setHasError(true);
      }
    } finally {
      if (requestId === requestIdRef.current) setIsLoading(false);
    }
  }, []);

  const closeNewProductDialog = useCallback(() => {
    setIsNewProductDialogOpen(false);
  }, []);

  const handleGenerateEmbeddings = useCallback(async () => {
    setIsGeneratingEmbeddings(true);
    setEmbeddingSnackbar(null);

    try {
      const result = await generateAdminProductEmbeddings();
      setEmbeddingSnackbar({
        type: result.generatedCount === 0 ? "warning" : "success",
        text: result.generatedCount === 0
          ? "Todos los productos ya tienen embeddings."
          : `Embeddings generados para ${result.generatedCount} productos.`,
      });
      await loadProducts(page, debouncedSearch);
    } catch {
      setEmbeddingSnackbar({
        type: "error",
        text: "No se pudieron generar los embeddings.",
      });
    } finally {
      setIsGeneratingEmbeddings(false);
    }
  }, [debouncedSearch, loadProducts, page]);

  const handleProductCreated = useCallback(() => {
    setIsNewProductDialogOpen(false);
    if (page !== 1) {
      setPage(1);
      return;
    }
    void loadProducts(1, debouncedSearch);
  }, [debouncedSearch, loadProducts, page]);

  useEffect(() => {
    if (page !== 1 && debouncedSearch !== previousSearchRef.current) {
      previousSearchRef.current = debouncedSearch;
      setPage(1);
      return;
    }

    previousSearchRef.current = debouncedSearch;
    void loadProducts(page, debouncedSearch);
  }, [debouncedSearch, loadProducts, page]);

  const products = response?.data ?? [];
  const summary = isLoading
    ? "Cargando productos..."
    : response
      ? `Mostrando ${products.length} de ${response.meta.total_items} productos registrados`
      : "No se pudieron cargar los productos";

  return (
    <div className="
      mx-auto flex min-h-[calc(100vh-126px)] w-full max-w-[1200px] flex-col
    ">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
        <div>
          <h1 className="
            m-0
            text-2xl font-bold leading-tight tracking-tight text-[#111827]
          ">
            Gestión de Productos
          </h1>
        </div>
        <div className="flex flex-wrap items-center gap-2 self-start">
          <Button
            type="button"
            variant="outline"
            onClick={handleGenerateEmbeddings}
            disabled={isGeneratingEmbeddings}
          >
            <FiCpu aria-hidden="true" className="h-3.5 w-3.5" />
            Generar embeddings
          </Button>
          <Button type="button" onClick={() => setIsNewProductDialogOpen(true)}>
            <FiPlus aria-hidden="true" className="h-3.5 w-3.5" />
            Nuevo Producto
          </Button>
        </div>
      </div>

      {embeddingSnackbar && (
        <Snackbar
          key={`${embeddingSnackbar.type}-${embeddingSnackbar.text}`}
          type={embeddingSnackbar.type}
          text={embeddingSnackbar.text}
        />
      )}

      <div className="mt-5 grid grid-cols-1 gap-3 sm:grid-cols-[minmax(0,1fr)_180px_180px]">
        <SearchInput
          id="product-search"
          label="Buscar productos por nombre, código o categoría"
          placeholder="Buscar por nombre, código o categoría..."
          value={searchValue}
          onValueChange={setSearchValue}
        />
        <FilterSelect
          id="product-category-filter"
          label="Filtrar por categoría"
          defaultValue="all-categories"
          options={categoryOptions}
        />
        <FilterSelect
          id="product-status-filter"
          label="Filtrar por estado"
          defaultValue="all-statuses"
          options={statusOptions}
        />
      </div>

      {hasError && (
        <div className="
          mt-4 flex flex-wrap items-center justify-between gap-3 px-4 py-3
          text-sm text-red-700
          rounded-md border border-red-200 bg-red-50
        " role="alert">
          <span>No se pudieron cargar los productos.</span>
          <button
            className="cursor-pointer font-semibold underline"
            type="button"
            onClick={() => void loadProducts(page, debouncedSearch)}
          >
            Reintentar
          </button>
        </div>
      )}

      <DataTable
        columns={productColumns}
        rows={isLoading ? [] : products}
        getRowKey={(product) => product.id}
        summary={summary}
        currentPage={page}
        pageCount={Math.max(response?.meta.total_pages ?? 1, 1)}
        emptyMessage={hasError ? "Error al consultar el catálogo." : "No hay productos registrados."}
        isLoading={isLoading}
        onPageChange={setPage}
      />
      <Dialog
        open={isNewProductDialogOpen}
        title="Nuevo Producto"
        onClose={closeNewProductDialog}
      >
        <ProductForm
          onCancel={closeNewProductDialog}
          onCreated={handleProductCreated}
        />
      </Dialog>
    </div>
  );
}
