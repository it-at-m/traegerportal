<!--
  @file DataTable.vue
  @description Reusable data table component with sorting and pagination.

  @remarks
  This component was generated with assistance from AI.
-->

<template>
  <div class="table-container">
    <div class="table-scroll">
      <table class="data-table">
        <caption
          v-if="caption"
          class="sr-only"
        >
          {{
            caption
          }}
        </caption>

        <thead>
          <tr>
            <th
              v-for="column in columns"
              :key="column.key"
              scope="col"
              :aria-sort="getAriaSort(column)"
            >
              <button
                v-if="column.sortable !== false"
                type="button"
                class="sort-button"
                :aria-label="getSortLabel(column)"
                @click="onSort(column.key)"
              >
                <span>{{ column.label }}</span>

                <span
                  v-if="sort?.key === column.key"
                  class="sort-icon"
                  :class="{ 'sort-icon--desc': sort.direction === 'desc' }"
                  aria-hidden="true"
                >
                  <muc-icon icon="arrow-up" />
                </span>
              </button>

              <span
                v-else
                class="sort-label"
              >
                <span>{{ column.label }}</span>
                <span
                  v-if="sort?.key === column.key"
                  class="sort-icon"
                  :class="{ 'sort-icon--desc': sort.direction === 'desc' }"
                  aria-hidden="true"
                >
                  <muc-icon icon="arrow-up" />
                </span>
              </span>
            </th>
          </tr>
        </thead>

        <tbody>
          <tr
            v-for="entry in visibleRows"
            :key="entry.key"
          >
            <td
              v-for="column in columns"
              :key="column.key"
            >
              <slot
                :name="`cell-${column.key}`"
                :row="entry.item"
                :column="column"
                :value="getValue(entry.item, column)"
              >
                {{ displayValue(getValue(entry.item, column)) }}
              </slot>
            </td>
          </tr>

          <tr v-if="visibleRows.length === 0">
            <td :colspan="Math.max(1, columns.length)">
              {{ emptyMessage }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Client-side pagination -->
    <div
      v-if="pagination"
      class="table-pagination"
    >
      <label class="page-size-control">
        <span>Zeilen pro Seite</span>

        <select
          v-model.number="selectedPageSize"
          class="page-size-select"
        >
          <option
            v-for="size in availablePageSizes"
            :key="size"
            :value="size"
          >
            {{ size }}
          </option>
        </select>
      </label>

      <span
        class="page-range"
        aria-live="polite"
        aria-atomic="true"
      >
        {{ firstItem }}–{{ lastItem }} von {{ items.length }}
      </span>

      <nav
        class="page-navigation"
        aria-label="Tabellenseiten"
      >
        <muc-button
          type="button"
          icon="arrow-left"
          variant="icon"
          aria-label="Vorherige Seite"
          :disabled="currentPageIndex === 0"
          @click="goToPage(currentPageIndex - 1)"
        />

        <muc-button
          type="button"
          icon="arrow-right"
          variant="icon"
          aria-label="Nächste Seite"
          :disabled="currentPageIndex >= pageCount - 1"
          @click="goToPage(currentPageIndex + 1)"
        />
      </nav>
    </div>
  </div>
</template>

<script setup lang="ts" generic="T extends object">
import { MucButton, MucIcon } from "@muenchen/muc-patternlab-vue";

import type { TableCellValue, TableColumn } from "./data-table/types";

import { displayValue, getValue } from "./data-table/tableValues";
import { useTablePagination } from "./data-table/useTablePagination";
import { useTableRows } from "./data-table/useTableRows";
import { useTableSorting } from "./data-table/useTableSorting";

const props = withDefaults(
  defineProps<{
    items: T[];
    columns: TableColumn<T>[];
    /** Optional. Without a valid key, use the original item index. */
    rowKey?: keyof T;
    caption?: string;
    pagination?: boolean;
    pageSize?: number;
    pageSizeOptions?: number[];
    defaultSort?: (a: T, b: T) => number;
    defaultSortKey?: string;
    defaultSortDirection?: "asc" | "desc";
    emptyMessage?: string;
  }>(),
  {
    rowKey: undefined,
    caption: "",
    pagination: true,
    pageSize: 10,
    pageSizeOptions: () => [5, 10, 25, 50],
    defaultSort: undefined,
    defaultSortKey: undefined,
    defaultSortDirection: "asc",
    emptyMessage: "Keine Einträge vorhanden.",
  }
);

defineSlots<
  Record<
    `cell-${string}`,
    (props: {
      row: T;
      column: TableColumn<T>;
      value: TableCellValue;
    }) => unknown
  >
>();

const { rows } = useTableRows(props);
const { sort, toggleSort, getAriaSort, getSortLabel, sortedRows } =
  useTableSorting(props, rows);
const {
  selectedPageSize,
  availablePageSizes,
  currentPageIndex,
  pageCount,
  firstItem,
  lastItem,
  visibleRows,
  goToPage,
  resetPage,
} = useTablePagination(props, sortedRows);

function onSort(key: string): void {
  toggleSort(key);
  resetPage();
}
</script>

<style scoped>
.table-container {
  --table-font-size: 0.875rem;
  width: 100%;
}

.table-scroll {
  width: 100%;
  overflow-x: auto;
}

/* Table */

.data-table {
  width: 100%;
  border-collapse: collapse;
  text-align: left;
  font-size: var(--table-font-size);
}

.data-table th,
.data-table td {
  padding: 0.5rem 0.5rem;
  border-bottom: 0.0625rem solid #ddd;
  vertical-align: middle;
  font-size: inherit;
}

.data-table th {
  font-weight: 600;
  background-color: #f5f5f5;
  white-space: nowrap;
}

.data-table tbody tr:hover {
  background-color: #f9f9f9;
}

/* Sorting */

.sort-button {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  width: auto;
  padding: 0.25em 0;
  border: none;
  background: transparent;
  color: inherit;
  font: inherit;
  font-weight: 600;
  text-align: left;
  cursor: pointer;
  white-space: nowrap;
}

.sort-button:hover {
  text-decoration: none;
}

.sort-button:focus-visible,
.page-size-select:focus-visible {
  outline: 0.125rem solid currentColor;
  outline-offset: 0.1875rem;
}

/* Sort icon */

.sort-label {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
}

.sort-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 1em;
  width: 1em;
  height: 1em;
  color: inherit;
  transition: transform 0.2s ease;
}

