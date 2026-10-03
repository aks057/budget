"use client";

import * as React from "react";

import { useMediaQuery } from "@/hooks/use-media-query";
import { Button } from "@/components/ui/button";
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command";
import { Drawer, DrawerContent, DrawerTrigger } from "@/components/ui/drawer";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import { Currencies, Currency } from "@/lib/currencies";
import { useMutation } from "@tanstack/react-query";
import { useAuth, useCurrentUser } from "@/components/providers/AuthProvider";
import { updateMe } from "@/lib/api/endpoints";
import { ApiError } from "@/lib/api/client";
import { toast } from "sonner";

const TOAST_ID = "update-currency";

export function CurrencyComboBox() {
  const [open, setOpen] = React.useState(false);
  const isDesktop = useMediaQuery("(min-width: 768px)");
  const user = useCurrentUser();
  const { setUser } = useAuth();

  const selectedOption = Currencies.find((currency) => currency.value === user.currency) ?? null;

  const mutation = useMutation({
    mutationFn: (currency: string) => updateMe({ fullName: user.fullName, currency }),
    onSuccess: (updated) => {
      toast.success(`Currency updated successfully 🎉`, { id: TOAST_ID });
      // Amounts are formatted client-side from user.currency, so updating the session user re-renders them.
      setUser(updated);
    },
    onError: (error) => {
      toast.error(error instanceof ApiError ? error.userMessage : "Something went wrong", { id: TOAST_ID });
    },
  });

  const selectOption = React.useCallback(
    (currency: Currency) => {
      toast.loading("Updating currency...", { id: TOAST_ID });
      mutation.mutate(currency.value);
    },
    [mutation]
  );

  if (isDesktop) {
    return (
      <Popover open={open} onOpenChange={setOpen}>
        <PopoverTrigger asChild>
          <Button
            variant="outline"
            className="w-full justify-start"
            disabled={mutation.isPending}
          >
            {selectedOption ? <>{selectedOption.label}</> : <>Set currency</>}
          </Button>
        </PopoverTrigger>
        <PopoverContent className="w-[200px] p-0" align="start">
          <OptionList setOpen={setOpen} setSelectedOption={selectOption} />
        </PopoverContent>
      </Popover>
    );
  }

  return (
    <Drawer open={open} onOpenChange={setOpen}>
      <DrawerTrigger asChild>
        <Button
          variant="outline"
          className="w-full justify-start"
          disabled={mutation.isPending}
        >
          {selectedOption ? <>{selectedOption.label}</> : <>Set currency</>}
        </Button>
      </DrawerTrigger>
      <DrawerContent>
        <div className="mt-4 border-t">
          <OptionList setOpen={setOpen} setSelectedOption={selectOption} />
        </div>
      </DrawerContent>
    </Drawer>
  );
}

function OptionList({
  setOpen,
  setSelectedOption,
}: {
  setOpen: (open: boolean) => void;
  setSelectedOption: (currency: Currency) => void;
}) {
  return (
    <Command>
      <CommandInput placeholder="Filter currency..." />
      <CommandList>
        <CommandEmpty>No results found.</CommandEmpty>
        <CommandGroup>
          {Currencies.map((currency: Currency) => (
            <CommandItem
              key={currency.value}
              value={currency.label}
              onSelect={() => {
                setSelectedOption(currency);
                setOpen(false);
              }}
            >
              {currency.label}
            </CommandItem>
          ))}
        </CommandGroup>
      </CommandList>
    </Command>
  );
}
