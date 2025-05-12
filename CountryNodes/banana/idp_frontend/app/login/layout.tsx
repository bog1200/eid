import {ReactNode} from "react";

export default function LoginLayout({ children }: { children: ReactNode }) {
    return (
        <div className={"flex w-full m-4 items-center justify-center content-center"}>
            {children}
        </div>
    )
}