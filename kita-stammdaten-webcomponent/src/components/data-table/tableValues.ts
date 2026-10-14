/**
 * @file tableValues.ts
 * @description Part of the DataTable.vue component
 * @generated Generated with AI assistance
 */

import type { TableCellValue, TableColumn } from "./types";

const collator = new Intl.Collator("de", {
  numeric: true,
  sensitivity: "base",
});

export function getValue<T extends object>(
  row: T,
  column: TableColumn<T>
): TableCellValue {
  if (column.value) return column.value(row);

  const value: unknown = (row as Record<string, unknown>)[column.key];
  return isCellValue(value) ? value : null;
}

export function getRowKeyValue<T extends object>(
  row: T,
  rowKey?: keyof T
): TableCellValue {
  if (rowKey === undefined) return null;
  const value: unknown = row[rowKey];
  return isCellValue(value) ? value : null;
}

function isCellValue(value: unknown): value is TableCellValue {
  return (
    value == null ||
    typeof value === "string" ||
    typeof value === "number" ||
    typeof value === "boolean" ||
    value instanceof Date
  );
}

export function displayValue(value: TableCellValue): string {
  if (value == null) return "";
  return value instanceof Date
    ? value.toLocaleDateString("de-DE")
    : String(value);
}

export function isEmpty(value: TableCellValue): boolean {
  return (
    value == null ||
    value === "" ||
    (value instanceof Date && Number.isNaN(value.getTime()))
  );
}

export function compareValues(a: TableCellValue, b: TableCellValue): number {
  if (a instanceof Date && b instanceof Date) return a.getTime() - b.getTime();
  if (typeof a === "number" && typeof b === "number") return a - b;
  return collator.compare(String(a), String(b));
}
