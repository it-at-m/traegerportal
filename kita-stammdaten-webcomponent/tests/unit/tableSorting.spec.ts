import { describe, expect, it } from "vitest";
import { reactive } from "vue";

import { useTableRows } from "../../src/components/data-table/useTableRows";
import { useTableSorting } from "../../src/components/data-table/useTableSorting";

describe("sorting", () => {
  it("cycles ascending -> descending -> unsorted without rowKey", () => {
    const props = reactive({
      items: [{ title: "B" }, { title: "A" }],
      columns: [{ key: "title", label: "Titel" }],
    });
    const { rows } = useTableRows(props);
    const sorting = useTableSorting(props, rows);

    sorting.toggleSort("title");
    expect(sorting.sortedRows.value.map((row) => row.item.title)).toEqual([
      "A",
      "B",
    ]);
    sorting.toggleSort("title");
    expect(sorting.sortedRows.value.map((row) => row.item.title)).toEqual([
      "B",
      "A",
    ]);
    sorting.toggleSort("title");
    expect(sorting.sort.value).toBeNull();
    expect(sorting.sortedRows.value.map((row) => row.item.title)).toEqual([
      "B",
      "A",
    ]);
  });

  it("keeps custom default comparator and hidden rowKey sorting", () => {
    const props = reactive({
      items: [
        { id: 11, count: 3 },
        { id: 9, count: 1 },
      ],
      columns: [{ key: "count", label: "Anzahl" }],
      rowKey: "id" as const,
      defaultSort: (
        a: { id: number; count: number },
        b: { id: number; count: number }
      ) => b.count - a.count,
    });
    const { rows } = useTableRows(props);
    const sorting = useTableSorting(props, rows);
    expect(sorting.sortedRows.value.map((row) => row.item.count)).toEqual([
      3, 1,
    ]);
    sorting.toggleSort("count");
    expect(sorting.sortedRows.value.map((row) => row.item.count)).toEqual([
      1, 3,
    ]);
    sorting.toggleSort("id");
    expect(sorting.sortedRows.value.map((row) => row.item.id)).toEqual([9, 11]);
  });
});
