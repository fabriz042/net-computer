import Image from "next/image";
import Link from "next/link";
import { FiArrowRight } from "react-icons/fi";
import Container from "@/app/components/shared/container";
import PublicHeader from "@/app/components/layout/public/public-header";
import PublicFooter from "@/app/components/layout/public/public-footer";
import Button from "@/app/components/ui/button";
import ProductCard, {
  type FeaturedProduct,
} from "@/app/components/store/product-card";
import HeroPcModel from "@/app/components/store/hero-pc-model";

const storeBenefits = [
  {
    title: "Garantía real",
    description: "24 meses de cobertura oficial",
  },
  {
    title: "Compra segura",
    description: "Pagos y facturación 100% protegidos",
  },
  {
    title: "Envíos rápidos",
    description: "Despacho y entrega a todo el país",
  },
  {
    title: "Soporte técnico",
    description: "Atención especializada 24/7",
  },
];

const categories = [
  {
    name: "Laptops Gamer",
    count: "24 modelos",
    slug: "laptops",
    image: "/images/homepage/gaming-laptop-white-v2.png",
  },
  {
    name: "Tarjetas Gráficas",
    count: "16 modelos",
    slug: "tarjetas-de-video",
    image: "/images/homepage/graphics-card-white-v2.png",
  },
  {
    name: "Monitores",
    count: "20 modelos",
    slug: "monitores",
    image: "/images/homepage/monitor-white-v2.png",
  },
  {
    name: "Procesadores",
    count: "30 modelos",
    slug: "procesadores",
    image: "/images/homepage/processor-white-v2.png",
  },
  {
    name: "Teclados",
    count: "12 modelos",
    slug: "teclados",
    image: "/images/homepage/keyboard-white-v2.png",
  },
  {
    name: "Mouse",
    count: "10 modelos",
    slug: "mouse",
    image: "/images/homepage/mouse-white-v2.png",
  },
  {
    name: "Audífonos",
    count: "8 modelos",
    slug: "audifonos",
    image: "/images/homepage/headphones-white-v2.png",
  },
];

const featuredProducts: FeaturedProduct[] = [
  {
    brand: "ASUS ROG",
    category: "Laptop gamer",
    name: "Asus ROG Strix Scar 16",
    price: "6,999.00",
    slug: "asus-rog-strix-scar-16",
    categorySlug: "laptops",
    imagePosition: "bg-position-[0%_0%]",
  },
  {
    brand: "NVIDIA",
    category: "Tarjeta gráfica",
    name: "NVIDIA GeForce RTX 4090 24GB",
    price: "6,199.00",
    slug: "nvidia-geforce-rtx-4090-24gb",
    categorySlug: "tarjetas-de-video",
    imagePosition: "bg-position-[50%_0%]",
  },
  {
    brand: "SAMSUNG",
    category: "Monitor",
    name: 'Monitor Samsung Odyssey 34" Curvo',
    price: "1,499.00",
    slug: "samsung-odyssey-34-curvo",
    categorySlug: "monitores",
    imagePosition: "bg-position-[0%_50%]",
  },
  {
    brand: "INTEL",
    category: "Procesador",
    name: "Intel Core i9-14900K 24 Núcleos",
    price: "2,099.00",
    slug: "intel-core-i9-14900k",
    categorySlug: "procesadores",
    imagePosition: "bg-position-[50%_50%]",
  },
  {
    brand: "CORSAIR",
    category: "Memoria RAM",
    name: "Corsair Dominator Titanium 32GB",
    price: "649.00",
    slug: "corsair-dominator-titanium-32gb",
    categorySlug: "memorias-ram",
    imagePosition: "bg-position-[100%_50%]",
  },
  {
    brand: "SAMSUNG",
    category: "Almacenamiento",
    name: "Samsung 990 PRO NVMe 2TB",
    price: "699.00",
    slug: "samsung-990-pro-nvme-2tb",
    categorySlug: "almacenamiento",
    imagePosition: "bg-position-[0%_100%]",
  },
  {
    brand: "NET PRO",
    category: "Periférico",
    name: "Teclado Mecánico Minimal Pro",
    price: "349.00",
    slug: "teclado-mecanico-minimal-pro",
    categorySlug: "teclados",
    imagePosition: "bg-position-[50%_100%]",
  },
];

