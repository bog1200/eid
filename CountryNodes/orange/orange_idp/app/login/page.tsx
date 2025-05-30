"use client";
import { useState } from "react";
import { useSearchParams } from "next/navigation";

export default function LoginPage() {
    const [status, setStatus] = useState("");
    const searchParams = useSearchParams();
    const state = searchParams.get("state");

    const login = async () => {
        if (typeof window === "undefined" || !window.ethereum) {
            setStatus("MetaMask not available");
            return;
        }

        try {
            const [did] = await window.ethereum.request({ method: "eth_requestAccounts" });
            setStatus("Got address: " + did);

            const res1 = await fetch("/api/auth/nonce", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ did }),
            });
            const { nonce } = await res1.json();

            const signature = await window.ethereum.request({
                method: "personal_sign",
                params: [nonce, did],
            });

            const res2 = await fetch("/api/auth/verify", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ did, signature }),
            });

            if (res2.ok) {
                const { token } = await res2.json();
                setStatus("Logged in!\n"+token);
                if (state) {
                    window.location.replace(process.env.NEXT_PUBLIC_CALLBACK_URI! + "?state=" + state + "&token=" + token); // Redirect to dashboard after login
                }
            } else {
                setStatus("Login failed!");
            }
        } catch (err) {
            console.error(err);
            setStatus("Error: " + err);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-purple-500 to-indigo-600 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-xl p-8 max-w-sm w-full text-center space-y-4">
                <h1 className="text-2xl font-bold text-gray-800">Login with MetaMask</h1>
                <p className="text-gray-500 text-sm">Secure blockchain login</p>

                <button
                    onClick={login}
                    className="bg-indigo-600 text-white py-2 px-4 rounded-full font-medium hover:bg-indigo-700 transition"
                >
                    Connect Wallet
                </button>

                {status && <p className="text-gray-700 text-xs mt-2">{status}</p>}
            </div>
        </div>
    );
}
