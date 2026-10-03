"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import React, { useMemo, useState } from "react";
import {
  ColumnDef,
  ColumnFiltersState,
  flexRender,
  getCoreRowModel,
  PaginationState,
  useReactTable,
} from "@tanstack/react-table";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import SkeletonWrapper from "@/components/SkeletonWrapper";
import { DataTableColumnHeader } from "@/components/datatable/ColumnHeader";
import { cn } from "@/lib/utils";
import { DataTableFacetedFilter } from "@/components/datatable/FacetedFilters";
import { Button } from "@/components/ui/button";
import { DataTableViewOptions } from "@/components/datatable/ColumnToggle";
import { useCurrentUser } from "@/components/providers/AuthProvider";
import { getCategories, getTransactions, toApiDate, Transaction } from "@/lib/api/endpoints";
import { GetFormatterForCurrency } from "@/lib/helpers";
import { TransactionType } from "@/lib/types";
import { parseISO } from "date-fns";

import { download, generateCsv, mkConfig } from "export-to-csv";
import { DownloadIcon, MoreHorizontal, TrashIcon } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import DeleteTransactionDialog from "@/app/(dashboard)/transactions/_components/DeleteTransactionDialog";

interface Props {
  from: Date;
  to: Date;
}

const PAGE_SIZE = 10;
const emptyData: Transaction[] = [];

const csvConfig = mkConfig({
  fieldSeparator: ",",
  decimalSeparator: ".",
  useKeysAsHeaders: true,
});

function buildColumns(formatter: Intl.NumberFormat): ColumnDef<Transaction>[] {
  return [
    {
      id: "category",
      accessorKey: "categoryId",
      header: ({ column }) => <DataTableColumnHeader column={column} title="Category" />,
      cell: ({ row }) => (
        <div className="flex gap-2 capitalize">
          {row.original.categoryIcon}
          <div className="capitalize">{row.original.categoryName}</div>
        </div>
      ),
    },
    {
      accessorKey: "description",
      header: ({ column }) => <DataTableColumnHeader column={column} title="Description" />,
      cell: ({ row }) => <div className="capitalize">{row.original.description}</div>,
    },
    {
      accessorKey: "date",
      header: "Date",
      cell: ({ row }) => {
        // `date` is a calendar date (yyyy-MM-dd); parseISO keeps it in local time so it never shifts a day.
        const formattedDate = parseISO(row.original.date).toLocaleDateString("default", {
          year: "numeric",
          month: "2-digit",
          day: "2-digit",
        });
        return <div className="text-muted-foreground">{formattedDate}</div>;
      },
    },
    {
      accessorKey: "type",
      header: ({ column }) => <DataTableColumnHeader column={column} title="Type" />,
      cell: ({ row }) => (
        <div
          className={cn(
            "capitalize rounded-full px-2.5 py-1 text-center text-xs font-semibold",
            row.original.type === "income" && "bg-income/10 text-income",
            row.original.type === "expense" && "bg-expense/10 text-expense"
          )}
        >
          {row.original.type}
        </div>
      ),
    },
    {
      accessorKey: "amount",
      header: ({ column }) => <DataTableColumnHeader column={column} title="Amount" />,
      cell: ({ row }) => (
        <p className={cn("text-right font-display font-semibold tabular-nums", row.original.type === "income" ? "text-income" : "text-foreground")}>
          {formatter.format(row.original.amount)}
        </p>
      ),
    },
    {
      id: "actions",
      enableHiding: false,
      cell: ({ row }) => <RowActions transaction={row.original} />,
    },
  ];
}

/** The API filters by a single type and a single category, so each facet keeps only the latest pick. */
function singleSelect(filters: ColumnFiltersState): ColumnFiltersState {
  return filters.map((filter) => {
    const values = filter.value as string[];
    return { ...filter, value: values.slice(-1) };
  });
}

function filterValue(filters: ColumnFiltersState, id: string): string | undefined {
  const values = filters.find((filter) => filter.id === id)?.value as string[] | undefined;
  return values?.[0];
}

