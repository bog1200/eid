import { NextResponse } from "next/server";
import { ethers } from "ethers";
import { prisma } from "@/lib/prisma";
import jwt from "jsonwebtoken";

export async function POST(req: Request) {
    const { address, signature } = await req.json();
    if (!address || !signature) {
        return NextResponse.json({ error: "Missing parameters" }, { status: 400 });
    }

    // 1️⃣ Find latest nonce for this address
    const nonces = await prisma.nonce.findMany({
        where: { address },
        orderBy: { createdAt: "desc" },
        take: 1,
    });

    if (!nonces.length) {
        return NextResponse.json({ error: "No nonce found" }, { status: 400 });
    }

    const { nonce, id: nonceId } = nonces[0];

    // 2️⃣ Verify signature
    let signer: string;
    try {
        signer = ethers.verifyMessage(nonce, signature);
    } catch {
        return NextResponse.json({ error: "Invalid signature format" }, { status: 400 });
    }

    if (signer.toLowerCase() !== address.toLowerCase()) {
        return NextResponse.json({ error: "Invalid signature" }, { status: 401 });
    }

    // 3️⃣ Find the user (address is unique)
    const user = await prisma.user.findUnique({ where: { blockchainAddress: address } });
    if (!user) {
        return NextResponse.json({ error: "User not registered" }, { status: 401 });
    }

    // 4️⃣ Link nonce to user
    await prisma.nonce.update({
        where: { id: nonceId },
        data: { userId: user.id },
    });

    // 5️⃣ Issue JWT
    const token = jwt.sign(
        {
            sub: user.blockchainAddress,
            email: user.email,
            firstName: user.firstName,
            lastName: user.lastName,
            dob: user.dob,
            pin: user.pin,
            address: user.address,
            gender: user.gender,
        },
        process.env.JWT_SECRET as string,
        { expiresIn: "1d" },
    );

    // 6️⃣ Delete nonce to prevent replay
    await prisma.nonce.delete({ where: { id: nonceId } });

    return NextResponse.json({
        token,
        user: {
            id: user.id,
            address: user.blockchainAddress,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
        },
    });
}
