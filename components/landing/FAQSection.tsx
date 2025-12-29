"use client";

import { Badge } from "@/components/ui/badge";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { HelpCircle } from "lucide-react";

const faqs = [
  {
    question: "Is BudWiser really free?",
    answer:
      "Yes! BudWiser is completely free for personal use. We believe everyone should have access to great budgeting tools. There are no hidden fees or premium tiers.",
  },
  {
    question: "How secure is my financial data?",
    answer:
      "Your data is protected with industry-standard encryption. We use Row Level Security (RLS) to ensure only you can access your data. We never sell or share your information with third parties.",
  },
  {
    question: "Can I access BudWiser on my phone?",
    answer:
      "Absolutely! BudWiser is fully responsive and works great on any device - desktop, tablet, or mobile. Just open it in your browser and you&apos;re good to go.",
  },
  {
    question: "Do I need to connect my bank account?",
    answer:
      "No, BudWiser doesn&apos;t require bank connections. You manually enter your transactions, giving you full control over what data you track. This also means better privacy.",
  },
  {
    question: "Can I categorize my transactions?",
    answer:
      "Yes! You can create custom categories for both income and expenses. This helps you understand exactly where your money is going and identify areas to save.",
  },
  {
    question: "How do I get started?",
    answer:
      "Simply click &quot;Get Started Free&quot; and create an account with your email. You&apos;ll be set up in less than a minute and can start tracking your finances right away.",
  },
];

export function FAQSection() {
  return (
    <section className="relative py-20 md:py-28 overflow-hidden">
      {/* Background */}
      <div className="absolute inset-0 -z-10">
        <div className="absolute inset-0 bg-gradient-to-b from-background via-muted/30 to-background" />
        <div className="absolute right-0 top-1/4 h-[400px] w-[400px] rounded-full bg-amber-500/5 blur-3xl" />
        <div className="absolute left-0 bottom-1/4 h-[300px] w-[300px] rounded-full bg-orange-500/5 blur-3xl" />
      </div>

      <div className="container px-4 md:px-6">
        {/* Header */}
        <div className="mx-auto mb-16 max-w-3xl text-center">
          <Badge
            variant="outline"
            className="mb-4 border-amber-500/30 bg-amber-500/5"
          >
            <HelpCircle className="mr-1 h-3 w-3" />
            FAQ
          </Badge>
          <h2 className="mb-4 text-3xl font-bold tracking-tight sm:text-4xl md:text-5xl">
            Frequently Asked{" "}
            <span className="gradient-text">Questions</span>
          </h2>
          <p className="text-lg text-muted-foreground md:text-xl">
            Got questions? We&apos;ve got answers. If you can&apos;t find what you&apos;re
            looking for, feel free to contact us.
          </p>
        </div>

        {/* FAQ Accordion */}
        <div className="mx-auto max-w-3xl">
          <Accordion type="single" collapsible className="w-full space-y-4">
            {faqs.map((faq, index) => (
              <AccordionItem
                key={index}
                value={`item-${index}`}
                className="group rounded-xl border border-border/50 bg-gradient-to-br from-background to-muted/20 px-6 backdrop-blur-sm transition-all duration-300 hover:border-amber-500/30 hover:shadow-lg data-[state=open]:border-amber-500/30 data-[state=open]:shadow-lg"
              >
                <AccordionTrigger className="py-5 text-left text-base font-semibold hover:no-underline group-hover:text-amber-500 [&[data-state=open]]:text-amber-500">
                  <span className="flex items-center gap-3">
                    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-amber-500/10 text-sm font-bold text-amber-500 transition-colors group-hover:bg-amber-500 group-hover:text-white [&[data-state=open]]:bg-amber-500 [&[data-state=open]]:text-white">
                      {String(index + 1).padStart(2, "0")}
                    </span>
                    {faq.question}
                  </span>
                </AccordionTrigger>
                <AccordionContent className="pb-5 pl-11 text-muted-foreground leading-relaxed">
                  {faq.answer}
                </AccordionContent>
              </AccordionItem>
            ))}
          </Accordion>
        </div>
      </div>
    </section>
  );
}
