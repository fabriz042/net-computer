import { redirect } from "next/navigation";

interface LegacyProductSearchPageProps {
  searchParams: Promise<Record<string, string | string[] | undefined>>;
}

export default async function LegacyProductSearchPage({
  searchParams,
}: LegacyProductSearchPageProps) {
  const queryValue = (await searchParams).q;
  const query = (Array.isArray(queryValue) ? queryValue[0] : queryValue)?.trim();

  if (!query) redirect("/");

  redirect(`/productos/busqueda/${encodeURIComponent(query)}`);
}
