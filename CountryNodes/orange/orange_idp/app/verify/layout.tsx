export const metadata = {
    title: "Verify | Orange IDP",
    description: "Verify your identity using OrangeIDP",
}

export default function VerifyLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="en">
            <body className="antialiased">
                {children}
            </body>
        </html>
    );
}