.sort-icon :deep(svg) {
  display: block;
  width: 1em !important;
  height: 1em !important;
  max-width: 1em;
  max-height: 1em;
}

/* Descending sort */

.sort-icon--desc {
  transform: rotate(180deg);
}

/* Reduced motion */

@media (prefers-reduced-motion: reduce) {
  .sort-icon {
    transition: none;
  }
}

/* Pagination */

.table-pagination {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  flex-wrap: wrap;
  gap: 1.5rem;
  padding: 0.75rem 1rem;
  font-size: var(--table-font-size);
}

.page-size-control,
.page-navigation {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
}

.page-size-select {
  min-width: 4rem;
  padding: 0.375rem 0.5rem;
  border: 0.0625rem solid #777;
  border-radius: 0.25rem;
  background: transparent;
  color: inherit;
  font: inherit;
  cursor: pointer;
}

.page-range {
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

/* Accessibility */

.sr-only {
  position: absolute;
  width: 0.0625rem;
  height: 0.0625rem;
  padding: 0;
  margin: -0.0625rem;
  overflow: hidden;
  clip-path: inset(50%);
  white-space: nowrap;
  border: 0;
}

/* Responsive layout */

@media (max-width: 37.5rem) {
  .table-pagination {
    justify-content: space-between;
    gap: 0.75rem;
  }
}
</style>
```
