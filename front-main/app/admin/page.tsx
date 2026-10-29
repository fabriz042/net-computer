"use client";

import { useCallback, useEffect, useState } from "react";
import { FiClock, FiCpu, FiLayers } from "react-icons/fi";
import {
  Bar,
  BarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
  type YAxisTickContentProps,
} from "recharts";
import Button from "@/app/components/ui/button";
import DataTable, { type DataTableColumn } from "@/app/components/ui/data-table";
import Snackbar, { type SnackbarType } from "@/app/components/ui/snackbar";
import {
  generateAdminSearchQueryEmbeddings,
  listAdminSearchQueries,
} from "@/app/modules/analytics/search-query/services/admin-search-query.service";
import type { AdminSearchQuery } from "@/app/modules/analytics/search-query/types/admin-search-query.type";

const searchQueryColumns: DataTableColumn<AdminSearchQuery>[] = [
  {
    id: "searchTerm",
    label: "Término",
    width: "wide",
    render: (query) => <span className="font-medium">{query.searchTerm}</span>,
  },
  {
    id: "searchCount",
    label: "Búsquedas",
    width: "normal",
    align: "center",
    render: (query) => query.searchCount,
  },
  {
    id: "searchesWithClick",
    label: "Búsquedas con clic",
    width: "medium",
    align: "center",
    render: (query) => query.searchesWithClick ?? 0,
  },
  {
    id: "productsFound",
    label: "Productos encontrados",
    width: "normal",
    align: "center",
    render: (query) => query.productsFound ?? 0,
  },
  {
    id: "clickThroughRate",
    label: "CTR",
    width: "compact",
    align: "center",
    render: (query) => {
      const searchCount = query.searchCount ?? 0;
      const searchesWithClick = query.searchesWithClick ?? 0;
      const clickThroughRate = searchCount > 0
        ? (searchesWithClick / searchCount) * 100
        : 0;

      return `${clickThroughRate.toFixed(1)}%`;
    },
  },
];

type SearchQueryAxisTickProps = YAxisTickContentProps & {
  searchQueries: AdminSearchQuery[];
};

function SearchQueryAxisTick({
  x,
  y,
  payload,
  searchQueries,
}: SearchQueryAxisTickProps) {
  const searchTerm = String(payload.value ?? "");
  const searchQuery = searchQueries.find((query) => query.searchTerm === searchTerm);
  const hasEmbedding = searchQuery?.hasEmbedding ?? false;
  const StatusIcon = hasEmbedding ? FiLayers : FiClock;

  return (
    <g transform={`translate(${Number(x)},${Number(y)})`}>
      <title>{hasEmbedding ? "Embedding listo" : "Embedding pendiente"}</title>
      <text x={-26} y={4} textAnchor="end" fill="#647895">
        {searchTerm}
      </text>
      <StatusIcon
        x={-20}
        y={-7}
        size={14}
        color={hasEmbedding ? "#15803d" : "#94a3b8"}
        aria-hidden="true"
      />
    </g>
  );
}

