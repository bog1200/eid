"use client";
import {useEffect, useState} from "react";
import OrangeIDPABI from "@/lib/abi.json";
import { ethers, BrowserProvider } from "ethers";
import {AbiCoder} from "ethers";
const OrangeIDPAddress = process.env.NEXT_PUBLIC_ORANGE_IDP_ADDRESS!;


export default function RegisterPage() {
    const [status, setStatus] = useState("");
    const [dbDid, setDbDid] = useState("");
    const [ethHash, setEthHash] = useState("");
    const [ethDid, setEthDid] = useState("");
    const [dbHash, setDbHash] = useState("");
    const [dbData, setDbData] = useState("");


    useEffect(() => {
        async function verifyIdentity() {
            try {
                const provider = new BrowserProvider(window.ethereum);
                const signer = await provider.getSigner();
                const contract = new ethers.Contract(OrangeIDPAddress, OrangeIDPABI, signer);

                if (!dbHash){
                    const resNonce = await fetch("/api/auth/nonce", {
                        method: "POST",
                        headers: { "Content-Type": "application/json" },
                        body: JSON.stringify({ did: signer.address }),
                    });

                    const { nonce } = await resNonce.json();

                    const signature = await window.ethereum.request({
                        method: "personal_sign",
                        params: [nonce, signer.address],
                    });

                    const did = signer.address.toLowerCase().startsWith("did:") ? signer.address : `did:ethr:${signer.address}`;

                    const res = await fetch("/api/users/"+did+"/userinfo?sig="+signature);
                    const dbIdentity = await res.json();

                    const dataHash = ethers.keccak256(
                        new AbiCoder().encode(
                            ["string", "string", "string", "string", "string", "string", "string", "string"],
                            [dbIdentity.did, dbIdentity.firstName, dbIdentity.lastName, dbIdentity.email, dbIdentity.dob, dbIdentity.pin, dbIdentity.address, dbIdentity.gender]
                        )
                    );
                    setDbData(JSON.stringify(dbIdentity, null, 2));
                    setDbHash(dataHash);
                    setDbDid(signer.address);
                }
                if (!ethHash){
                    const [chainDid, chainHash] = await contract.getIdentityProof(signer.address);
                    if (!chainHash) {
                        setStatus("No identity proof found for this address.");
                        return;
                    }
                    setEthHash(chainHash);
                    setEthDid(chainDid);
                }

                if (!ethHash || !dbHash) {
                    setStatus("No identity proof found in the chain or database.");
                    return;
                }

                if (ethHash === dbHash){
                    setStatus("Identity verified successfully!");
                }
                else {
                    setStatus("Identity verification failed. Hashes do not match.");
                }
            } catch (error) {
                console.error("Error verifying identity:", error);
                setStatus("Failed to verify identity. Please try again.");
            }
        }
        verifyIdentity();
    }, [ethHash, dbHash]);

    return (
        <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-orange-500 to-yellow-500 p-4">
            <div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-fit">
                <h1 className="text-2xl font-bold text-center text-gray-800 mb-4">
                    Verify Identity
                </h1>
                {status && (
                    <p className="mt-4 text-center text-sm text-gray-700">{status}</p>
                )}

                <div className="mt-6 space-y-4">
                    <div className="bg-gray-100 p-4 rounded-lg">
                        <h2 className="text-lg font-semibold text-gray-800">Chain Identity</h2>
                        <p className="text-sm text-gray-600">DID: {ethDid || "No identity proof found."}</p>
                        <p className="text-sm text-gray-600">Hash: {ethHash || "No identity proof found."}</p>
                    </div>
                    <div className="bg-gray-100 p-4 rounded-lg">
                        <h2 className="text-lg font-semibold text-gray-800">Database Identity</h2>
                        <p className="text-sm text-gray-600">DID: {dbDid || "No identity found in database."}</p>
                        <p className="text-sm text-gray-600">Hash: {dbHash || "No identity found in database."}</p>
                    </div>
                    <div className="bg-gray-100 p-4 rounded-lg">
                        <h2 className="text-lg font-semibold text-gray-800">Identity data</h2>
                        <p className="text-sm text-gray-600">{ethHash === dbHash ? dbData : "Invalid identity"}</p>
                    </div>
            </div>
        </div>
    </div>
    );
}
