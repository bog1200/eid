import { NextResponse } from "next/server";
import { randomBytes } from "crypto";
import { prisma } from "@/lib/prisma";

export async function POST(req: Request) {
    const { address } = await req.json();
    if (!address) {
        return NextResponse.json({ error: "Address required" }, { status: 400 });
    }

    // 1️⃣ Generate nonce
    const nonce = "Login nonce: " + randomBytes(16).toString("hex");

    // 2️⃣ Save nonce (no userId required)
    await prisma.nonce.create({
        data: {
            address,
            nonce,
        },
    });

    return NextResponse.json({ nonce }, { status: 200 });
}
