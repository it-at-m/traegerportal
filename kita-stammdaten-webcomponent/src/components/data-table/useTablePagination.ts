import type { ComputedRef } from "vue";

import { computed, ref, watch } from "vue";

import type { TableProps, TableRow } from "./types";

type PaginationProps<T extends object> = Pick<
  TableProps<T>,
  "items" | "pagination" | "pageSize" | "pageSizeOptions"
>;

export function validPageSize(value: number): boolean {
  return Number.isSafeInteger(value) && value > 0;
}

export function useTablePagination<T extends object>(
  props: PaginationProps<T>,
  sortedRows: ComputedRef<TableRow<T>[]>
) {
  const page = ref(0);
  const selectedPageSize = ref(
    props.pageSize !== undefined && validPageSize(props.pageSize)
      ? props.pageSize
      : 10
  );

  const availablePageSizes = computed(() =>
    [
      ...new Set(
        [...(props.pageSizeOptions ?? []), selectedPageSize.value].filter(
          validPageSize
        )
      ),
    ].sort((a, b) => a - b)
  );

  const pageCount = computed(() =>
    Math.max(1, Math.ceil(sortedRows.value.length / selectedPageSize.value))
  );
  const currentPageIndex = computed(() =>
    Math.min(page.value, pageCount.value - 1)
  );
  const firstItem = computed(() =>
    props.items.length === 0
      ? 0
      : currentPageIndex.value * selectedPageSize.value + 1
  );
  const lastItem = computed(() =>
    Math.min(
      (currentPageIndex.value + 1) * selectedPageSize.value,
      props.items.length
    )
  );
  const visibleRows = computed(() => {
    if (!props.pagination) return sortedRows.value;
    const start = currentPageIndex.value * selectedPageSize.value;
    return sortedRows.value.slice(start, start + selectedPageSize.value);
  });

  function goToPage(index: number): void {
    page.value = Math.max(0, Math.min(index, pageCount.value - 1));
  }

  function resetPage(): void {
    page.value = 0;
  }

  watch(() => props.pagination, resetPage);
  watch(
    () => props.pageSize,
    (size) => {
      selectedPageSize.value =
        size !== undefined && validPageSize(size) ? size : 10;
      resetPage();
    }
  );
  watch(selectedPageSize, resetPage);
  watch([() => props.items, () => props.items.length], resetPage);

  return {
    selectedPageSize,
    availablePageSizes,
    pageCount,
    currentPageIndex,
    firstItem,
    lastItem,
    visibleRows,
    goToPage,
    resetPage,
  };
}
