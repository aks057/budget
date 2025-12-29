import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Shield } from "lucide-react";

export default function PrivacyPage() {
  return (
    <div className="container max-w-4xl py-12 md:py-20">
      <div className="mb-8 text-center">
        <div className="mb-4 inline-flex items-center gap-2 rounded-full bg-amber-500/10 px-4 py-2">
          <Shield className="h-5 w-5 text-amber-500" />
          <span className="text-sm font-medium text-amber-500">Legal</span>
        </div>
        <h1 className="mb-4 text-4xl font-bold">Privacy Policy</h1>
        <p className="text-muted-foreground">
          Last updated: {new Date().toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}
        </p>
      </div>

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>1. Information We Collect</CardTitle>
          </CardHeader>
          <CardContent className="prose prose-sm dark:prose-invert max-w-none">
            <p className="text-muted-foreground">
              We collect information you provide directly to us, including:
            </p>
            <ul className="mt-4 list-disc space-y-2 pl-6 text-muted-foreground">
              <li>Account information (email address, name)</li>
              <li>Financial data you choose to enter (transactions, categories, budgets)</li>
              <li>Usage data and preferences</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>2. How We Use Your Information</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>We use the information we collect to:</p>
            <ul className="mt-4 list-disc space-y-2 pl-6">
              <li>Provide, maintain, and improve our services</li>
              <li>Process transactions and send related information</li>
              <li>Send technical notices and support messages</li>
              <li>Respond to your comments and questions</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>3. Data Security</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              We take reasonable measures to help protect your personal
              information from loss, theft, misuse, unauthorized access,
              disclosure, alteration, and destruction. This includes:
            </p>
            <ul className="mt-4 list-disc space-y-2 pl-6">
              <li>Encryption of data at rest and in transit</li>
              <li>Row-level security ensuring users can only access their own data</li>
              <li>Regular security audits and monitoring</li>
              <li>Secure authentication protocols</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>4. Data Sharing</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              We do not sell, trade, or rent your personal information to third
              parties. We may share information only in the following
              circumstances:
            </p>
            <ul className="mt-4 list-disc space-y-2 pl-6">
              <li>With your consent</li>
              <li>To comply with legal obligations</li>
              <li>To protect our rights and safety</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>5. Your Rights</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>You have the right to:</p>
            <ul className="mt-4 list-disc space-y-2 pl-6">
              <li>Access your personal data</li>
              <li>Correct inaccurate data</li>
              <li>Request deletion of your data</li>
              <li>Export your data</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>6. Contact Us</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              If you have any questions about this Privacy Policy, please
              contact us at{" "}
              <a
                href="mailto:privacy@budwiser.app"
                className="text-amber-500 hover:underline"
              >
                privacy@budwiser.app
              </a>
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