function TransactionTable({ from, to }: Props) {
  const user = useCurrentUser();
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const [pagination, setPagination] = useState<PaginationState>({ pageIndex: 0, pageSize: PAGE_SIZE });

  const type = filterValue(columnFilters, "type") as TransactionType | undefined;
  const categoryId = filterValue(columnFilters, "category");

  // A new range starts again from the first page. Adjusting state during render (not in an effect)
  // avoids one wasted request for the old page index with the new range.
  const fromKey = toApiDate(from);
  const toKey = toApiDate(to);
  const rangeKey = `${fromKey}_${toKey}`;
  const [prevRangeKey, setPrevRangeKey] = useState(rangeKey);
  if (rangeKey !== prevRangeKey) {
    setPrevRangeKey(rangeKey);
    setPagination((prev) => ({ ...prev, pageIndex: 0 }));
  }

  const history = useQuery({
    queryKey: ["transactions", "history", fromKey, toKey, type, categoryId, pagination.pageIndex, pagination.pageSize],
    queryFn: () =>
      getTransactions({
        from,
        to,
        page: pagination.pageIndex,
        size: pagination.pageSize,
        type,
        categoryId,
      }),
    placeholderData: keepPreviousData,
  });

  const categoriesQuery = useQuery({
    queryKey: ["categories", "all"],
    queryFn: () => getCategories(),
  });

  const formatter = useMemo(() => GetFormatterForCurrency(user.currency), [user.currency]);
  const columns = useMemo(() => buildColumns(formatter), [formatter]);

  const table = useReactTable({
    data: history.data?.items ?? emptyData,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getRowId: (row) => row.id,
    state: {
      columnFilters,
      pagination,
    },
    manualPagination: true,
    manualFiltering: true,
    enableSorting: false,
    pageCount: history.data?.page.totalPages ?? 0,
    onColumnFiltersChange: (updater) => {
      // A new filter starts again from the first page.
      setColumnFilters((prev) => singleSelect(typeof updater === "function" ? updater(prev) : updater));
      setPagination((prev) => ({ ...prev, pageIndex: 0 }));
    },
    onPaginationChange: setPagination,
  });

  const categoriesOptions = useMemo(
    () =>
      (categoriesQuery.data ?? []).map((category) => ({
        value: category.id,
        label: `${category.icon} ${category.name}`,
      })),
    [categoriesQuery.data]
  );

  const handleExportCSV = () => {
    // Exports the rows currently loaded (one page).
    const data = table.getRowModel().rows.map((row) => ({
      category: row.original.categoryName,
      category_icon: row.original.categoryIcon,
      description: row.original.description,
      type: row.original.type,
      amount: row.original.amount,
      formattedAmount: formatter.format(row.original.amount),
      date: row.original.date,
    }));
    if (data.length === 0) return;
    download(csvConfig)(generateCsv(csvConfig)(data));
  };

  const pageMeta = history.data?.page;

  return (
    <div className="w-full">
      <div className="flex flex-wrap items-end justify-between gap-2 py-4">
        <div className="flex gap-2">
          {table.getColumn("category") && (
            <DataTableFacetedFilter
              title="Category"
              column={table.getColumn("category")}
              options={categoriesOptions}
            />
          )}
          {table.getColumn("type") && (
            <DataTableFacetedFilter
              title="Type"
              column={table.getColumn("type")}
              options={[
                { label: "Income", value: "income" },
                { label: "Expense", value: "expense" },
              ]}
            />
          )}
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant={"outline"} size={"sm"} className="ml-auto h-8 lg:flex" onClick={handleExportCSV}>
            <DownloadIcon className="mr-2 h-4 w-4" />
            Export CSV
          </Button>
          <DataTableViewOptions table={table} />
        </div>
      </div>
      <SkeletonWrapper isLoading={history.isLoading}>
        <div className={cn("overflow-hidden rounded-2xl border bg-card/60 shadow-glass backdrop-blur-xl transition-opacity", history.isPlaceholderData && "opacity-60")}>
          <Table>
            <TableHeader>
              {table.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  {headerGroup.headers.map((header) => (
                    <TableHead key={header.id}>
                      {header.isPlaceholder ? null : flexRender(header.column.columnDef.header, header.getContext())}
                    </TableHead>
                  ))}
                </TableRow>
              ))}
            </TableHeader>
            <TableBody key={`${pagination.pageIndex}-${type ?? ""}-${categoryId ?? ""}`} className="animate-in fade-in-0 slide-in-from-bottom-1 duration-300">
              {history.isError ? (
                <TableRow>
                  <TableCell colSpan={columns.length} className="h-24 text-center text-expense">
                    Could not load transactions.
                  </TableCell>
                </TableRow>
              ) : table.getRowModel().rows?.length ? (
                table.getRowModel().rows.map((row) => (
                  <TableRow key={row.id} data-state={row.getIsSelected() && "selected"} className="transition-colors hover:bg-primary/5">
                    {row.getVisibleCells().map((cell) => (
                      <TableCell key={cell.id}>{flexRender(cell.column.columnDef.cell, cell.getContext())}</TableCell>
                    ))}
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={columns.length} className="h-24 text-center">
                    No results.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </div>
        <div className="flex items-center justify-end space-x-2 py-4">
          {pageMeta && pageMeta.totalPages > 0 && (
            <p className="mr-auto text-sm text-muted-foreground">
              Page {pageMeta.page + 1} of {pageMeta.totalPages} · {pageMeta.totalElements} transactions
            </p>
          )}
          <Button
            variant="outline"
            size="sm"
            onClick={() => table.previousPage()}
            disabled={!table.getCanPreviousPage()}
          >
            Previous
          </Button>
          <Button
            variant="outline"
            size="sm"
            onClick={() => table.nextPage()}
            disabled={!table.getCanNextPage() || history.isPlaceholderData}
          >
            Next
          </Button>
        </div>
      </SkeletonWrapper>
    </div>
  );
}

export default TransactionTable;

function RowActions({ transaction }: { transaction: Transaction }) {
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);

  return (
    <>
      <DeleteTransactionDialog
        open={showDeleteDialog}
        setOpen={setShowDeleteDialog}
        transactionId={transaction.id}
      />
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant={"ghost"} className="h-8 w-8 p-0 ">
            <span className="sr-only">Open menu</span>
            <MoreHorizontal className="h-4 w-4" />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end">
          <DropdownMenuLabel>Actions</DropdownMenuLabel>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            className="flex items-center gap-2"
            onSelect={() => {
              setShowDeleteDialog((prev) => !prev);
            }}
          >
            <TrashIcon className="h-4 w-4 text-muted-foreground" />
            Delete
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </>
  );
}
