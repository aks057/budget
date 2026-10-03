import { HeroSection } from "@/components/landing/HeroSection";
import { AgentDemo } from "@/components/landing/AgentDemo";
import { FeaturesSection } from "@/components/landing/FeaturesSection";
import { ArchitectureSection } from "@/components/landing/ArchitectureSection";
import { HowItWorks } from "@/components/landing/HowItWorks";
import { FAQSection } from "@/components/landing/FAQSection";
import { CTASection } from "@/components/landing/CTASection";

export default function LandingPage() {
  return (
    <>
      <HeroSection />
      <AgentDemo />
      <FeaturesSection />
      <ArchitectureSection />
      <HowItWorks />
      <FAQSection />
      <CTASection />
    </>
  );
}
