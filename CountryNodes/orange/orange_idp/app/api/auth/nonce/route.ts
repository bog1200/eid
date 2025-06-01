import { NextResponse } from "next/server";
import { randomBytes } from "crypto";
import { prisma } from "@/lib/prisma";

export async function POST(req: Request) {
    const { did } = await req.json();
    if (!did) {
        return NextResponse.json({ error: "Address required" }, { status: 400 });
    }

    // 1️⃣ Generate nonce
    const nonce = "Login nonce: " + randomBytes(16).toString("hex");

    // 2️⃣ Save nonce (no userId required)
    await prisma.nonce.create({
        data: {
            did: did.startsWith("did:") ? did.toLowerCase() : `did:ethr:${did.toLowerCase()}`,
            nonce,
        },
    });

    return NextResponse.json({ nonce }, { status: 200 });
}
