import React, {Suspense} from "react";

export const metadata = {
    title: "Login | Orange IDP",
    description: "Login to Orange IDP",
}
export default async function LoginLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
   <Suspense fallback={<h1>Loading...</h1>}>
       {children}
    </Suspense>
  );
}