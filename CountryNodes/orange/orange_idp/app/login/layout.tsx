import React, {Suspense} from "react";

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