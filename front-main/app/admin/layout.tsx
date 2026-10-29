import type { ReactNode } from "react";
import AdminHeader from "@/app/components/layout/admin/admin-header";
import AdminSidebar from "@/app/components/layout/admin/admin-sidebar";

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col bg-white">
      <AdminHeader />
      <div className="flex flex-1 flex-col md:flex-row">
        <AdminSidebar />
        <main className="
          min-w-0 flex-1 p-4 sm:p-6 lg:p-7
          bg-[#f7f9fc]
        ">
          {children}
        </main>
      </div>
    </div>
  );
}
