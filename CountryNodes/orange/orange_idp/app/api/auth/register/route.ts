import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { AbiCoder ,ethers } from "ethers";

export async function POST(req: Request) {
    const {
        blockchainAddress,
        signature,
        firstName,
        lastName,
        email,
        dob,
        pin,
        address,
        gender,
    } = await req.json();

    if (
        !blockchainAddress ||
        !signature ||
        !firstName ||
        !lastName ||
        !email ||
        !dob ||
        !pin ||
        !address ||
        !gender
    ) {
        return NextResponse.json({ error: "All fields are required" }, { status: 400 });
    }

    const did: string = blockchainAddress.startsWith("did:") ? blockchainAddress.toLowerCase() : `did:ethr:${blockchainAddress.toLowerCase()}`;

    console.log("Validating noonce for DID:", did);
    // Find the latest nonce
    const nonces = await prisma.nonce.findMany({
        where: { did },
        orderBy: { createdAt: "desc" },
        take: 1,
    });



    if (!nonces.length) {
        return NextResponse.json({ error: "No nonce found" }, { status: 400 });
    }
    console.log("Found nonce for DID:", did, "Nonce:", nonces[0].nonce);

    const { nonce, id: nonceId } = nonces[0];

    // Verify signature
    let signer: string;
    try {
        signer = ethers.verifyMessage(nonce, signature);
    } catch {
        return NextResponse.json({ error: "Invalid signature format" }, { status: 400 });
    }
    // Delete nonce

    const d = await prisma.nonce.delete({ where: { id: nonceId } });
    if (!d) {
        return NextResponse.json({ error: "Failed to delete nonce" }, { status: 500 });
    }

    if (signer.toLowerCase() !== did.substring(9).toLowerCase()) {
        return NextResponse.json({ error: "Invalid signature" }, { status: 401 });
    }

    // Check if user already exists
    const existingUser = await prisma.user.findUnique({ where: { did } });
    if (existingUser) {
        return NextResponse.json({ error: "User already registered" }, { status: 400 });
    }

    console.log("Creating new user with DID:", did);

    const dataHash = ethers.keccak256(
        new AbiCoder().encode(
            ["string", "string", "string", "string", "string", "string", "string", "string"],
            [did, firstName, lastName, email, dob, pin, address, gender]
        )
    );

    // Create new user
    const user = await prisma.user.create({
        data: {
            did,
            firstName,
            lastName,
            email,
            dob: new Date(dob),
            pin,
            address,
            gender,
        },
    });

    console.log("New user created:", user);



    console.log("Data hash:", dataHash);

    return NextResponse.json({ user, dataHash }, { status: 201 });
}
