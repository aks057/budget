export const Currencies = [
  { value: "INR", label: "₹ Rupees", locale: "en-IN" },
  { value: "USD", label: "$ Dollar", locale: "en-US" },
  { value: "EUR", label: "€ Euro", locale: "de-DE" },
  { value: "GBP", label: "£ Pound", locale: "en-GB" },
];

export type Currency = (typeof Currencies)[0];