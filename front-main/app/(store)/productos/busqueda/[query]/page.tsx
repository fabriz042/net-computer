"use client";

import { Suspense, useCallback, useEffect, useRef, useState } from "react";
import { useParams } from "next/navigation";
import Button from "@/app/components/ui/button";
import Breadcrumbs from "@/app/components/shared/breadcrumbs";
import CatalogProductCard from "@/app/components/store/catalog-product-card";
import Container from "@/app/components/shared/container";
import PublicHeader from "@/app/components/layout/public/public-header";
import PublicFooter from "@/app/components/layout/public/public-footer";
import { trackPublicEvent } from "@/app/modules/analytics/event/services/public-event-tracker";
import { listPublicProducts } from "@/app/modules/catalog/product/services/public-product.service";
import type {
  PublicProduct,
  PublicProductListResponse,
} from "@/app/modules/catalog/product/types/public-product.type";

const pageSize = 12;

function ProductSearchContent() {
  const { query: routeQuery } = useParams<{ query: string }>();
  const query = decodeURIComponent(routeQuery).trim();
  const [products, setProducts] = useState<PublicProduct[]>([]);
  const [meta, setMeta] = useState<PublicProductListResponse["meta"] | null>(null);
  const [page, setPage] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [hasError, setHasError] = useState(false);
  const trackedQueryRef = useRef<string | null>(null);

  useEffect(() => {
    trackedQueryRef.current = null;
    setPage(1);
  }, [query]);

  const trackSearchResults = useCallback((productIds: string[]) => {
    if (!query || trackedQueryRef.current === query) return;

    trackedQueryRef.current = query;
    trackPublicEvent("SEARCH", { searchTerm: query, productIds });
  }, [query]);

  useEffect(() => {
    let isCurrent = true;

    setIsLoading(true);
    setHasError(false);

    void listPublicProducts({
      search: query,
      page,
      per_page: pageSize,
      order: "desc",
    })
      .then((response) => {
        if (!isCurrent) return;
        trackSearchResults(response.data.map((product) => product.id));
        setProducts(response.data);
        setMeta(response.meta);
      })
      .catch(() => {
        if (!isCurrent) return;
        trackSearchResults([]);
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
  }, [page, query, trackSearchResults]);

  return (
    <Container>
      <PublicHeader />
      <main className="min-h-[60vh] px-4 pb-10 pt-8 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        <Breadcrumbs
          items={[
            { label: "Inicio", href: "/" },
            { label: `Buscar: ${query}` },
          ]}
        />
        <header className="mb-6">
          <h1 className="m-0 text-2xl font-bold leading-tight text-[#111827]">
            Resultados para “{query}”
          </h1>
          {!isLoading && !hasError && (
            <p className="mb-0 mt-2 text-base text-[#647895]">
              {meta?.total_items ?? 0} productos encontrados
            </p>
          )}
        </header>

        {hasError ? (
          <p className="m-0 py-8 text-base text-red-700" role="alert">
            No se pudo completar la búsqueda. Inténtalo nuevamente.
          </p>
        ) : isLoading ? (
          <p className="m-0 py-8 text-base text-[#647895]" role="status">
            Buscando productos...
          </p>
        ) : products.length === 0 ? (
          <p className="m-0 py-8 text-base text-[#647895]">
            No encontramos productos para “{query}”.
          </p>
        ) : (
          <>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {products.map((product) => (
                <CatalogProductCard
                  key={product.id}
                  product={product}
                  onClick={() =>
                    trackPublicEvent("CLICK", {
                      componentId: "search-results-product-list",
                      productId: product.id,
                    })
                  }
                />
              ))}
            </div>
            {meta && meta.total_pages > 1 && (
              <nav
                className="mt-6 flex items-center justify-center gap-4"
                aria-label="Paginación de resultados"
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

function SearchResultsFallback() {
  return (
    <Container>
      <PublicHeader />
      <main className="min-h-[60vh] px-4 pb-10 pt-8 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        <Breadcrumbs
          items={[
            { label: "Inicio", href: "/" },
            { label: "Buscar productos" },
          ]}
        />
        <p className="m-0 py-8 text-base text-[#647895]" role="status">
          Cargando resultados...
        </p>
      </main>
      <PublicFooter />
    </Container>
  );
}

export default function ProductSearchPage() {
  return (
    <Suspense fallback={<SearchResultsFallback />}>
      <ProductSearchContent />
    </Suspense>
  );
}
