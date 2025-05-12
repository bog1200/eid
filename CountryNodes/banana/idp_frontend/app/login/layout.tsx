import {ReactNode, Suspense} from "react";

export default function LoginLayout({ children }: { children: ReactNode }) {
    return (
        <Suspense fallback={<div>Loading login...</div>}>
        <div className={"flex w-full m-4 items-center justify-center content-center"}>
            {children}
        </div>
        </Suspense>
    )
}