export default function AdminPage() {
  const [searchQueries, setSearchQueries] = useState<AdminSearchQuery[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [hasError, setHasError] = useState(false);
  const [isGeneratingEmbeddings, setIsGeneratingEmbeddings] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    type: SnackbarType;
    text: string;
  } | null>(null);

  const loadSearchQueries = useCallback(async () => {
    setIsLoading(true);
    setHasError(false);

    try {
      setSearchQueries(await listAdminSearchQueries());
    } catch {
      setSearchQueries([]);
      setHasError(true);
      setSnackbar({
        type: "error",
        text: "No se pudieron cargar los términos más buscados.",
      });
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadSearchQueries();
  }, [loadSearchQueries]);

  const handleGenerateEmbeddings = useCallback(async () => {
    setIsGeneratingEmbeddings(true);
    setSnackbar(null);

    try {
      const result = await generateAdminSearchQueryEmbeddings();
      setSnackbar({
        type: result.generatedCount === 0 ? "warning" : "success",
        text: result.generatedCount === 0
          ? "No hay términos pendientes sin embedding."
          : `Embeddings generados para ${result.generatedCount} términos.`
      });
      await loadSearchQueries();
    } catch {
      setSnackbar({
        type: "error",
        text: "No se pudieron generar los embeddings de las búsquedas.",
      });
    } finally {
      setIsGeneratingEmbeddings(false);
    }
  }, [loadSearchQueries]);

  const chartHeight = Math.max(240, searchQueries.length * 44 + 48);

  return (
    <div className="flex min-h-[calc(100vh-126px)] w-full flex-col">
      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
        <div>
          <h1 className="m-0 font-bold leading-tight tracking-tight text-[#111827]">
            Términos más buscados
          </h1>
          <p className="mb-0 mt-2 text-[#647895]">
            Búsquedas registradas en la tienda.
          </p>
        </div>
        <Button
          type="button"
          onClick={handleGenerateEmbeddings}
          disabled={isGeneratingEmbeddings}
        >
          <FiCpu aria-hidden="true" className="h-4 w-4" />
          Generar embeddings
        </Button>
      </div>

      {snackbar && (
        <Snackbar
          key={`${snackbar.type}-${snackbar.text}`}
          type={snackbar.type}
          text={snackbar.text}
        />
      )}

      <section className="mt-6 p-5 rounded-lg border border-[#dce4ef] bg-white">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h2 className="m-0 font-semibold text-[#111827]">
            Top 20 de búsquedas
          </h2>
          <div className="flex flex-wrap items-center gap-4 text-[#647895]">
            <span className="flex items-center gap-1.5">
              <FiLayers aria-hidden="true" className="h-4 w-4 text-green-700" />
              Con embedding
            </span>
            <span className="flex items-center gap-1.5">
              <FiClock aria-hidden="true" className="h-4 w-4 text-slate-400" />
              Pendiente
            </span>
          </div>
        </div>

        {isLoading ? (
          <p className="mb-0 mt-5 text-[#647895]">Cargando búsquedas...</p>
        ) : hasError ? (
          <div className="mt-5 flex flex-wrap items-center justify-between gap-3">
            <p className="m-0 text-red-700">No se pudo cargar el gráfico.</p>
            <Button type="button" variant="outline" onClick={() => void loadSearchQueries()}>
              Reintentar
            </Button>
          </div>
        ) : searchQueries.length === 0 ? (
          <p className="mb-0 mt-5 text-[#647895]">
            Aún no hay búsquedas registradas.
          </p>
        ) : (
          <div className="mt-4 w-full">
            <ResponsiveContainer width="100%" height={chartHeight}>
              <BarChart
                data={searchQueries}
                layout="vertical"
                margin={{ top: 8, right: 28, bottom: 8, left: 8 }}
              >
                <CartesianGrid stroke="#e5eaf1" strokeDasharray="4 4" horizontal={false} />
                <XAxis
                  type="number"
                  allowDecimals={false}
                  tick={{ fill: "#647895" }}
                />
                <YAxis
                  dataKey="searchTerm"
                  type="category"
                  width={320}
                  interval={0}
                  tick={(props) => (
                    <SearchQueryAxisTick {...props} searchQueries={searchQueries} />
                  )}
                />
                <Tooltip
                  cursor={{ fill: "#f1f5f9" }}
                  formatter={(value) => [`${value}`, "Búsquedas"]}
                />
                <Bar
                  dataKey="searchCount"
                  fill="#111827"
                  maxBarSize={32}
                  radius={[0, 6, 6, 0]}
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </section>

      <section className="mt-6">
        <h2 className="m-0 font-semibold text-[#111827]">Detalle del Top 20</h2>
        <DataTable
          columns={searchQueryColumns}
          rows={searchQueries}
          getRowKey={(query) => query.searchTerm}
          summary={`Mostrando ${searchQueries.length} de 20 términos`}
          currentPage={1}
          pageCount={1}
          emptyMessage={hasError
            ? "No se pudieron cargar los términos."
            : "Aún no hay búsquedas registradas."}
          loadingMessage="Cargando búsquedas..."
          isLoading={isLoading}
          layout="content"
          showPagination={false}
        />
      </section>
    </div>
  );
}
