import type { NextConfig } from "next";
import path from "node:path";
import { loadEnvConfig } from "@next/env";

loadEnvConfig(path.resolve(process.cwd(), ".."));

const nextConfig: NextConfig = {
  /* config options here */
  agentRules: false,
  env: {
    NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:4001/api",
  },
  experimental: {
    agentFeedback: false,
  },
  cacheComponents: true,
  partialPrefetching: true,
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;
