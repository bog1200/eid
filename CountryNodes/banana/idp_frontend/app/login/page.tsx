'use client'

import { useSearchParams } from 'next/navigation'
import React, { useEffect, useState } from 'react'

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
    const appid =  searchParams.get('client_id') || searchParams.get('app');

    const [data, setData] = useState<AppData | null | undefined>();
    const [allow, setAllow] = useState<boolean>(false);
    const [message, setMessage] = useState<string>("");
    const [did, setDid] = useState<string>("");


    if (searchParams.get('error')) {
        setMessage("Error: " + searchParams.get('error'));
    }

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

    useEffect(() => {
        async function autofillDID() {
            if (typeof window === "undefined" || !window.ethereum) return;

            try {
                const [address] = await window.ethereum.request({
                    method: "eth_requestAccounts",
                });
                if (address) {
                    setDid(`did:ethr:${address.toLowerCase()}`);
                }
            } catch (err) {
                console.error("Failed to get Ethereum address:", err);
            }
        }

        autofillDID();
    }, []);
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
                <button onClick={() => window.location.replace(data.redirectUri+"?error=denied")} className={"bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded"}>
                    No
                </button>
            </div>
            {allow && (
            <div className={"my-4"}>
                <form className={"border-2 border-foreground"} action="/api/identity/startLogin" method={"POST"}>
                    <label className={"text-sm w-full font-bold py-2 px-4 rounded"} htmlFor="did">
                        Username/DID:
                    </label>
                    <input type="hidden" name="appId" value={appid!} />
                    <input type="text" id={"did"} name="did" placeholder="User ID / DID"  value={did}
                           onChange={(e) => setDid(e.target.value)} />

                    <button className={"w-full bg-green-500 "} type={"submit"}>Log IN</button>
                    <p className={"text-red-500"}>{message}</p>
                </form>
            </div>
            )}

        </div>
    )
}
