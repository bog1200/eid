import { prisma } from "@/lib/prisma";
export async function GET(
    _request: Request,
    { params }: { params: Promise<{ did: string }> }
) {
  const { did } = await params;
  if (!did) {
    return new Response("Did not provided", { status: 400 });
  }

  try {
    const user = await prisma.user.findUnique({
      where: { blockchainAddress: did.toLowerCase() },
    });

    if (user) {
      return new Response(null, {status: 200})
    }
    return new Response(null, {status: 404})

  } catch (error) {
    console.error("Error fetching user:", error);
  }
}