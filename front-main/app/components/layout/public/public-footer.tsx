import Link from "next/link";

const productLinks = [
  { label: "Laptops Gamer", href: "/catalogo?categoria=laptops-gamer" },
  { label: "Tarjetas de Video", href: "/catalogo?categoria=tarjetas-graficas" },
  { label: "Monitores", href: "/catalogo?categoria=monitores" },
];

const serviceLinks = [
  { label: "Garantías", href: "/garantias" },
  { label: "Envíos", href: "/envios" },
  { label: "Seguimiento", href: "/seguimiento" },
  { label: "Soporte Técnico", href: "/soporte" },
];

const footerLinkClassName = `
  text-xs leading-[1.5] text-[#71839d] no-underline
  transition hover:text-[#111827]
`;

export default function PublicFooter() {
  return (
    <footer className="border-t border-[#e0e6ef] bg-white">
      <div className="px-4 sm:px-6 min-[761px]:px-[clamp(24px,4.1vw,50px)]">
        <div className="grid grid-cols-2 gap-x-6 gap-y-7 py-7 sm:grid-cols-4 sm:gap-8">
          <section>
            <h2 className="mb-3 text-xs font-bold uppercase text-[#111827]">
              NET COMPUTER
            </h2>
            <p className="m-0 max-w-[210px] text-xs leading-[1.5] text-[#71839d]">
              Tienda especializada en hardware, laptops y ensamble de computadoras.
            </p>
          </section>

          <section>
            <h2 className="mb-3 text-xs font-bold uppercase text-[#111827]">
              Productos
            </h2>
            <ul className="m-0 flex list-none flex-col gap-1 p-0">
              {productLinks.map((link) => (
                <li key={link.label}>
                  <Link className={footerLinkClassName} href={link.href}>
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </section>

          <section>
            <h2 className="mb-3 text-xs font-bold uppercase text-[#111827]">
              Servicio
            </h2>
            <ul className="m-0 flex list-none flex-col gap-1 p-0">
              {serviceLinks.map((link) => (
                <li key={link.label}>
                  <Link className={footerLinkClassName} href={link.href}>
                    {link.label}
                  </Link>
                </li>
              ))}
            </ul>
          </section>

          <section>
            <h2 className="mb-3 text-xs font-bold uppercase text-[#111827]">
              Contacto
            </h2>
            <div className="flex flex-col gap-1 text-xs leading-[1.5] text-[#71839d]">
              <a className={footerLinkClassName} href="mailto:contacto@netcomputer.com">
                contacto@netcomputer.com
              </a>
              <a className={footerLinkClassName} href="tel:+51987654321">
                +51 987 654 321
              </a>
              <span>Lun a Sáb: 9:00 - 18:00</span>
              <a
                className={footerLinkClassName}
                href="https://wa.me/51987654321"
              >
                Atención directa vía WhatsApp
              </a>
            </div>
          </section>
        </div>

        <div className="
          flex flex-col gap-2 py-4 sm:flex-row sm:items-center sm:justify-between
          text-xs text-[#94a3b8]
          border-t border-[#e0e6ef]
        ">
          <p className="m-0">
            © 2026 Net Computer. Todos los derechos reservados.
          </p>
          <nav className="flex gap-3" aria-label="Información legal">
            <Link className={footerLinkClassName} href="/terminos">
              Términos
            </Link>
            <span aria-hidden="true">•</span>
            <Link className={footerLinkClassName} href="/privacidad">
              Privacidad
            </Link>
          </nav>
        </div>
      </div>
    </footer>
  );
}
