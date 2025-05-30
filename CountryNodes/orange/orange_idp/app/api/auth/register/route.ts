import { NextResponse } from "next/server";
import { prisma } from "@/lib/prisma";
import { ethers } from "ethers";

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

    const did: string = blockchainAddress.toLowerCase().startsWith("did:") ? blockchainAddress : `did:eth:${blockchainAddress}`;

    // Find the latest nonce
    const nonces = await prisma.nonce.findMany({
        where: { did },
        orderBy: { createdAt: "desc" },
        take: 1,
    });

    if (!nonces.length) {
        return NextResponse.json({ error: "No nonce found" }, { status: 400 });
    }

    const { nonce, id: nonceId } = nonces[0];

    // Verify signature
    let signer: string;
    try {
        signer = ethers.verifyMessage(nonce, signature);
    } catch {
        return NextResponse.json({ error: "Invalid signature format" }, { status: 400 });
    }

    if (signer !== did) {
        return NextResponse.json({ error: "Invalid signature" }, { status: 401 });
    }

    // Check if user already exists
    const existingUser = await prisma.user.findUnique({ where: { did } });
    if (existingUser) {
        return NextResponse.json({ error: "User already registered" }, { status: 400 });
    }

    // Create new user
    const user = await prisma.user.create({
        data: {
            did,
            blockchainHash: "", // Populate as needed
            firstName,
            lastName,
            email,
            dob: new Date(dob),
            pin,
            address,
            gender,
        },
    });

    // Delete nonce
    await prisma.nonce.delete({ where: { id: nonceId } });

    return NextResponse.json({ user }, { status: 201 });
}
