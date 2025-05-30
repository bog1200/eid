"use client";
import { useState } from "react";

export default function RegisterPage() {
    const [form, setForm] = useState({
        firstName: "",
        lastName: "",
        email: "",
        dob: "",
        pin: "",
        address: "",
        gender: "",
    });
    const [status, setStatus] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!window.ethereum) {
            setStatus("MetaMask not found");
            return;
        }

        try {
            const [blockchainAddress] = await window.ethereum.request({
                method: "eth_requestAccounts",
            });

            const resNonce = await fetch("/api/auth/nonce", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ did: blockchainAddress }),
            });

            const { nonce } = await resNonce.json();

            const signature = await window.ethereum.request({
                method: "personal_sign",
                params: [nonce, blockchainAddress],
            });

            const res = await fetch("/api/auth/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    blockchainAddress,
                    signature,
                    ...form,
                    dob: new Date(form.dob).toISOString(),
                }),
            });

            const data = await res.json();
            if (res.ok) {
                setStatus("✅ Registered successfully!");
            } else {
                setStatus("❌ Error: " + data.error);
            }
        } catch (err) {
            console.error(err);
            setStatus("❌ Error: " + (err as Error).message);
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-500 to-purple-600 p-4">
            <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-md">
                <h1 className="text-2xl font-bold text-center text-gray-800 mb-4">
                    Register with MetaMask
                </h1>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <input
                        name="firstName"
                        placeholder="First Name"
                        value={form.firstName}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        name="lastName"
                        placeholder="Last Name"
                        value={form.lastName}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        name="email"
                        placeholder="Email"
                        type="email"
                        value={form.email}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        name="dob"
                        placeholder="Date of Birth"
                        type="date"
                        value={form.dob}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        name="pin"
                        placeholder="PIN"
                        value={form.pin}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        name="address"
                        placeholder="Physical Address"
                        value={form.address}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />
                    <input
                        name="gender"
                        placeholder="Gender"
                        value={form.gender}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                    />

                    <button
                        type="submit"
                        className="w-full bg-indigo-600 text-white py-2 rounded font-semibold hover:bg-indigo-700 transition"
                    >
                        Register
                    </button>
                </form>
                {status && (
                    <p className="mt-4 text-center text-sm text-gray-700">{status}</p>
                )}
            </div>
        </div>
    );
}