const productFilters = [
  { label: "Todos", href: "/#productos-destacados", active: true },
  { label: "Laptops", href: "/productos/laptops" },
  { label: "Gráficas", href: "/productos/tarjetas-de-video" },
  { label: "Monitores", href: "/productos/monitores" },
];

export default function Home() {
  return (
    <Container>
      <PublicHeader />
      <main>
        <section
          className="
            relative isolate mb-6 min-h-[660px] overflow-hidden rounded-[11px]
            px-4 sm:px-6
            min-[761px]:mb-[41px] min-[761px]:rounded-[14px]
            min-[1000px]:min-h-[517px]
            min-[761px]:px-[clamp(24px,4.1vw,50px)]
          "
          aria-labelledby="hero-title"
        >
          <HeroPcModel />
          <div
            aria-hidden="true"
            className="
              pointer-events-none absolute inset-0 z-10
              bg-[linear-gradient(180deg,_rgba(255,255,255,.98)_0%,_rgba(255,255,255,.94)_36%,_rgba(255,255,255,.62)_57%,_rgba(255,255,255,0)_100%)]
              min-[1000px]:bg-[linear-gradient(90deg,_#ffffff_0%,_rgba(255,255,255,.98)_30%,_rgba(255,255,255,.93)_44%,_rgba(255,255,255,.82)_54%,_rgba(255,255,255,.48)_66%,_rgba(255,255,255,.16)_80%,_rgba(255,255,255,0)_90%)]
            "
          />
          <div className="
            relative z-20 flex min-h-[660px] items-start pt-[30px]
            min-[1000px]:min-h-[517px] min-[1000px]:items-center min-[1000px]:pt-0
          ">
            <div className="min-w-0 max-w-[560px]">
              <p className="
                mb-[10px] min-[761px]:mb-3
                text-[10px] font-bold uppercase leading-[1.2] text-[#647895]
              ">
                Hardware &amp; computadoras
              </p>
              <h1
                id="hero-title"
                className="
                  min-[761px]:max-w-[510px]
                  text-[32px] font-bold leading-[1.13] tracking-[-0.045em] text-[#111827]
                  sm:text-[clamp(32px,8vw,42px)] min-[761px]:text-[clamp(32px,3.15vw,39px)]
                "
              >
                Tecnología y potencia para tu trabajo y juego.
              </h1>
              <p className="
                mt-[14px] max-w-[560px] min-[761px]:mt-[18px] min-[761px]:max-w-[455px]
                text-[12px] leading-[1.35] text-[#647895]
                min-[761px]:text-[13px]
              ">
                Equipos de alto rendimiento, componentes seleccionados y asesoría
                especializada con garantía oficial.
              </p>
              <div className="mt-[22px] min-[761px]:mt-[33px]">
                <Button href="/catalogo" size="large">
                  Ver catálogo
                </Button>
              </div>
            </div>
          </div>
        </section>

        <section
          className="
            mx-4 grid grid-cols-2 gap-x-2.5 gap-y-3.5 px-2.5 py-3
            min-[761px]:mx-[clamp(24px,4.1vw,50px)] min-[761px]:grid-cols-4 min-[761px]:gap-[18px] min-[761px]:px-4 min-[761px]:py-[11px]
            border border-[#dce3ed]
          "
          aria-label="Beneficios de compra"
        >
          {storeBenefits.map((benefit) => (
            <div key={benefit.title}>
              <h2 className="
                mb-1
                text-[9px] font-bold uppercase leading-[1.2] text-[#111827] min-[761px]:text-[10px]
              ">
                {benefit.title}
              </h2>
              <p className="
                m-0
                text-[9px] leading-[1.4] text-[#647895] min-[761px]:text-[10px]
              ">
                {benefit.description}
              </p>
            </div>
          ))}
        </section>

        <section
          className="
            px-4 pb-4 pt-[21px] sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)] min-[761px]:pt-[23px]
          "
          aria-labelledby="categories-title"
        >
          <div className="flex items-center justify-between gap-5">
            <h2
              id="categories-title"
              className="
                m-0
                text-xl font-bold leading-[1.2] tracking-[-0.025em] text-[#111827]
              "
            >
              Categorías
            </h2>
            <Link
              href="/catalogo"
              className="
                inline-flex items-center gap-1 whitespace-nowrap
                text-[10px] font-semibold text-[#526582] no-underline
                hover:text-[#111827]
              "
            >
              Ver todas <FiArrowRight aria-hidden="true" className="h-[13px] w-[13px]" />
            </Link>
          </div>

          <div className="
            mt-[11px] grid grid-cols-2 gap-3 min-[761px]:mt-[13px]
            min-[761px]:grid-cols-4 min-[761px]:gap-4 min-[1051px]:grid-cols-7 min-[1051px]:gap-5
          ">
            {categories.map((category) => (
              <Link
                className="
                  flex min-w-0 flex-col
                  text-inherit no-underline
                  overflow-hidden rounded-xl border border-[#dce3ed] bg-white
                  group transition hover:-translate-y-0.5 hover:shadow-[0_7px_18px_rgb(16_24_40_/_9%)]
                "
                href={`/productos/${category.slug}`}
                key={category.slug}
              >
                <Image
                  src={category.image}
                  alt=""
                  width={1024}
                  height={1024}
                  sizes="(min-width: 1051px) 14vw, (min-width: 761px) 25vw, 50vw"
                  className="block aspect-square w-full flex-none object-cover"
                />
                <span className="
                  flex min-h-[46px] flex-col justify-center gap-1 px-2 py-1.5 min-[761px]:px-2.5
                ">
                  <span className="
                    overflow-hidden text-ellipsis whitespace-nowrap
                    text-[11px] font-semibold leading-[1.2] text-[#111827]
                  ">
                    {category.name}
                  </span>
                  <span className="text-[10px] leading-[1.2] text-[#71839d]">
                    {category.count}
                  </span>
                </span>
              </Link>
            ))}
          </div>
        </section>

        <section
          id="productos-destacados"
          className="px-4 pb-8 pt-8 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]"
          aria-labelledby="featured-products-title"
        >
          <div className="mb-4 flex flex-col justify-between gap-3 sm:flex-row sm:items-start">
            <div>
              <h2
                id="featured-products-title"
                className="m-0 text-xl font-bold leading-tight tracking-[-0.025em] text-[#111827]"
              >
                Productos recomendados
              </h2>
              <p className="mb-0 mt-1 text-[10px] text-[#71839d]">
                Laptops, componentes y equipos armados.
              </p>
            </div>

            <nav
              className="flex max-w-full gap-1.5 overflow-x-auto pb-1"
              aria-label="Filtrar productos destacados"
            >
              {productFilters.map((filter) => {
                return (
                  <Button
                    href={filter.href}
                    size="chip"
                    variant={filter.active ? "black" : "outline"}
                    key={filter.label}
                    aria-current={filter.active ? "page" : undefined}
                  >
                    {filter.label}
                  </Button>
                );
              })}
            </nav>
          </div>

          <div className="grid grid-cols-1 gap-[14px] sm:grid-cols-2 md:grid-cols-4">
            {featuredProducts.map((product) => (
              <ProductCard key={product.slug} product={product} />
            ))}
          </div>

          <div className="mt-5 flex justify-center">
            <Button href="/catalogo" size="medium" variant="outline">
              Ver más productos
            </Button>
          </div>
        </section>
      </main>
      <PublicFooter />
    </Container>
  );
}
