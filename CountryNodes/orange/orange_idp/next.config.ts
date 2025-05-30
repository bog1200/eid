import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
    async headers() {
        return [
            {
                // matching all API routes
                source: "/(.*)",
                headers: [
                    { key: "Access-Control-Allow-Origin", value: "http://localhost:8080" },
                ]
            }
            ]
    }
};

export default nextConfig;
