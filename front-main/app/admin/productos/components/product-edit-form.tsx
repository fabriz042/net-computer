"use client";

import { useEffect, useState } from "react";
import Button from "@/app/components/ui/button";
import { getAdminProduct } from "@/app/modules/catalog/product/services/admin-product.service";
import type { AdminProductDetail } from "@/app/modules/catalog/product/types/admin-product.type";
import ProductForm from "./product-form";

interface ProductEditFormProps {
  productId: string;
  onCancel: () => void;
  onUpdated: () => void;
}

export default function ProductEditForm({
  productId,
  onCancel,
  onUpdated,
}: ProductEditFormProps) {
  const [product, setProduct] = useState<AdminProductDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [retryCount, setRetryCount] = useState(0);

  useEffect(() => {
    let isCurrent = true;
    setProduct(null);
    setIsLoading(true);
    setHasError(false);

    void getAdminProduct(productId)
      .then((result) => {
        if (isCurrent) setProduct(result);
      })
      .catch(() => {
        if (isCurrent) setHasError(true);
      })
      .finally(() => {
        if (isCurrent) setIsLoading(false);
      });

    return () => {
      isCurrent = false;
    };
  }, [productId, retryCount]);

  if (isLoading) {
    return (
      <p className="mt-5 text-sm text-[#647895]" role="status">
        Cargando datos del producto...
      </p>
    );
  }

  if (hasError || !product) {
    return (
      <div className="mt-5 space-y-4">
        <p className="m-0 text-sm text-red-700" role="alert">
          No se pudieron cargar los datos del producto.
        </p>
        <div className="flex justify-end gap-2">
          <Button type="button" size="small" variant="outline" onClick={onCancel}>
            Cerrar
          </Button>
          <Button
            type="button"
            size="small"
            variant="outline"
            onClick={() => setRetryCount((current) => current + 1)}
          >
            Reintentar
          </Button>
        </div>
      </div>
    );
  }

  return (
    <ProductForm
      key={product.id}
      initialProduct={product}
      onCancel={onCancel}
      onUpdated={onUpdated}
    />
  );
}
