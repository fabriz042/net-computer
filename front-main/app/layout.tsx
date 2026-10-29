import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "@/app/globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Net Computer - Tienda de Tecnología",
  description: "Venta de componentes de cómputo, laptops, tarjetas de video y periféricos",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="es"
      className={`h-full antialiased ${geistSans.variable} ${geistMono.variable}`}
    >
      <body className="min-h-full flex flex-col font-sans text-[#111827]">
        {children}
      </body>
    </html>
  );
}
