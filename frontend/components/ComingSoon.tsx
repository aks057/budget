"use client";

import { Construction, ArrowLeft } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";

interface ComingSoonProps {
  title: string;
  description?: string;
}

export default function ComingSoon({ title, description }: ComingSoonProps) {
  return (
    <div className="flex min-h-[60vh] items-center justify-center px-4">
      <Card className="w-full max-w-md border-dashed">
        <CardContent className="flex flex-col items-center justify-center p-8 text-center">
          <div className="mb-6 rounded-full bg-amber-500/10 p-4">
            <Construction className="h-12 w-12 text-amber-500" />
          </div>
          <h1 className="mb-2 text-2xl font-bold">{title}</h1>
          <p className="mb-6 text-muted-foreground">
            {description || "We're working hard to bring you something amazing. Stay tuned!"}
          </p>
          <Button asChild variant="outline">
            <Link href="/">
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Home
            </Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
