import { prisma } from "@/lib/prisma";
import {NextResponse} from "next/server";
import {ethers} from "ethers";
export async function GET(
    _request: Request,
    { params }: { params: Promise<{ did: string }> }
) {
  const { did } = await params;
  if (!did) {
    return new Response("Did not provided", { status: 400 });
  }

  //get sig from query params
   const sig = new URL(_request.url).searchParams.get("sig")!

  // 1️⃣ Find latest nonce for this address
  const nonces = await prisma.nonce.findMany({
    where: {
      OR: [
        { did: did.toLowerCase() },
        { did: did.substring(9).toLowerCase() }
      ]
    },
    orderBy: { createdAt: "desc" },
    take: 1,
  });

  if (!nonces.length) {
    return NextResponse.json({ error: "No nonce found" }, { status: 400 });
  }

  const { nonce } = nonces[0];

  // 2️⃣ Verify signature
  let signer: string;
  try {
    signer = ethers.verifyMessage(nonce, sig);
  } catch {
    return NextResponse.json({ error: "Invalid signature format" }, { status: 400 });
  }

  if (signer.toLowerCase() !== did.substring(9).toLowerCase()) {
    return NextResponse.json({ error: "Invalid signature" }, { status: 401 });
  }

  try {
    const user = await prisma.user.findFirst({
      where: {
        OR: [
          {did: did.toLowerCase()},
          {did: `did:ethr:${did.toLowerCase()}`},
        ]
      }
    });

    if (user) {
      return new Response(JSON.stringify(user), {status: 200})
    }
    return new Response(null, {status: 404})

  } catch (error) {
    console.error("Error fetching user:", error);
  }
}