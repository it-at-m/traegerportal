import { describe, expect, it } from "vitest";
import { reactive } from "vue";

import { useTablePagination } from "../../src/components/data-table/useTablePagination";
import {
  createRowKey,
  useTableRows,
} from "../../src/components/data-table/useTableRows";
import { useTableSorting } from "../../src/components/data-table/useTableSorting";

describe("table row identity", () => {
  it("uses the original input index when rowKey is omitted", () => {
    const items = [{ name: "C" }, { name: "A" }, { name: "B" }];
    const props = reactive({
      items,
      columns: [{ key: "name", label: "Name" }],
      pagination: true,
      pageSize: 2,
    });
    const { rows } = useTableRows(props);
    const sorting = useTableSorting(props, rows);
    const pagination = useTablePagination(props, sorting.sortedRows);

    expect(rows.value.map((row) => row.key)).toEqual([
      "index_0",
      "index_1",
      "index_2",
    ]);
    expect(sorting.sort.value).toBeNull();

    sorting.toggleSort("name");
    expect(
      pagination.visibleRows.value.map((row) => [row.item.name, row.key])
    ).toEqual([
      ["A", "index_1"],
      ["B", "index_2"],
    ]);
    pagination.goToPage(1);
    expect(
      pagination.visibleRows.value.map((row) => [row.item.name, row.key])
    ).toEqual([["C", "index_0"]]);
  });

  it("uses rowKey when present and keeps the old descending default", () => {
    const props = reactive({
      items: [{ id: 1 }, { id: 3 }, { id: 2 }],
      columns: [{ key: "id", label: "ID" }],
      rowKey: "id" as const,
    });
    const { rows } = useTableRows(props);
    const { sortedRows, sort } = useTableSorting(props, rows);
    expect(sort.value).toEqual({ key: "id", direction: "desc" });
    expect(sortedRows.value.map((row) => [row.item.id, row.key])).toEqual([
      [3, "value_number_3"],
      [2, "value_number_2"],
      [1, "value_number_1"],
    ]);
  });

  it("falls back to source index for missing/empty/invalid values", () => {
    expect(createRowKey({ id: null }, 4, "id")).toBe("index_4");
    expect(createRowKey({ id: undefined }, 5, "id")).toBe("index_5");
    expect(createRowKey({ id: "" }, 6, "id")).toBe("index_6");
    expect(createRowKey({ id: NaN }, 7, "id")).toBe("index_7");
    expect(createRowKey({ id: new Date("invalid") }, 8, "id")).toBe("index_8");
    expect(createRowKey({ id: { x: 1 } }, 9, "id")).toBe("index_9");
  });

  it("does not mask duplicate supplied identifiers", () => {
    expect(createRowKey({ id: "same" }, 0, "id")).toBe(
      createRowKey({ id: "same" }, 1, "id")
    );
  });

  it("distinguishes explicit values from fallback indexes and primitive types", () => {
    expect(createRowKey({ id: "0" }, 0, "id")).not.toBe(
      createRowKey({ id: null }, 0, "id")
    );
    expect(createRowKey({ id: 1 }, 0, "id")).not.toBe(
      createRowKey({ id: "1" }, 0, "id")
    );
  });

  it("preserves the original item in the row wrapper", () => {
    const item = { name: "A" };
    const { rows } = useTableRows({ items: [item] });
    expect(rows.value[0].item).toBe(item);
  });
});
