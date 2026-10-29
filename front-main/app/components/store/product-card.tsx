import Button from "@/app/components/ui/button";

export type FeaturedProduct = {
  brand: string;
  category: string;
  name: string;
  price: string;
  slug: string;
  categorySlug: string;
  imagePosition: string;
};

export default function ProductCard({ product }: { product: FeaturedProduct }) {
  return (
    <article className="
      flex h-full min-w-0 flex-col p-1.5
      rounded-xl border border-[#dce3ed] bg-white
      cursor-pointer
    ">
      <div className="aspect-[1.62] w-full overflow-hidden rounded-md bg-slate-100">
        <span
          className={`
            block h-full w-full
            bg-[url('/images/featured-products.png')] bg-size-[300%_300%] bg-no-repeat ${product.imagePosition}
          `}
          aria-hidden="true"
        />
      </div>

      <div className="flex flex-1 flex-col px-1.5 pb-2 pt-2">
        <p className="
          m-0
          text-[9px] font-medium uppercase leading-[1.3] tracking-[0.02em] text-[#526582]
        ">
          {product.brand} <span aria-hidden="true">•</span> {product.category}
        </p>
        <h3 className="
          mb-0 mt-1 min-h-7
          text-xs font-semibold leading-[1.35] text-[#111827]
        ">
          {product.name}
        </h3>
        <p className="
          mt-auto inline-flex items-center gap-1.5 pt-2
          text-[9px] font-medium text-emerald-700
        ">
          <span className="h-[6px] w-[6px] rounded-full bg-emerald-500" />
          En stock
        </p>
        <div className="mt-2 flex items-center justify-between gap-2">
          <p className="m-0 text-base font-bold text-[#111827]">
            S/. {product.price}
          </p>
          <Button href={`/productos/${product.categorySlug}/${product.slug}`} size="small">
            Ver detalle
          </Button>
        </div>
      </div>
    </article>
  );
}
