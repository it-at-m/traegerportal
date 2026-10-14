/**
 * @file types.ts
 * @description Part of the DataTable.vue component
 * @generated Generated with AI assistance
 */

/** Values supported by the default cell renderer and sort comparator. */
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

export type SortDirection = "asc" | "desc";

export interface SortState {
  key: string;
  direction: SortDirection;
}

/**
 * A row carries its source index through sorting and pagination.
 * `item` stays unchanged, so existing cell slots still receive the original data.
 */
export interface TableRow<T> {
  item: T;
  originalIndex: number;
  key: string;
}

export interface TableProps<T extends object> {
  items: T[];
  columns: TableColumn<T>[];
  /**
   * Optional field identifying the row. Values should be stable and unique.
   * Missing/null/invalid values fall back to the original input index.
   * Duplicate *provided* values are the caller's responsibility.
   */
  rowKey?: keyof T;
  caption?: string;
  pagination?: boolean;
  pageSize?: number;
  pageSizeOptions?: number[];
  defaultSort?: (a: T, b: T) => number;
  defaultSortKey?: string;
  defaultSortDirection?: SortDirection;
}
