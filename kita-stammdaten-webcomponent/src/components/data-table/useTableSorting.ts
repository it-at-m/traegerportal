import type { ComputedRef } from "vue";

import { computed, ref } from "vue";

import type { SortState, TableColumn, TableProps, TableRow } from "./types";

import {
  compareValues,
  getRowKeyValue,
  getValue,
  isEmpty,
} from "./tableValues";

type SortingProps<T extends object> = Pick<
  TableProps<T>,
  | "rowKey"
  | "columns"
  | "defaultSort"
  | "defaultSortKey"
  | "defaultSortDirection"
>;

export function useTableSorting<T extends object>(
  props: SortingProps<T>,
  rows: ComputedRef<TableRow<T>[]>
) {
  // Preserve the previous DESC rowKey default, but only when a key exists.
  const useRowKeyDefault = computed(
    () =>
      props.rowKey !== undefined &&
      props.defaultSort === undefined &&
      props.defaultSortKey === undefined
  );

  // Without a rowKey or explicit sort configuration, preserve input order.
  const defaultSortState = computed<SortState | null>(() => {
    if (useRowKeyDefault.value) {
      return { key: String(props.rowKey), direction: "desc" };
    }

    // A custom comparator defaults to the first sortable column (ASC).
    const key =
      props.defaultSortKey ??
      (props.defaultSort
        ? props.columns.find((column) => column.sortable !== false)?.key
        : undefined);

    const column = props.columns.find(
      (candidate) => candidate.key === key && candidate.sortable !== false
    );

    return column
      ? { key: column.key, direction: props.defaultSortDirection ?? "asc" }
      : null;
  });

  // undefined: use default; null: explicitly unsorted.
  const sortOverride = ref<SortState | null | undefined>(undefined);
  const sort = computed<SortState | null>(() =>
    sortOverride.value === undefined
      ? defaultSortState.value
      : sortOverride.value
  );

  function toggleSort(key: string): void {
    if (sort.value?.key !== key) {
      sortOverride.value = { key, direction: "asc" };
    } else if (sort.value.direction === "asc") {
      sortOverride.value = { key, direction: "desc" };
    } else if (useRowKeyDefault.value) {
      // Return to the default, except when doing so would change nothing.
      sortOverride.value =
        key === String(props.rowKey) ? { key, direction: "asc" } : undefined;
    } else {
      sortOverride.value = null;
    }
  }

  function getAriaSort(
    column: TableColumn<T>
  ): "ascending" | "descending" | undefined {
    if (sort.value?.key !== column.key) return undefined;
    return sort.value.direction === "asc" ? "ascending" : "descending";
  }

  function getSortLabel(column: TableColumn<T>): string {
    if (sort.value?.key !== column.key) {
      return `${column.label} aufsteigend sortieren`;
    }
    if (sort.value.direction === "asc") {
      return `${column.label} absteigend sortieren`;
    }
    if (useRowKeyDefault.value) {
      return column.key === String(props.rowKey)
        ? `${column.label} aufsteigend sortieren`
        : `${column.label} auf Standardsortierung zurücksetzen`;
    }
    return `${column.label} Sortierung zurücksetzen`;
  }

  const sortedRows = computed<TableRow<T>[]>(() => {
    const result = [...rows.value];
    const activeSort = sort.value;
    if (!activeSort) return result;

    const column = props.columns.find((col) => col.key === activeSort.key);
    const isRowKeySort =
      props.rowKey !== undefined && activeSort.key === String(props.rowKey);

    // Keep rowKey sorting supported, even when its column is hidden/unsortable.
    if ((!column || column.sortable === false) && !isRowKeySort) {
      return result;
    }

    const comparator = props.defaultSort;
    const defaultState = defaultSortState.value;

    if (comparator && defaultState?.key === activeSort.key) {
      const factor = activeSort.direction === defaultState.direction ? 1 : -1;
      return result.sort(
        (a, b) =>
          comparator(a.item, b.item) * factor ||
          a.originalIndex - b.originalIndex
      );
    }

    const direction = activeSort.direction === "asc" ? 1 : -1;
    return result.sort((a, b) => {
      const aValue = column
        ? getValue(a.item, column)
        : getRowKeyValue(a.item, props.rowKey);
      const bValue = column
        ? getValue(b.item, column)
        : getRowKeyValue(b.item, props.rowKey);

      if (isEmpty(aValue) && isEmpty(bValue))
        return a.originalIndex - b.originalIndex;
      if (isEmpty(aValue)) return 1;
      if (isEmpty(bValue)) return -1;

      return (
        compareValues(aValue, bValue) * direction ||
        a.originalIndex - b.originalIndex
      );
    });
  });

  return { sort, toggleSort, getAriaSort, getSortLabel, sortedRows };
}
