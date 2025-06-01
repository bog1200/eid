"use client";
import { useState } from "react";
import OrangeIDPABI from "@/lib/abi.json";
import { ethers, BrowserProvider } from "ethers";
const OrangeIDPAddress = process.env.NEXT_PUBLIC_ORANGE_IDP_ADDRESS!;
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
            const provider = new BrowserProvider(window.ethereum);

            await window.ethereum.request({
                method: "wallet_switchEthereumChain",
                params: [{ chainId: "0xaa36a7" }], // 0xaa36a7 is the hex chain ID for Sepolia
            });
            await provider.send("eth_requestAccounts", []);
            const signer = await provider.getSigner();
            const blockchainAddress = await signer.getAddress();

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
            console.log("Response:", res);




            const data = await res.json();
            if (res.ok) {
                console.log("Registration data:", data);
                setStatus("✅ Registered successfully!");
            } else {
                setStatus("❌ Error: " + data.error);
            }

            const contract = new ethers.Contract(
                OrangeIDPAddress,
                OrangeIDPABI,
                signer);

            console.log("Registering identity proof on blockchain...");
            const tx = await contract.registerIdentityProof(blockchainAddress, data.dataHash);
            await tx.wait();
            console.log("Transaction hash:", tx.hash);
            setStatus("✅ Identity proof registered on blockchain!");
        } catch (err) {
            console.error(err);
            setStatus("❌ Error: " + (err as Error).message);
        }



    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-orange-500 to-yellow-600 p-4">
            <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-xl">
                <h1 className="text-2xl font-bold text-center text-gray-800 mb-4">
                    Register to Orange IDP
                </h1>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <input
                        name="firstName"
                        placeholder="First Name"
                        value={form.firstName}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 text-black rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                    />
                    <input
                        name="lastName"
                        placeholder="Last Name"
                        value={form.lastName}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 text-black px-3 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                    />
                    <input
                        name="email"
                        placeholder="Email"
                        type="email"
                        value={form.email}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 text-black rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                    />
                    <input
                        name="dob"
                        placeholder="Date of Birth"
                        type="date"
                        value={form.dob}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 text-black rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                    />
                    <input
                        name="pin"
                        placeholder="PIN"
                        value={form.pin}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 text-black rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                    />
                    <input
                        name="address"
                        placeholder="Physical Address"
                        value={form.address}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 text-black rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                    />
                    <input
                        name="gender"
                        placeholder="Gender"
                        value={form.gender}
                        onChange={handleChange}
                        required
                        className="w-full border border-gray-300 text-black rounded px-3 py-2 focus:outline-none focus:ring-2 focus:ring-orange-500"
                    />

                    <button
                        type="submit"
                        className="w-full bg-orange-600 text-white py-2 rounded font-semibold hover:bg-orange-700 transition"
                    >
                        Register
                    </button>
                    <button
                        type="button"
                        onClick={() => window.location.href = "/login"}
                        className="w-full bg-orange-300 text-white py-2 rounded font-semibold hover:bg-gray-400 transition"
                    >
                        Back to Login
                    </button>
                </form>
                {status && (
                    <p className="mt-4 text-center text-sm text-gray-700">{status}</p>
                )}
            </div>
        </div>
    );
}
