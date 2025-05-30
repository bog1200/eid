import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
    async headers() {
        return [
            {
                // matching all API routes
                source: "/(.*)",
                headers: [
                    { key: "Access-Control-Allow-Origin", value: "https://orange-node.romail.app" },
                ]
            }
            ]
    }
};

export default nextConfig;
