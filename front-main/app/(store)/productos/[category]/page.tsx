"use client";

import { Suspense, useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Button from "@/app/components/ui/button";
import Breadcrumbs from "@/app/components/shared/breadcrumbs";
import CatalogProductCard from "@/app/components/store/catalog-product-card";
import Container from "@/app/components/shared/container";
import PublicHeader from "@/app/components/layout/public/public-header";
import PublicFooter from "@/app/components/layout/public/public-footer";
import { listPublicProducts } from "@/app/modules/catalog/product/services/public-product.service";
import type {
  PublicProduct,
  PublicProductListResponse,
} from "@/app/modules/catalog/product/types/public-product.type";

const pageSize = 12;

const categoryLabels: Record<string, string> = {
  laptops: "Laptops",
  "tarjetas-de-video": "Tarjetas de Video",
  procesadores: "Procesadores",
  monitores: "Monitores",
  teclados: "Teclados",
  mouse: "Mouse",
  audifonos: "Audífonos",
};

const getCategoryLabel = (slug: string): string =>
  categoryLabels[slug] ??
  slug
    .split("-")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

function CategoryProductsContent() {
  const { category: categorySlug } = useParams<{ category: string }>();
  const [products, setProducts] = useState<PublicProduct[]>([]);
  const [meta, setMeta] = useState<PublicProductListResponse["meta"] | null>(null);
  const [page, setPage] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    setPage(1);
  }, [categorySlug]);

  useEffect(() => {
    let isCurrent = true;

    setIsLoading(true);
    setHasError(false);

    void listPublicProducts({
      category_slug: categorySlug,
      page,
      per_page: pageSize,
      order: "desc",
    })
      .then((response) => {
        if (!isCurrent) return;
        setProducts(response.data);
        setMeta(response.meta);
      })
      .catch(() => {
        if (!isCurrent) return;
        setProducts([]);
        setMeta(null);
        setHasError(true);
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false);
      });

    return () => {
      isCurrent = false;
    };
  }, [categorySlug, page]);

  const categoryName =
    products.find((product) => product.categoryName)?.categoryName ??
    getCategoryLabel(categorySlug);

  return (
    <Container>
      <PublicHeader />
      <main className="min-h-[60vh] px-4 pb-10 pt-8 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        <Breadcrumbs
          items={[
            { label: "Inicio", href: "/" },
            { label: categoryName },
          ]}
        />
        <header className="mb-6">
          <h1 className="m-0 text-2xl font-bold leading-tight text-[#111827]">
            {categoryName}
          </h1>
          <p className="mb-0 mt-2 text-base text-[#647895]">
            Productos disponibles en esta categoría.
          </p>
        </header>

        {hasError ? (
          <p className="m-0 py-8 text-base text-red-700" role="alert">
            No se pudieron cargar los productos. Inténtalo nuevamente.
          </p>
        ) : isLoading ? (
          <p className="m-0 py-8 text-base text-[#647895]" role="status">
            Cargando productos...
          </p>
        ) : products.length === 0 ? (
          <p className="m-0 py-8 text-base text-[#647895]">
            No hay productos disponibles en esta categoría.
          </p>
        ) : (
          <>
            <p className="mb-4 mt-0 text-base text-[#647895]">
              {meta?.total_items ?? products.length} productos
            </p>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {products.map((product) => (
                <CatalogProductCard key={product.id} product={product} />
              ))}
            </div>
            {meta && meta.total_pages > 1 && (
              <nav
                className="mt-6 flex items-center justify-center gap-4"
                aria-label="Paginación de productos"
              >
                <Button
                  type="button"
                  size="default"
                  variant="outline"
                  disabled={!meta.has_previous || isLoading}
                  onClick={() => setPage((currentPage) => currentPage - 1)}
                >
                  Anterior
                </Button>
                <span className="text-base text-[#647895]">
                  Página {meta.current_page} de {meta.total_pages}
                </span>
                <Button
                  type="button"
                  size="default"
                  disabled={!meta.has_next || isLoading}
                  onClick={() => setPage((currentPage) => currentPage + 1)}
                >
                  Siguiente
                </Button>
              </nav>
            )}
          </>
        )}
      </main>
      <PublicFooter />
    </Container>
  );
}

function CategoryProductsFallback() {
  return (
    <Container>
      <PublicHeader />
      <main className="min-h-[60vh] px-4 pb-10 pt-8 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        <Breadcrumbs
          items={[
            { label: "Inicio", href: "/" },
            { label: "Productos" },
          ]}
        />
        <p className="m-0 py-8 text-base text-[#647895]" role="status">
          Cargando productos...
        </p>
      </main>
      <PublicFooter />
    </Container>
  );
}

export default function CategoryProductsPage() {
  return (
    <Suspense fallback={<CategoryProductsFallback />}>
      <CategoryProductsContent />
    </Suspense>
  );
}
