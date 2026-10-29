import type { ReactNode } from "react";

export default function Container({ children }: { children: ReactNode }) {
  return (
    <div className="
      mx-auto min-h-screen w-full max-w-[1600px]
      overflow-hidden border-b border-[#dce3ed]
    ">
      {children}
    </div>
  );
}
