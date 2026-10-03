"use client";

import { SectionHeading } from "@/components/landing/SectionHeading";
import { Reveal } from "@/components/motion";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { HelpCircle } from "lucide-react";

const faqs = [
  {
    question: "Is Bud-Wiser free?",
    answer: "Yes. It's free for personal use, with no premium tier and no card required.",
  },
  {
    question: "Can the AI change my data without asking?",
    answer:
      "No. The assistant can read your data through tools, but anything that writes (adding a transaction, creating a budget or goal) becomes a proposal you must confirm. Proposals expire after 15 minutes.",
  },
  {
    question: "Does the AI make up numbers?",
    answer:
      "It's instructed to only state numbers that come from tool results, and you can see every tool it called. All maths (totals, forecasts, goal plans) is done by the backend, not by the language model.",
  },
  {
    question: "Do I need to connect my bank?",
    answer:
      "No. You add transactions yourself, or tell the assistant what you spent. That keeps you in control and keeps your bank credentials out of the picture.",
  },
  {
    question: "How is my data protected?",
    answer:
      "Every query is scoped to your account, sessions use short-lived tokens with rotating httpOnly refresh cookies, and the AI never touches the database directly.",
  },
  {
    question: "Does it work on my phone?",
    answer: "Yes. The whole app is responsive and works in any modern mobile browser.",
  },
];

export function FAQSection() {
  return (
    <section id="faq" className="relative scroll-mt-24 py-24 md:py-32">
      <div className="container max-w-3xl">
        <SectionHeading eyebrow="FAQ" icon={HelpCircle} title={<>Questions, <span className="gradient-text">answered</span></>} />
        <Reveal>
          <Accordion type="single" collapsible className="space-y-3">
            {faqs.map((faq, index) => (
              <AccordionItem
                key={faq.question}
                value={`item-${index}`}
                className="glass-card border-b-0 px-5 transition-colors data-[state=open]:border-primary/30"
              >
                <AccordionTrigger className="py-5 text-left font-display text-base font-semibold hover:no-underline data-[state=open]:text-primary">
                  {faq.question}
                </AccordionTrigger>
                <AccordionContent className="pb-5 leading-relaxed text-muted-foreground">{faq.answer}</AccordionContent>
              </AccordionItem>
            ))}
          </Accordion>
        </Reveal>
      </div>
    </section>
  );
}
