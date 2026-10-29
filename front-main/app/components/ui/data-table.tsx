import type { ReactNode } from "react";
import { FiChevronLeft, FiChevronRight } from "react-icons/fi";

type TableColumnWidth = "wide" | "medium" | "normal" | "narrow" | "compact";
type TableAlignment = "left" | "center" | "right";
type TableLayout = "fill" | "content";

export type DataTableColumn<T> = {
  id: string;
  label: string;
  width?: TableColumnWidth;
  align?: TableAlignment;
  render: (row: T) => ReactNode;
};

interface DataTableProps<T> {
  columns: DataTableColumn<T>[];
  rows: T[];
  getRowKey: (row: T) => string;
  summary: string;
  currentPage: number;
  pageCount: number;
  emptyMessage?: string;
  isLoading?: boolean;
  loadingMessage?: string;
  layout?: TableLayout;
  showPagination?: boolean;
  onPageChange?: (page: number) => void;
}

const columnWidthClasses: Record<TableColumnWidth, string> = {
  wide: "w-[43%]",
  medium: "w-[18%]",
  normal: "w-[13%]",
  narrow: "w-[12%]",
  compact: "w-[7%]",
};

const alignmentClasses: Record<TableAlignment, string> = {
  left: "text-left",
  center: "text-center",
  right: "text-right",
};

export default function DataTable<T>({
  columns,
  rows,
  getRowKey,
  summary,
  currentPage,
  pageCount,
  emptyMessage = "No hay resultados para mostrar.",
  isLoading = false,
  loadingMessage = "Cargando productos...",
  layout = "fill",
  showPagination = true,
  onPageChange,
}: DataTableProps<T>) {
  const visiblePageCount = Math.min(3, pageCount);
  const firstVisiblePage = Math.max(
    1,
    Math.min(currentPage - 1, pageCount - visiblePageCount + 1)
  );
  const pages = Array.from(
    { length: visiblePageCount },
    (_, index) => firstVisiblePage + index
  );

  return (
    <section className={`
      mt-4 flex flex-col overflow-hidden
      border border-[#e0e6ef] bg-white
      ${layout === "fill" ? "min-h-[calc(100vh-330px)] flex-1" : ""}
    `}>
      <div className="overflow-x-auto">
        <table className="
          w-full min-w-[850px] text-left
          table-fixed border-collapse
        ">
          <thead className="
            h-10
            text-base font-semibold uppercase text-[#647895]
            border-b border-[#e0e6ef] bg-[#f8fafc]
          ">
            <tr>
              {columns.map((column) => (
                <th
                  className={`
                    ${column.width ? columnWidthClasses[column.width] : ""} px-4
                    text-base font-semibold uppercase text-[#647895]
                    ${alignmentClasses[column.align ?? "left"]}
                  `}
                  key={column.id}
                  scope="col"
                >
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.length > 0 ? (
              rows.map((row, index) => (
                <tr
                  className={`
                    h-[54px]
                    text-base text-[#111827]
                    border-b border-[#edf1f6] ${index % 2 === 1 ? "bg-[#fafbfc]" : "bg-white"}
                  `}
                  key={getRowKey(row)}
                >
                  {columns.map((column) => (
                    <td
                      className={`
                        px-4
                        ${alignmentClasses[column.align ?? "left"]}
                      `}
                      key={column.id}
                    >
                      {column.render(row)}
                    </td>
                  ))}
                </tr>
              ))
            ) : (
              <tr>
                <td
                  className="
                    px-4 py-8
                    text-base text-center text-[#71839d]
                  "
                  colSpan={columns.length}
                >
                  {isLoading ? loadingMessage : emptyMessage}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <footer className="
        mt-auto flex flex-wrap items-center justify-between gap-3 px-4 py-3
        text-base text-[#71839d]
        border-t border-[#e0e6ef]
      ">
        <span>{summary}</span>
        {showPagination && (
          <nav
            className="
              flex items-center gap-2
              text-base text-[#111827]
            "
            aria-label="Paginación de la tabla"
          >
            <button
              className="
                inline-flex cursor-pointer items-center gap-1
                text-[#111827]
                bg-transparent disabled:cursor-not-allowed disabled:text-slate-300
              "
              type="button"
              disabled={isLoading || currentPage <= 1}
              onClick={() => onPageChange?.(currentPage - 1)}
            >
              <FiChevronLeft aria-hidden="true" className="h-3 w-3" />
              Ant.
            </button>
            {pages.map((page) => (
              <button
                className={`cursor-pointer disabled:cursor-not-allowed ${page === currentPage ? "text-[#111827] font-bold" : ""}`}
                type="button"
                disabled={isLoading}
                aria-current={page === currentPage ? "page" : undefined}
                key={page}
                onClick={() => onPageChange?.(page)}
              >
                {page}
              </button>
            ))}
            <button
              className="inline-flex cursor-pointer items-center gap-1 disabled:cursor-not-allowed disabled:text-slate-300"
              type="button"
              disabled={isLoading || currentPage >= pageCount}
              onClick={() => onPageChange?.(currentPage + 1)}
            >
              Sig.
              <FiChevronRight aria-hidden="true" className="h-3 w-3" />
            </button>
          </nav>
        )}
      </footer>
    </section>
  );
}
