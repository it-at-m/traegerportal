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
                @click="toggleSort(column.key)"
              >
                <span>{{ column.label }}</span>

                <span
                  v-if="sort?.key === column.key"
                  class="sort-icon sort-icon--active"
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
                  class="sort-icon sort-icon--active"
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
            v-for="row in visibleRows"
            :key="String(row[rowKey])"
          >
            <td
              v-for="column in columns"
              :key="column.key"
            >
              <slot
                :name="`cell-${column.key}`"
                :row="row"
                :column="column"
                :value="getValue(row, column)"
              >
                {{ displayValue(getValue(row, column)) }}
              </slot>
            </td>
          </tr>

          <tr v-if="visibleRows.length === 0">
            <td :colspan="Math.max(1, columns.length)">
              Keine Einträge vorhanden.
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

<script lang="ts">
export type TableCellValue =
  | string
  | number
  | boolean
  | Date
  | null
  | undefined;

export interface TableColumn<T> {
  key: string;
  label: string;
  sortable?: boolean;
  value?: (row: T) => TableCellValue;
}
</script>

<script setup lang="ts" generic="T extends object">
import { MucButton, MucIcon } from "@muenchen/muc-patternlab-vue";
import { computed, ref, watch } from "vue";

const props = withDefaults(
  defineProps<{
    items: T[];
    columns: TableColumn<T>[];
    rowKey: keyof T;
    caption?: string;
    pagination?: boolean;
    pageSize?: number;
    pageSizeOptions?: number[];
    defaultSort?: (a: T, b: T) => number;
    defaultSortKey?: string;
    defaultSortDirection?: "asc" | "desc";
  }>(),
  {
    caption: "",
    pagination: true,
    pageSize: 10,
    pageSizeOptions: () => [5, 10, 25, 50],
    defaultSort: undefined,
    defaultSortKey: undefined,
    defaultSortDirection: "asc",
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

type SortDirection = "asc" | "desc";

interface SortState {
  key: string;
  direction: SortDirection;
}

// Default to rowKey DESC, even if the column is hidden.
const useRowKeyFallback = computed(
  () => props.defaultSort === undefined && props.defaultSortKey === undefined
);

// A custom comparator defaults to the first sortable column (ASC).
const defaultSortState = computed<SortState | null>(() => {
  if (useRowKeyFallback.value) {
    return { key: String(props.rowKey), direction: "desc" };
  }

  const key =
    props.defaultSortKey ??
    props.columns.find((column) => column.sortable !== false)?.key;

  const column = props.columns.find(
    (column) => column.key === key && column.sortable !== false
  );

  return column
    ? { key: column.key, direction: props.defaultSortDirection }
    : null;
});

// undefined: default sort; null: unsorted.
const sortOverride = ref<SortState | null | undefined>(undefined);
const sort = computed<SortState | null>(() =>
  sortOverride.value === undefined ? defaultSortState.value : sortOverride.value
);
const page = ref(0);

const collator = new Intl.Collator("de", {
  numeric: true,
  sensitivity: "base",
});

// Cell values

function getValue(row: T, column: TableColumn<T>): TableCellValue {
  if (column.value) {
    return column.value(row);
  }

  const value = (row as Record<string, unknown>)[column.key];

  if (
    typeof value === "string" ||
    typeof value === "number" ||
    typeof value === "boolean" ||
    value instanceof Date
  ) {
    return value;
  }

  return null;
}

function getRowKeyValue(row: T): TableCellValue {
  const value: unknown = row[props.rowKey];

  if (
    typeof value === "string" ||
    typeof value === "number" ||
    typeof value === "boolean" ||
    value instanceof Date
  ) {
    return value;
  }

  return null;
}

function displayValue(value: TableCellValue): string {
  if (value == null) return "";

  return value instanceof Date
    ? value.toLocaleDateString("de-DE")
    : String(value);
}

// Sorting

function toggleSort(key: string): void {
  if (sort.value?.key !== key) {
    sortOverride.value = { key, direction: "asc" };
  } else if (sort.value.direction === "asc") {
    sortOverride.value = { key, direction: "desc" };
  } else if (useRowKeyFallback.value) {
    // Restore rowKey DESC; toggle rowKey to ASC to avoid a no-op.
    sortOverride.value =
      key === String(props.rowKey) ? { key, direction: "asc" } : undefined;
  } else {
    sortOverride.value = null;
  }

  page.value = 0;
}

function getAriaSort(column: TableColumn<T>) {
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

  if (useRowKeyFallback.value) {
    return column.key === String(props.rowKey)
      ? `${column.label} aufsteigend sortieren`
      : `${column.label} auf Standardsortierung zurücksetzen`;
  }

  return `${column.label} Sortierung zurücksetzen`;
}

function isEmpty(value: TableCellValue): boolean {
  return (
    value == null ||
    value === "" ||
    (value instanceof Date && Number.isNaN(value.getTime()))
  );
}

function compareValues(a: TableCellValue, b: TableCellValue): number {
  if (a instanceof Date && b instanceof Date) {
    return a.getTime() - b.getTime();
  }

  if (typeof a === "number" && typeof b === "number") {
    return a - b;
  }

  return collator.compare(String(a), String(b));
}

const sortedItems = computed<T[]>(() => {
  const result = [...props.items];
  const activeSort = sort.value;

  // Keep the original order when sorting is cleared.
  if (!activeSort) return result;

  const column = props.columns.find((col) => col.key === activeSort.key);
  const isRowKeySort = activeSort.key === String(props.rowKey);

  // Allow rowKey sorting even if its column is hidden or not sortable.
  if ((!column || column.sortable === false) && !isRowKeySort) {
    return result;
  }

  // Reuse the default comparator when its column is selected again.
  const comparator = props.defaultSort;
  const defaultState = defaultSortState.value;

  if (comparator && defaultState?.key === activeSort.key) {
    const factor = activeSort.direction === defaultState.direction ? 1 : -1;
    return result.sort((a, b) => comparator(a, b) * factor);
  }

  const direction = activeSort.direction === "asc" ? 1 : -1;

  return result.sort((a, b) => {
    const valueA = column ? getValue(a, column) : getRowKeyValue(a);
    const valueB = column ? getValue(b, column) : getRowKeyValue(b);

    if (isEmpty(valueA) && isEmpty(valueB)) return 0;
    if (isEmpty(valueA)) return 1;
    if (isEmpty(valueB)) return -1;

    return compareValues(valueA, valueB) * direction;
  });
});

// Client-side pagination

function validPageSize(value: number): boolean {
  return Number.isSafeInteger(value) && value > 0;
}

const selectedPageSize = ref(
  validPageSize(props.pageSize) ? props.pageSize : 10
);

const availablePageSizes = computed(() =>
  [
    ...new Set(
      [...props.pageSizeOptions, selectedPageSize.value].filter(validPageSize)
    ),
  ].sort((a, b) => a - b)
);

const pageCount = computed(() =>
  Math.max(1, Math.ceil(sortedItems.value.length / selectedPageSize.value))
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
  if (!props.pagination) return sortedItems.value;

  const start = currentPageIndex.value * selectedPageSize.value;

  return sortedItems.value.slice(start, start + selectedPageSize.value);
});

function goToPage(index: number): void {
  page.value = Math.max(0, Math.min(index, pageCount.value - 1));
}

// Pagination toggled
watch(
  () => props.pagination,
  () => {
    page.value = 0;
  }
);

// External page size changed
watch(
  () => props.pageSize,
  (size) => {
    selectedPageSize.value = validPageSize(size) ? size : 10;
    page.value = 0;
  }
);

// Page size selected
watch(selectedPageSize, () => {
  page.value = 0;
});

// Items changed
watch([() => props.items, () => props.items.length], () => {
  page.value = 0;
});
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

  /* Vuetify high-emphasis surface color. */
  color: inherit;
  color: rgba(var(--v-theme-on-surface), var(--v-high-emphasis-opacity));
  opacity: 0;
  transition: transform 0.2s ease;
}

.sort-icon :deep(svg) {
  display: block;
  width: 1em !important;
  height: 1em !important;
  max-width: 1em;
  max-height: 1em;
}

/* Show active icons only, including on hover. */

.sort-icon--active {
  opacity: 1;
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