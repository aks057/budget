import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { FileText } from "lucide-react";

export default function TermsPage() {
  return (
    <div className="container max-w-4xl py-12 md:py-20">
      <div className="mb-8 text-center">
        <div className="mb-4 inline-flex items-center gap-2 rounded-full bg-amber-500/10 px-4 py-2">
          <FileText className="h-5 w-5 text-amber-500" />
          <span className="text-sm font-medium text-amber-500">Legal</span>
        </div>
        <h1 className="mb-4 text-4xl font-bold">Terms of Service</h1>
        <p className="text-muted-foreground">
          Last updated: {new Date().toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" })}
        </p>
      </div>

      <div className="space-y-6">
        <Card>
          <CardHeader>
            <CardTitle>1. Acceptance of Terms</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              By accessing and using BudWiser, you accept and agree to be bound
              by the terms and provisions of this agreement. If you do not agree
              to these terms, please do not use our service.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>2. Description of Service</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              BudWiser is a personal finance management application that allows
              users to:
            </p>
            <ul className="mt-4 list-disc space-y-2 pl-6">
              <li>Track income and expenses</li>
              <li>Categorize transactions</li>
              <li>View financial analytics and reports</li>
              <li>Set and monitor budgets</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>3. User Accounts</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>To use BudWiser, you must:</p>
            <ul className="mt-4 list-disc space-y-2 pl-6">
              <li>Create an account with accurate information</li>
              <li>Maintain the security of your account credentials</li>
              <li>Notify us immediately of any unauthorized access</li>
              <li>Be responsible for all activities under your account</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>4. User Responsibilities</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>You agree not to:</p>
            <ul className="mt-4 list-disc space-y-2 pl-6">
              <li>Use the service for any illegal purposes</li>
              <li>Attempt to gain unauthorized access to our systems</li>
              <li>Interfere with or disrupt the service</li>
              <li>Share your account credentials with others</li>
            </ul>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>5. Data Accuracy</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              BudWiser relies on the data you provide. We are not responsible
              for any decisions made based on inaccurate data entry. Always
              verify your financial information for accuracy.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>6. Intellectual Property</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              All content, features, and functionality of BudWiser are owned by
              us and are protected by international copyright, trademark, and
              other intellectual property laws.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>7. Limitation of Liability</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              BudWiser is provided &quot;as is&quot; without warranties of any kind. We
              shall not be liable for any indirect, incidental, special, or
              consequential damages resulting from your use of the service.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>8. Changes to Terms</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              We reserve the right to modify these terms at any time. We will
              notify users of significant changes. Continued use of the service
              after changes constitutes acceptance of the new terms.
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>9. Contact</CardTitle>
          </CardHeader>
          <CardContent className="text-muted-foreground">
            <p>
              For questions about these Terms of Service, please contact us at{" "}
              <a
                href="mailto:legal@budwiser.app"
                className="text-amber-500 hover:underline"
              >
                legal@budwiser.app
              </a>
            </p>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
