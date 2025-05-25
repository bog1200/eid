'use client'

import { useSearchParams } from 'next/navigation'
import { useEffect, useState } from 'react'

interface Scope {
    name: string;
    description: string;
}

interface AppData {
    name: string;
    redirectUri: string;
    scopes: Scope[];
}

export default function LoginPage() {
    const searchParams = useSearchParams()
    const appid = searchParams.get('app') || searchParams.get('client_id');

    const [data, setData] = useState<AppData | null | undefined>();
    const [allow, setAllow] = useState<boolean>(false);
    const [message, setMessage] = useState<string>("");

    useEffect(() => {
        async function fetchData() {
            if (!appid) return;

            try {
                const res = await fetch(`/api/apps/${appid}`);
                if (!res.ok) {
                    throw new Error(`HTTP error! status: ${res.status}`);
                }
                const result = await res.json();
                setData(result);
            } catch (error) {
                console.error("Failed to fetch data:", error);
                setData(null);
            }
        }
        fetchData();
    }, [appid]);

    const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        const formData = new FormData(event.currentTarget);

        try {
            const res = await fetch("/api/identity/startLogin", {
                method: "POST",
                body: formData,
            });


            if (res.redirected) {
                // Navigate to the redirected URL
                window.location.href = res.url;
            } else {
                // get the response body
                const resBody = await res.text();
                console.log(resBody);
                setMessage(resBody);
            }
        } catch (error) {
            console.error("Error submitting form:", error);
            setMessage("An error occurred. Please try again.");
        }
    };


    if (!data) return <div>Loading or invalid data...</div>;
    return (
        <div>
            <h1 className={"text-4xl"}>Login - {data.name}</h1>
            <h2>{data.name} requests access to the following data:</h2>
            <ul>
                {data.scopes.map((item: Scope) => (
                    <li key={item.name}>{item.name}: {item.description}</li>
                    ))
                }
            </ul>
            <h2>Do you want to give access?</h2>
            <div className={"flex flex-row gap-2"}>
                <button onClick={() => setAllow(true) } className={"bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"}>
                    Yes
                </button>
                <button onClick={() => window.location.href=data.redirectUri+"?error=denied"} className={"bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"}>
                    No
                </button>
            </div>
            {allow && (
            <div className={"my-4"}>
                <form className={"border-2 border-foreground"} onSubmit={handleSubmit} method={"POST"}>
                    <label className={"text-sm w-full font-bold py-2 px-4 rounded"} htmlFor="did">
                        Username/DID:
                    </label>
                    <input type="hidden" name="appId" value={appid!} />
                    <input type="text" id={"did"} name="did" placeholder="User ID / DID" />

                    <button className={"w-full bg-green-500 "} type={"submit"}>Log IN</button>
                    <p className={"text-red-500"}>{message}</p>
                </form>
            </div>
            )}

        </div>
    )
}
