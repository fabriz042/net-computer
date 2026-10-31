"use client";

import Image from "next/image";
import Link from "next/link";
import { useParams, useRouter } from "next/navigation";
import { Suspense, useEffect, useState, type ReactNode } from "react";
import { FiMinus, FiPlus, FiShoppingBag } from "react-icons/fi";
import Button from "@/app/components/ui/button";
import Snackbar, { type SnackbarType } from "@/app/components/ui/snackbar";
import { useCart } from "@/app/hooks/use-cart";
import Breadcrumbs from "@/app/components/shared/breadcrumbs";
import Container from "@/app/components/shared/container";
import PublicHeader from "@/app/components/layout/public/public-header";
import PublicFooter from "@/app/components/layout/public/public-footer";
import {
  getPublicProductBySlug,
  listPublicProducts,
} from "@/app/modules/catalog/product/services/public-product.service";
import type { PublicProduct } from "@/app/modules/catalog/product/types/public-product.type";
import type { CartProductSnapshot } from "@/app/modules/users/cart/types/cart.type";

const specificationLabels: Record<string, string> = {
  processor: "Procesador",
  ram: "Memoria RAM",
  storage: "Almacenamiento",
  gpu: "Tarjeta gráfica",
  display: "Pantalla",
  vram: "Memoria de video",
  interface: "Interfaz",
  ports: "Puertos",
  recommended_psu: "Fuente recomendada",
  socket: "Socket",
  cores: "Núcleos",
  threads: "Hilos",
  base_clock: "Frecuencia base",
  boost_clock: "Frecuencia máxima",
  cache: "Caché",
  screen_size: "Tamaño de pantalla",
  resolution: "Resolución",
  refresh_rate: "Frecuencia de actualización",
  response_time: "Tiempo de respuesta",
  panel: "Tipo de panel",
  switches: "Tipo de switch",
  layout: "Distribución",
  connectivity: "Conectividad",
  lighting: "Iluminación",
  sensor: "Sensor",
  dpi: "Sensibilidad",
  weight: "Peso",
  buttons: "Cantidad de botones",
};

