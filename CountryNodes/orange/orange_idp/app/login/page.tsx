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
            let [did] = await window.ethereum.request({ method: "eth_requestAccounts" });
            did = did.toLowerCase().startsWith("did:") ? did : `did:ethr:${did}`;
            setStatus("Got address: " + did);

            const res1 = await fetch("/api/auth/nonce", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ did }),
            });
            const { nonce } = await res1.json();

            const signature = await window.ethereum.request({
                method: "personal_sign",
                params: [nonce, did.substring(9)],
            });

            const res2 = await fetch("/api/auth/login", {
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
        <div className="min-h-screen bg-gradient-to-br from-orange-500 to-yellow-600 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl shadow-xl p-8 max-w-xl w-full text-center space-y-4">
                <h1 className="text-2xl font-bold text-gray-800">Login with Orange IDP</h1>
                <p className="text-gray-500 text-sm">Blockchain login</p>

                <button
                    onClick={login}
                    className="bg-orange-600 text-white py-2 px-4 rounded-full font-medium hover:bg-orange-700 transition"
                >
                    Log in with Wallet
                </button>

                <p className="text-gray-500 text-xs">
                    By logging in, you agree to our{" "}
                    <a href="#" className="text-orange-600 hover:underline">
                        Terms of Service
                    </a>
                    {" "}and{" "}
                    <a href="#" className="text-orange-600 hover:underline">
                        Privacy Policy
                    </a>
                </p>
                <p className="text-gray-500 text-xs">
                    Don&#39;t have an account?{" "}
                    <a href="/register" className="text-orange-600 hover:underline">
                        Register here
                    </a>
                </p>
                <p className="text-gray-500 text-xs">
                    If you want to verify the identity integrity, you can{" "}
                    <a href="/verify" className="text-orange-600 hover:underline">
                        click here
                    </a>
                </p>
                <button
                    onClick={() => window.history.back()}
                    className="text-gray-500 hover:text-gray-800 text-xs underline"
                >
                    Go back
                </button>


                {status && <p className="text-gray-700 text-xs mt-2 wrap-anywhere">{status}</p>}
            </div>
        </div>
    );
}
