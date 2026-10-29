import Link from "next/link";
import { FiChevronRight } from "react-icons/fi";

export interface BreadcrumbItem {
  label: string;
  href?: string;
}

export default function Breadcrumbs({
  items,
}: {
  items: BreadcrumbItem[];
}) {
  return (
    <nav className="mb-5" aria-label="Migas de pan">
      <ol className="m-0 flex list-none flex-wrap items-center gap-2 p-0">
        {items.map((item, index) => {
          const isCurrent = index === items.length - 1;

          return (
            <li className="flex items-center gap-2" key={`${item.label}-${index}`}>
              {index > 0 && (
                <FiChevronRight
                  aria-hidden="true"
                  className="h-4 w-4 text-[#94a3b8]"
                />
              )}
              {item.href && !isCurrent ? (
                <Link
                  className="text-base text-[#647895] no-underline transition hover:text-[#111827]"
                  href={item.href}
                >
                  {item.label}
                </Link>
              ) : (
                <span
                  className={`text-base ${isCurrent ? "font-medium text-[#111827]" : "text-[#647895]"}`}
                  aria-current={isCurrent ? "page" : undefined}
                >
                  {item.label}
                </span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
