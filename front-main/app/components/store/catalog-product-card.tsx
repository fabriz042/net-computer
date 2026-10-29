import Image from "next/image";
import Link from "next/link";
import type { MouseEventHandler } from "react";
import { FiImage } from "react-icons/fi";
import type { PublicProduct } from "@/app/modules/catalog/product/types/public-product.type";

const formatPrice = (price: number): string =>
  price.toLocaleString("es-PE", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });

export default function CatalogProductCard({
  product,
  onClick,
}: {
  product: PublicProduct;
  onClick?: MouseEventHandler<HTMLAnchorElement>;
}) {
  return (
    <Link
      onClick={onClick}
      className="
      flex min-w-0 flex-col overflow-hidden
      rounded-xl border border-[#dce3ed] bg-white
      text-inherit no-underline transition hover:-translate-y-0.5 hover:shadow-[0_7px_18px_rgb(16_24_40_/_9%)]
    "
      href={`/productos/${product.categorySlug ?? "productos"}/${product.slug}`}
    >
      <div className="relative aspect-[1.5] w-full overflow-hidden bg-[#f1f4f8]">
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
          <FiImage
            aria-hidden="true"
            className="absolute left-1/2 top-1/2 h-8 w-8 -translate-x-1/2 -translate-y-1/2 text-slate-400"
          />
        )}
      </div>
      <div className="flex flex-1 flex-col gap-2 p-4">
        <p className="m-0 text-base font-medium uppercase text-[#647895]">
          {product.brandName ?? "Sin marca"}
          {product.categoryName && (
            <>
              <span aria-hidden="true"> · </span>
              {product.categoryName}
            </>
          )}
        </p>
        <h2 className="m-0 line-clamp-2 text-lg font-semibold leading-snug text-[#111827]">
          {product.name}
        </h2>
        <p className="m-0 text-base text-emerald-700">
          {product.stock > 0 ? "En stock" : "Agotado"}
        </p>
        <p className="mt-auto m-0 pt-2 text-xl font-bold text-[#111827]">
          S/. {formatPrice(product.price)}
        </p>
      </div>
    </Link>
  );
}
