import { redirect } from "next/navigation";

interface LegacySearchPageProps {
  params: Promise<{ query: string }>;
}

export default async function LegacySearchPage({ params }: LegacySearchPageProps) {
  const { query } = await params;
  redirect(`/productos/busqueda/${encodeURIComponent(query)}`);
}