const formatPrice = (price: number): string =>
  price.toLocaleString("es-PE", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

const formatSpecificationLabel = (key: string): string =>
  specificationLabels[key] ??
  key
    .split("_")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

const formatSlugLabel = (slug: string): string =>
  slug
    .split("-")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");

const formatSpecificationValue = (value: unknown): string => {
  if (typeof value === "boolean") return value ? "Sí" : "No";
  if (Array.isArray(value)) return value.map(String).join(", ");
  if (value !== null && typeof value === "object") {
    return Object.values(value).map(String).join(", ");
  }
  return String(value);
};

const toCartProduct = (product: PublicProduct): CartProductSnapshot => ({
  productId: product.id,
  productName: product.name,
  productSlug: product.slug,
  categorySlug: product.categorySlug,
  brandName: product.brandName,
  imageUrl: product.primaryImageUrl ?? product.images[0] ?? null,
  unitPrice: product.price,
  availableStock: product.stock,
});

function StorePageShell({ children }: { children: ReactNode }) {
  return (
    <Container>
      <PublicHeader />
      {children}
      <PublicFooter />
    </Container>
  );
}

function RecommendedProductCard({
  product,
  fallbackCategory,
}: {
  product: PublicProduct;
  fallbackCategory: string;
}) {
  const href = `/productos/${product.categorySlug ?? fallbackCategory}/${product.slug}`;

  return (
    <Link
      className="
        flex min-w-0 flex-col gap-3 p-2
        rounded-lg border border-[#dce3ed] bg-white text-inherit no-underline
        transition hover:-translate-y-0.5 hover:shadow-[0_7px_18px_rgb(16_24_40_/_9%)]
      "
      href={href}
    >
      <div className="relative aspect-[1.6] w-full overflow-hidden rounded-md bg-[#f1f4f8]">
        {product.primaryImageUrl ? (
          <Image
            src={product.primaryImageUrl}
            alt={product.name}
            fill
            unoptimized
            sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 25vw"
            className="object-cover"
          />
        ) : (
          <span className="absolute inset-0 grid place-items-center text-4xl text-slate-400" aria-hidden="true">
            ▧
          </span>
        )}
      </div>
      <span className="flex flex-1 flex-col gap-2">
        <span className="text-sm font-medium uppercase text-[#647895]">
          {product.brandName ?? "Recomendado"}
        </span>
        <span className="line-clamp-2 text-base font-semibold leading-snug text-[#111827]">
          {product.name}
        </span>
        <span className={`mt-auto text-sm ${product.stock > 0 ? "text-emerald-700" : "text-slate-500"}`}>
          {product.stock > 0 ? "En stock" : "Agotado"}
        </span>
      </span>
    </Link>
  );
}

function ProductDetailContent() {
  const { category, slug } = useParams<{ category: string; slug: string }>();
  const router = useRouter();
  const { addItem, pendingProductId } = useCart();
  const [cartFeedback, setCartFeedback] = useState<{
    type: SnackbarType;
    text: string;
  } | null>(null);
  const [product, setProduct] = useState<PublicProduct | null>(null);
  const [recommendations, setRecommendations] = useState<PublicProduct[]>([]);
  const [selectedImageIndex, setSelectedImageIndex] = useState(0);
  const [quantity, setQuantity] = useState(1);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);

  useEffect(() => {
    let isCurrent = true;

    setIsLoading(true);
    setHasError(false);
    setProduct(null);
    setRecommendations([]);
    setSelectedImageIndex(0);
    setQuantity(1);

    void getPublicProductBySlug(slug)
      .then((result) => {
        if (!isCurrent) return;
        setProduct(result);
        setIsLoading(false);
      })
      .catch(() => {
        if (!isCurrent) return;
        setHasError(true);
        setIsLoading(false);
      });

    void listPublicProducts({ page: 1, per_page: 8, order: "desc" })
      .then((result) => {
        if (isCurrent) {
          setRecommendations(
            result.data.filter((item) => item.slug !== slug).slice(0, 4)
          );
        }
      })
      .catch(() => {
        if (isCurrent) setRecommendations([]);
      });

    return () => {
      isCurrent = false;
    };
  }, [slug]);

  if (isLoading) {
    return (
      <StorePageShell>
        <main className="min-h-[60vh] px-4 py-10 text-base text-[#647895] sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
          <Breadcrumbs
            items={[
              { label: "Inicio", href: "/" },
              { label: formatSlugLabel(category), href: `/productos/${category}` },
              { label: formatSlugLabel(slug) },
            ]}
          />
          Cargando producto...
        </main>
      </StorePageShell>
    );
  }

  if (hasError || !product) {
    return (
      <StorePageShell>
        <main className="min-h-[60vh] px-4 py-10 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
          <Breadcrumbs
            items={[
              { label: "Inicio", href: "/" },
              { label: formatSlugLabel(category), href: `/productos/${category}` },
              { label: formatSlugLabel(slug) },
            ]}
          />
          <div className="flex min-h-[40vh] flex-col items-center justify-center gap-5 text-center">
            <h1 className="m-0 text-2xl font-bold text-[#111827]">
              No encontramos este producto
            </h1>
            <p className="m-0 text-base text-[#647895]">
              Puede que ya no esté disponible o que el enlace no sea válido.
            </p>
            <Button href={`/productos/${category}`} variant="outline" size="large">
              Volver a la categoría
            </Button>
          </div>
        </main>
      </StorePageShell>
    );
  }

  const galleryImages = Array.from(
    new Set(
      [product.primaryImageUrl, ...product.images].filter(
        (image): image is string => Boolean(image)
      )
    )
  );
  const selectedImage = galleryImages[selectedImageIndex] ?? null;
  const specifications = Object.entries(product.specifications ?? {}).filter(
    ([, value]) => value !== null && value !== undefined
  );
  const hasStock = product.stock > 0;
  const isAddingToCart = pendingProductId === product.id;

  const handleAddToCart = async (goToCart: boolean) => {
    setCartFeedback(null);
    try {
      await addItem(toCartProduct(product), quantity);
      if (goToCart) {
        router.push("/carrito");
        return;
      }
      setCartFeedback({
        type: "success",
        text: quantity === 1
          ? "Producto añadido al carrito."
          : `${quantity} unidades añadidas al carrito.`,
      });
    } catch (error) {
      setCartFeedback({
        type: "error",
        text: error instanceof Error ? error.message : "No se pudo añadir el producto al carrito.",
      });
    }
  };

  return (
    <StorePageShell>
      <main className="px-4 pb-10 pt-6 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        {cartFeedback && (
          <Snackbar
            key={`${cartFeedback.type}-${cartFeedback.text}`}
            type={cartFeedback.type}
            text={cartFeedback.text}
          />
        )}
        <Breadcrumbs
          items={[
            { label: "Inicio", href: "/" },
            {
              label: product.categoryName ?? category,
              href: `/productos/${product.categorySlug ?? category}`,
            },
            { label: product.name },
          ]}
        />
        <section className="grid grid-cols-1 gap-6 lg:grid-cols-2 lg:gap-8">
          <div className="min-w-0">
            <div className="relative aspect-[1.3] w-full overflow-hidden rounded-lg bg-black">
              {selectedImage ? (
                <Image
                  src={selectedImage}
                  alt={product.name}
                  fill
                  priority
                  unoptimized
                  sizes="(max-width: 1024px) 100vw, 50vw"
                  className="object-contain"
                />
              ) : (
                <div className="absolute inset-0 grid place-items-center text-base text-white">
                  Imagen no disponible
                </div>
              )}
            </div>

            {galleryImages.length > 1 && (
              <div className="mt-3 grid grid-cols-4 gap-3">
                {galleryImages.map((image, index) => (
                  <button
                    aria-label={`Ver imagen ${index + 1} de ${product.name}`}
                    aria-pressed={index === selectedImageIndex}
                    className={`
                      relative aspect-[1.35] overflow-hidden rounded-md bg-[#f1f4f8]
                      border ${index === selectedImageIndex ? "border-[#111827]" : "border-[#dce3ed]"}
                    `}
                    key={image}
                    onClick={() => setSelectedImageIndex(index)}
                    type="button"
                  >
                    <Image
                      src={image}
                      alt=""
                      fill
                      unoptimized
                      sizes="25vw"
                      className="object-cover"
                    />
                  </button>
                ))}
              </div>
            )}

          </div>

          <div className="flex min-w-0 flex-col">
            <p className="mb-2 mt-0 text-base font-semibold uppercase tracking-wide text-[#526582]">
              {product.brandName ?? "Net Computer"}
              {product.categoryName && (
                <>
                  <span aria-hidden="true"> · </span>
                  {product.categoryName}
                </>
              )}
            </p>
            <h1 className="m-0 text-3xl font-bold leading-tight text-[#111827]">
              {product.name}
            </h1>

            <div className="mt-4 rounded-lg border border-[#e0e6ef] bg-[#f8fafc] px-4 py-3">
              <p className="m-0 text-2xl font-bold text-[#111827]">
                S/. {formatPrice(product.price)}
              </p>
              <p className="mb-0 mt-1 text-sm text-[#71839d]">
                Precio final. Consulta disponibilidad antes de comprar.
              </p>
            </div>

            <section className="mt-5" aria-labelledby="product-description-title">
              <h2
                id="product-description-title"
                className="m-0 text-base font-bold uppercase text-[#111827]"
              >
                Descripción general
              </h2>
              <p className="mb-0 mt-2 text-base leading-relaxed text-[#526582]">
                {product.name} de la categoría {product.categoryName ?? "hardware"}.
                Revisa sus características técnicas y disponibilidad antes de realizar tu compra.
              </p>
            </section>

            <div className="mt-6 grid grid-cols-[auto_minmax(0,1fr)] items-center gap-3">
              <div className="inline-flex h-10 items-center rounded-md border border-[#d5deea] bg-white">
                <button
                  aria-label="Reducir cantidad"
                  className="grid h-10 w-10 place-items-center text-[#526582] disabled:text-slate-300"
                  disabled={quantity <= 1}
                  onClick={() => setQuantity((current) => Math.max(1, current - 1))}
                  type="button"
                >
                  <FiMinus aria-hidden="true" />
                </button>
                <span className="min-w-8 text-center text-base font-semibold text-[#111827]">
                  {quantity}
                </span>
                <button
                  aria-label="Aumentar cantidad"
                  className="grid h-10 w-10 place-items-center text-[#526582] disabled:text-slate-300"
                  disabled={!hasStock || quantity >= product.stock}
                  onClick={() => setQuantity((current) => Math.min(product.stock, current + 1))}
                  type="button"
                >
                  <FiPlus aria-hidden="true" />
                </button>
              </div>
              <Button
                size="large"
                type="button"
                disabled={!hasStock || isAddingToCart}
                onClick={() => void handleAddToCart(false)}
              >
                <FiShoppingBag aria-hidden="true" />
                Añadir al carrito
              </Button>
            </div>
            <Button
              size="large"
              variant="outline"
              type="button"
              disabled={!hasStock || isAddingToCart}
              onClick={() => void handleAddToCart(true)}
            >
              Comprar ahora
            </Button>

            <p className={`mb-0 mt-4 inline-flex items-center gap-2 text-base font-medium ${hasStock ? "text-emerald-700" : "text-slate-500"}`}>
              <span className={`h-2 w-2 rounded-full ${hasStock ? "bg-emerald-500" : "bg-slate-400"}`} />
              {hasStock ? `En stock: ${product.stock} unidades` : "Agotado"}
            </p>
          </div>
        </section>

        <section className="mt-8" aria-labelledby="product-specifications-title">
          <h2
            id="product-specifications-title"
            className="mb-3 mt-0 text-xl font-bold text-[#111827]"
          >
            Especificaciones técnicas
          </h2>
          {specifications.length > 0 ? (
            <dl className="m-0 overflow-hidden rounded-lg border border-[#e0e6ef]">
              {specifications.map(([key, value], index) => (
                <div
                  className={`grid grid-cols-1 gap-2 px-4 py-3 sm:grid-cols-[minmax(180px,0.45fr)_1fr] sm:gap-5 ${index % 2 === 0 ? "bg-[#f8fafc]" : "bg-white"}`}
                  key={key}
                >
                  <dt className="font-semibold text-[#111827]">
                    {formatSpecificationLabel(key)}
                  </dt>
                  <dd className="m-0 text-[#526582]">
                    {formatSpecificationValue(value)}
                  </dd>
                </div>
              ))}
            </dl>
          ) : (
            <p className="m-0 text-base text-[#647895]">
              No hay especificaciones técnicas registradas.
            </p>
          )}
        </section>

        {recommendations.length > 0 && (
          <section className="mt-8" aria-labelledby="recommended-products-title">
            <h2
              id="recommended-products-title"
              className="mb-3 mt-0 text-xl font-bold text-[#111827]"
            >
              También te puede interesar
            </h2>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
              {recommendations.map((recommendedProduct) => (
                <RecommendedProductCard
                  fallbackCategory={category}
                  key={recommendedProduct.id}
                  product={recommendedProduct}
                />
              ))}
            </div>
          </section>
        )}
      </main>
    </StorePageShell>
  );
}

function ProductDetailFallback() {
  return (
    <StorePageShell>
      <main className="min-h-[60vh] px-4 py-10 text-base text-[#647895] sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        Cargando producto...
      </main>
    </StorePageShell>
  );
}

export default function ProductDetailPage() {
  return (
    <Suspense fallback={<ProductDetailFallback />}>
      <ProductDetailContent />
    </Suspense>
  );
}
