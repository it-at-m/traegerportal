/**
 * @file useTableRows.ts
 * @description Part of the DataTable.vue component
 * @generated Generated with AI assistance
 */

import { computed } from "vue";

import type { TableProps, TableRow } from "./types";

/**
 * Create Vue keys BEFORE sorting/pagination.
 * A valid configured key wins; otherwise use the original array position.
 * The prefixes prevent a numeric source index colliding with an explicit key.
 */
export function createRowKey<T extends object>(
  item: T,
  index: number,
  rowKey?: keyof T
): string {
  const value: unknown = rowKey === undefined ? undefined : item[rowKey];

  if (typeof value === "string" && value !== "") {
    return `value_string_${value}`;
  }
  if (typeof value === "number" && Number.isFinite(value)) {
    return `value_number_${value}`;
  }
  if (typeof value === "boolean") {
    return `value_boolean_${value}`;
  }
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return `value_date_${value.getTime()}`;
  }

  return `index_${index}`;
}

export function useTableRows<T extends object>(
  props: Pick<TableProps<T>, "items" | "rowKey">
) {
  const rows = computed<TableRow<T>[]>(() =>
    props.items.map((item, originalIndex) => ({
      item,
      originalIndex,
      key: createRowKey(item, originalIndex, props.rowKey),
    }))
  );

  return { rows };
}
