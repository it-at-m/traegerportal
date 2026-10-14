/**
 * @file DataTable.spec.js
 * @description Unit tests for DataTable.
 *
 * @generated This tests were generated with assistance from AI.
 */

// @vitest-environment jsdom
// Requires Vitest, Vue Test Utils, and jsdom.
import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { h, nextTick, reactive } from "vue";

import DataTable from "../../src/components/DataTable.vue";

// Mock design system components with native elements for DOM tests.
vi.mock("@muenchen/muc-patternlab-vue", async () => {
  const { defineComponent, h: createElement } = await import("vue");

  return {
    MucButton: defineComponent({
      name: "MucButton",
      inheritAttrs: false,
      props: {
        type: String,
        icon: String,
        variant: String,
        disabled: Boolean,
      },
      setup(props, { attrs, slots }) {
        return () =>
          createElement(
            "button",
            { ...attrs, type: props.type, disabled: props.disabled },
            slots.default?.()
          );
      },
    }),
    MucIcon: defineComponent({
      name: "MucIcon",
      props: { icon: String },
      setup() {
        return () =>
          createElement("svg", { class: "mock-icon", "aria-hidden": "true" });
      },
    }),
  };
});

function makeRows() {
  // Intentionally unsorted.
  return [
    { id: 3, name: "Zoe", age: 40, active: true },
    { id: 1, name: "Anna", age: 22, active: false },
    { id: 4, name: "Berta", age: 31, active: true },
    { id: 2, name: "Clara", age: 22, active: false },
  ];
}

function makeColumns() {
  return [
    { key: "id", label: "ID" },
    { key: "name", label: "Name" },
    { key: "age", label: "Alter" },
    { key: "active", label: "Aktiv", sortable: false },
  ];
}

function makeManyRows(count) {
  return Array.from({ length: count }, (_, index) => ({
    id: index + 1,
    name: `Person ${index + 1}`,
    age: 20 + index,
    active: index % 2 === 0,
  }));
}

function mountTable(props = {}, options = {}) {
  return mount(DataTable, {
    props: {
      items: makeRows(),
      columns: makeColumns(),
      rowKey: "id",
      ...props,
    },
    ...options,
  });
}

function firstColumnValues(wrapper) {
  return wrapper.findAll("tbody tr").map((row) => row.findAll("td")[0]?.text());
}

function rowCells(wrapper) {
  return wrapper
    .findAll("tbody tr")
    .map((row) => row.findAll("td").map((cell) => cell.text()));
}

function header(wrapper, label) {
  const result = wrapper
    .findAll("thead th")
    .find((th) => th.text().trim() === label);
  expect(result, `Column ${label} is missing`).toBeDefined();
  return result;
}

function sortButton(wrapper, label) {
  return header(wrapper, label).get("button.sort-button");
}

function nextButton(wrapper) {
  return wrapper.get('button[aria-label="Nächste Seite"]');
}

function previousButton(wrapper) {
  return wrapper.get('button[aria-label="Vorherige Seite"]');
}

function rangeText(wrapper) {
  return wrapper.get(".page-range").text();
}

function availableSizes(wrapper) {
  return wrapper
    .findAll("select.page-size-select option")
    .map((option) => Number(option.element.value));
}

describe("DataTable - rendering and cells", () => {
  it('renders each column label and scope="col"', () => {
    const wrapper = mountTable();

    expect(wrapper.findAll("thead th").map((th) => th.text())).toEqual([
      "ID",
      "Name",
      "Alter",
      "Aktiv",
    ]);
    expect(
      wrapper
        .findAll("thead th")
        .every((th) => th.attributes("scope") === "col")
    ).toBe(true);
  });

  it("renders all rows in the default descending ID order", () => {
    const wrapper = mountTable({ pagination: false });

    expect(rowCells(wrapper)).toEqual([
      ["4", "Berta", "31", "true"],
      ["3", "Zoe", "40", "true"],
      ["2", "Clara", "22", "false"],
      ["1", "Anna", "22", "false"],
    ]);
  });

  it("renders a caption only when provided", async () => {
    const wrapper = mountTable({ caption: "Mitarbeitendenübersicht" });

    expect(wrapper.get("caption").text()).toBe("Mitarbeitendenübersicht");
    expect(wrapper.get("caption").classes()).toContain("sr-only");

    await wrapper.setProps({ caption: "" });
    expect(wrapper.find("caption").exists()).toBe(false);
  });

  it("omits the caption element when unset", () => {
    expect(mountTable().find("caption").exists()).toBe(false);
  });

  it("renders null and undefined as empty cells", () => {
    const wrapper = mountTable({
      items: [{ id: 1, name: null, age: undefined }],
      columns: makeColumns().slice(0, 3),
    });

    expect(rowCells(wrapper)).toEqual([["1", "", ""]]);
  });

  it("renders numbers and booleans as strings", () => {
    const wrapper = mountTable({
      items: [{ id: 10, age: 0, active: false }],
      columns: [
        { key: "id", label: "ID" },
        { key: "age", label: "Alter" },
        { key: "active", label: "Aktiv" },
      ],
    });

    expect(rowCells(wrapper)).toEqual([["10", "0", "false"]]);
  });

  it("formats dates using the German locale", () => {
    const date = new Date(2025, 1, 3); // Feb 3, 2025 (local).
    const wrapper = mountTable({
      items: [{ id: 1, date }],
      columns: [{ key: "date", label: "Datum" }],
    });

    expect(firstColumnValues(wrapper)).toEqual([
      date.toLocaleDateString("de-DE"),
    ]);
  });

  it("renders unsupported objects and arrays as empty cells", () => {
    const wrapper = mountTable({
      items: [{ id: 1, metadata: { a: 1 }, tags: ["a", "b"] }],
      columns: [
        { key: "id", label: "ID" },
        { key: "metadata", label: "Meta" },
        { key: "tags", label: "Tags" },
      ],
    });

    expect(rowCells(wrapper)).toEqual([["1", "", ""]]);
  });

  it("renders missing cell values as empty", () => {
    const wrapper = mountTable({
      items: [{ id: 1 }],
      columns: [{ key: "missing", label: "Fehlt" }],
    });

    expect(firstColumnValues(wrapper)).toEqual([""]);
  });

  it("uses column.value with the original row", () => {
    const value = vi.fn((row) => `${row.name} (${row.age})`);
    const wrapper = mountTable({
      columns: [
        { key: "id", label: "ID" },
        { key: "name", label: "Name", value },
      ],
    });

    expect(rowCells(wrapper).map((row) => row[1])).toEqual([
      "Berta (31)",
      "Zoe (40)",
      "Clara (22)",
      "Anna (22)",
    ]);
    expect(value).toHaveBeenCalledWith(
      expect.objectContaining({ id: 4, name: "Berta" })
    );
  });

  it("passes row, column, and value to a named cell slot", () => {
    const slot = vi.fn(({ row, column, value }) =>
      h(
        "strong",
        { class: "custom-name" },
        `${column.label}: ${value} #${row.id}`
      )
    );
    const wrapper = mountTable({}, { slots: { "cell-name": slot } });

    expect(
      wrapper.findAll("strong.custom-name").map((el) => el.text())
    ).toEqual([
      "Name: Berta #4",
      "Name: Zoe #3",
      "Name: Clara #2",
      "Name: Anna #1",
    ]);
    expect(slot).toHaveBeenCalledWith(
      expect.objectContaining({
        row: expect.objectContaining({ id: 4, name: "Berta" }),
        column: expect.objectContaining({ key: "name", label: "Name" }),
        value: "Berta",
      })
    );
  });

  it("passes the computed column value to the cell slot", () => {
    const wrapper = mountTable(
      {
        columns: [
          { key: "id", label: "ID" },
          {
            key: "name",
            label: "Name",
            value: (row) => row.name.toUpperCase(),
          },
        ],
      },
      {
        slots: {
          "cell-name": ({ value }) =>
            h("em", { class: "computed-value" }, value),
        },
      }
    );

    expect(wrapper.findAll("em.computed-value").map((el) => el.text())).toEqual(
      ["BERTA", "ZOE", "CLARA", "ANNA"]
    );
  });

  it("renders the empty state with the correct colspan", () => {
    const wrapper = mountTable({ items: [] });

    expect(wrapper.findAll("tbody tr")).toHaveLength(1);
    expect(wrapper.get("tbody td").text()).toBe("Keine Einträge vorhanden.");
    expect(wrapper.get("tbody td").attributes("colspan")).toBe("4");
    expect(rangeText(wrapper)).toBe("0–0 von 0");
  });

  it("uses a minimum colspan of one without columns", () => {
    const wrapper = mountTable({ items: [], columns: [] });

    expect(wrapper.findAll("thead th")).toHaveLength(0);
    expect(wrapper.get("tbody td").attributes("colspan")).toBe("1");
  });

  it("removes the empty state after rows are added", async () => {
    const wrapper = mountTable({ items: [] });

    await wrapper.setProps({ items: makeRows() });
    expect(wrapper.find("tbody").text()).not.toContain(
      "Keine Einträge vorhanden."
    );
    expect(wrapper.findAll("tbody tr")).toHaveLength(4);
  });
});

describe("DataTable - default sorting and sort state", () => {
  it("sorts by rowKey descending by default", () => {
    const wrapper = mountTable({ pagination: false });

    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("descending");
    expect(header(wrapper, "Name").attributes("aria-sort")).toBeUndefined();
  });

  it("sorts by rowKey when the key is not a column", () => {
    const wrapper = mountTable({
      pagination: false,
      columns: [{ key: "name", label: "Name" }],
    });

    expect(firstColumnValues(wrapper)).toEqual([
      "Berta",
      "Zoe",
      "Clara",
      "Anna",
    ]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBeUndefined();
  });

  it("sorts by rowKey even when its column is not sortable", () => {
    const wrapper = mountTable({
      columns: [
        { key: "id", label: "ID", sortable: false },
        { key: "name", label: "Name" },
      ],
    });

    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("descending");
    expect(header(wrapper, "ID").find("button").exists()).toBe(false);
  });

  it("sorts numbered rowKey strings naturally", () => {
    const wrapper = mountTable({
      items: [
        { id: "item2", title: "B" },
        { id: "item10", title: "C" },
        { id: "item1", title: "A" },
      ],
      rowKey: "id",
      columns: [{ key: "title", label: "Titel" }],
    });

    expect(firstColumnValues(wrapper)).toEqual(["C", "B", "A"]);
  });

  it("changes the default rowKey sort from DESC to ASC", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "ID").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("ascending");
  });

  it("changes the rowKey sort from ASC back to DESC", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "ID").trigger("click");
    await sortButton(wrapper, "ID").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
  });

  it("sorts another column ASC and then DESC", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "4", "2", "3"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("ascending");
    expect(header(wrapper, "ID").attributes("aria-sort")).toBeUndefined();

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("descending");
  });

  it("restores the default ID DESC order on the third click", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Name").trigger("click");

    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("descending");
    expect(header(wrapper, "Name").attributes("aria-sort")).toBeUndefined();
  });

  it("starts a new column sort in ascending order", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Alter").trigger("click");

    // Equal ages retain input order (IDs 1, 2).
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "4", "3"]);
    expect(header(wrapper, "Alter").attributes("aria-sort")).toBe("ascending");
  });

  it("applies an ascending defaultSortKey", () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    expect(firstColumnValues(wrapper)).toEqual(["1", "4", "2", "3"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("ascending");
  });

  it("applies a descending defaultSortKey", () => {
    const wrapper = mountTable({
      defaultSortKey: "name",
      defaultSortDirection: "desc",
    });

    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("descending");
  });

  it("cycles ASC, DESC, and unsorted without a rowKey default", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });
    expect(firstColumnValues(wrapper)).toEqual(["1", "4", "2", "3"]);

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("keeps input order for an unknown defaultSortKey", () => {
    const wrapper = mountTable({ defaultSortKey: "does-not-exist" });

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("keeps input order for a non-sortable defaultSortKey", () => {
    const wrapper = mountTable({ defaultSortKey: "active" });

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("omits sorting controls for non-sortable columns", () => {
    const wrapper = mountTable();

    expect(header(wrapper, "Aktiv").find("button").exists()).toBe(false);
    expect(header(wrapper, "Aktiv").get(".sort-label").text()).toBe("Aktiv");
    expect(wrapper.findAll("thead button.sort-button")).toHaveLength(3);
  });

  it("shows a sort icon only on the active column", async () => {
    const wrapper = mountTable();

    expect(wrapper.findAll(".sort-icon")).toHaveLength(1);
    expect(header(wrapper, "ID").get(".sort-icon").classes()).toContain(
      "sort-icon--desc"
    );

    await sortButton(wrapper, "Name").trigger("click");
    expect(wrapper.findAll(".sort-icon")).toHaveLength(1);
    expect(header(wrapper, "Name").get(".sort-icon").classes()).not.toContain(
      "sort-icon--desc"
    );
    expect(header(wrapper, "ID").find(".sort-icon").exists()).toBe(false);
  });

  it("reflects sort direction and hides icons from screen readers", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    expect(
      header(wrapper, "Name").get(".sort-icon").attributes("aria-hidden")
    ).toBe("true");
    expect(header(wrapper, "Name").get(".sort-icon").classes()).not.toContain(
      "sort-icon--desc"
    );

    await sortButton(wrapper, "Name").trigger("click");
    expect(header(wrapper, "Name").get(".sort-icon").classes()).toContain(
      "sort-icon--desc"
    );
  });

  it("updates localized sort labels during the default sort cycle", async () => {
    const wrapper = mountTable();

    expect(sortButton(wrapper, "ID").attributes("aria-label")).toBe(
      "ID aufsteigend sortieren"
    );
    expect(sortButton(wrapper, "Name").attributes("aria-label")).toBe(
      "Name aufsteigend sortieren"
    );

    await sortButton(wrapper, "ID").trigger("click");
    expect(sortButton(wrapper, "ID").attributes("aria-label")).toBe(
      "ID absteigend sortieren"
    );

    await sortButton(wrapper, "Name").trigger("click");
    expect(sortButton(wrapper, "Name").attributes("aria-label")).toBe(
      "Name absteigend sortieren"
    );

    await sortButton(wrapper, "Name").trigger("click");
    expect(sortButton(wrapper, "Name").attributes("aria-label")).toBe(
      "Name auf Standardsortierung zurücksetzen"
    );
  });

  it("uses the reset label without default rowKey sorting", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    await sortButton(wrapper, "Name").trigger("click");
    expect(sortButton(wrapper, "Name").attributes("aria-label")).toBe(
      "Name Sortierung zurücksetzen"
    );
  });
});

describe("DataTable - value comparison", () => {
  it("sorts numbers numerically", () => {
    const wrapper = mountTable({
      items: [
        { id: 1, score: 10 },
        { id: 2, score: 2 },
        { id: 3, score: -1 },
      ],
      columns: [{ key: "score", label: "Punkte" }],
      defaultSortKey: "score",
    });

    expect(firstColumnValues(wrapper)).toEqual(["-1", "2", "10"]);
  });

  it("sorts strings with numbers naturally", () => {
    const wrapper = mountTable({
      items: [
        { id: 1, code: "A10" },
        { id: 2, code: "a2" },
        { id: 3, code: "A1" },
      ],
      columns: [{ key: "code", label: "Code" }],
      defaultSortKey: "code",
    });

    expect(firstColumnValues(wrapper)).toEqual(["A1", "a2", "A10"]);
  });

  it("compares strings case-insensitively", () => {
    const wrapper = mountTable({
      items: [
        { id: 1, word: "zebra" },
        { id: 2, word: "Anna" },
        { id: 3, word: "birne" },
      ],
      columns: [{ key: "word", label: "Wort" }],
      defaultSortKey: "word",
    });

    expect(firstColumnValues(wrapper)).toEqual(["Anna", "birne", "zebra"]);
  });

  it("uses the German collator with base sensitivity", () => {
    const wrapper = mountTable({
      items: [
        { id: 1, word: "Müller" },
        { id: 2, word: "muller" },
        { id: 3, word: "Schmidt" },
      ],
      columns: [
        { key: "id", label: "ID" },
        { key: "word", label: "Wort" },
      ],
      defaultSortKey: "word",
    });

    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3"]);
  });

  it("sorts dates by timestamp in both directions", async () => {
    const wrapper = mountTable({
      items: [
        { id: 1, date: new Date(2025, 5, 1) },
        { id: 2, date: new Date(2023, 0, 1) },
        { id: 3, date: new Date(2024, 3, 10) },
      ],
      columns: [
        { key: "id", label: "ID" },
        { key: "date", label: "Datum" },
      ],
      defaultSortKey: "date",
    });

    expect(firstColumnValues(wrapper)).toEqual(["2", "3", "1"]);
    await sortButton(wrapper, "Datum").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "3", "2"]);
  });

  it("keeps empty values last when sorting ascending", () => {
    const wrapper = mountTable({
      items: [
        { id: 1, score: null },
        { id: 2, score: 4 },
        { id: 3, score: "" },
        { id: 4, score: 1 },
        { id: 5, score: undefined },
      ],
      columns: [
        { key: "id", label: "ID" },
        { key: "score", label: "Punkte" },
      ],
      defaultSortKey: "score",
    });

    expect(firstColumnValues(wrapper)).toEqual(["4", "2", "1", "3", "5"]);
  });

  it("keeps empty values last when sorting descending", () => {
    const wrapper = mountTable({
      items: [
        { id: 1, score: null },
        { id: 2, score: 4 },
        { id: 3, score: "" },
        { id: 4, score: 1 },
        { id: 5, score: undefined },
      ],
      columns: [
        { key: "id", label: "ID" },
        { key: "score", label: "Punkte" },
      ],
      defaultSortKey: "score",
      defaultSortDirection: "desc",
    });

    expect(firstColumnValues(wrapper)).toEqual(["2", "4", "1", "3", "5"]);
  });

  it("treats invalid dates as empty sort values", () => {
    const wrapper = mountTable({
      items: [
        { id: 1, date: new Date("invalid") },
        { id: 2, date: new Date(2025, 0, 1) },
        { id: 3, date: new Date(2024, 0, 1) },
      ],
      columns: [
        { key: "id", label: "ID" },
        { key: "date", label: "Datum" },
      ],
      defaultSortKey: "date",
    });

    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "1"]);
  });

  it("preserves input order for equal values", () => {
    const wrapper = mountTable({
      items: [
        { id: 3, score: 1 },
        { id: 1, score: 1 },
        { id: 2, score: 2 },
      ],
      columns: [
        { key: "id", label: "ID" },
        { key: "score", label: "Punkte" },
      ],
      defaultSortKey: "score",
    });

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "2"]);
  });

  it("sorts using values returned by column.value", () => {
    const wrapper = mountTable({
      columns: [
        { key: "id", label: "ID" },
        { key: "length", label: "Länge", value: (row) => row.name.length },
      ],
      defaultSortKey: "length",
    });

    // Name lengths; equal values retain input order.
    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
  });

  it("sorts booleans by their string representation", () => {
    const wrapper = mountTable({
      items: [
        { id: 1, ok: true },
        { id: 2, ok: false },
      ],
      columns: [
        { key: "id", label: "ID" },
        { key: "ok", label: "OK" },
      ],
      defaultSortKey: "ok",
    });

    expect(firstColumnValues(wrapper)).toEqual(["2", "1"]);
  });

  it("does not mutate input items while sorting", async () => {
    const items = makeRows();
    const originalIds = items.map((row) => row.id);
    const originalObjects = [...items];
    const wrapper = mountTable({ items, defaultSortKey: "name" });

    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Alter").trigger("click");

    expect(items.map((row) => row.id)).toEqual(originalIds);
    expect(items.every((row, index) => row === originalObjects[index])).toBe(
      true
    );
  });
});

describe("DataTable - custom default comparator", () => {
  it("chooses the first sortable column without defaultSortKey", () => {
    const comparator = vi.fn((a, b) => a.id - b.id);
    const wrapper = mountTable({
      columns: [
        { key: "id", label: "ID", sortable: false },
        { key: "name", label: "Name" },
        { key: "age", label: "Alter" },
      ],
      defaultSort: comparator,
    });

    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("ascending");
    expect(comparator).toHaveBeenCalled();
  });

  it("uses the comparator instead of column values", () => {
    const comparator = vi.fn((a, b) => a.id - b.id);
    const wrapper = mountTable({
      defaultSort: comparator,
      defaultSortKey: "name",
    });

    // Sorts by ID, not name.
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4"]);
    expect(comparator).toHaveBeenCalled();
  });

  it("reverses the comparator when switching direction", async () => {
    const wrapper = mountTable({
      defaultSort: (a, b) => a.id - b.id,
      defaultSortKey: "name",
    });

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
  });

  it("restores input order when sorting is cleared", async () => {
    const wrapper = mountTable({
      defaultSort: (a, b) => a.id - b.id,
      defaultSortKey: "name",
    });

    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Name").trigger("click");

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("reuses the comparator after switching columns", async () => {
    const wrapper = mountTable({
      defaultSort: (a, b) => a.id - b.id,
      defaultSortKey: "name",
    });

    await sortButton(wrapper, "Alter").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "4", "3"]);
    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4"]);
  });

  it("respects the configured default comparator direction", async () => {
    const wrapper = mountTable({
      defaultSort: (a, b) => a.id - b.id,
      defaultSortKey: "name",
      defaultSortDirection: "desc",
    });

    // Comparator sets the default order.
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("descending");

    // DESC -> unsorted -> ASC (reversed).
    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
  });

  it("remains unsorted without a sortable default column", () => {
    const wrapper = mountTable({
      columns: [{ key: "id", label: "ID", sortable: false }],
      defaultSort: (a, b) => a.id - b.id,
    });

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });
});

describe("DataTable - client-side pagination", () => {
  it("enables pagination with a page size of ten by default", () => {
    const wrapper = mountTable({ items: makeManyRows(12) });

    expect(wrapper.find(".table-pagination").exists()).toBe(true);
    expect(wrapper.get("select.page-size-select").element.value).toBe("10");
    expect(firstColumnValues(wrapper)).toEqual([
      "12",
      "11",
      "10",
      "9",
      "8",
      "7",
      "6",
      "5",
      "4",
      "3",
    ]);
    expect(rangeText(wrapper)).toBe("1–10 von 12");
  });

  it("shows all rows without pagination controls when disabled", () => {
    const wrapper = mountTable({
      items: makeManyRows(12),
      pagination: false,
      pageSize: 5,
    });

    expect(wrapper.find(".table-pagination").exists()).toBe(false);
    expect(wrapper.findAll("tbody tr")).toHaveLength(12);
    expect(firstColumnValues(wrapper)[0]).toBe("12");
  });

  it("navigates to the next and previous pages", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    expect(rangeText(wrapper)).toBe("1–5 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["12", "11", "10", "9", "8"]);

    await nextButton(wrapper).trigger("click");
    expect(rangeText(wrapper)).toBe("6–10 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["7", "6", "5", "4", "3"]);

    await previousButton(wrapper).trigger("click");
    expect(rangeText(wrapper)).toBe("1–5 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["12", "11", "10", "9", "8"]);
  });

  it("shows only remaining rows on the last page", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await nextButton(wrapper).trigger("click");

    expect(rangeText(wrapper)).toBe("11–12 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["2", "1"]);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("disables navigation at the first and last pages", async () => {
    const wrapper = mountTable({ items: makeManyRows(11), pageSize: 5 });

    expect(previousButton(wrapper).element.disabled).toBe(true);
    expect(nextButton(wrapper).element.disabled).toBe(false);

    await nextButton(wrapper).trigger("click");
    expect(previousButton(wrapper).element.disabled).toBe(false);
    expect(nextButton(wrapper).element.disabled).toBe(false);

    await nextButton(wrapper).trigger("click");
    expect(previousButton(wrapper).element.disabled).toBe(false);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("disables both navigation buttons for one page", () => {
    const wrapper = mountTable();

    expect(previousButton(wrapper).element.disabled).toBe(true);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("disables navigation for an empty table", () => {
    const wrapper = mountTable({ items: [] });

    expect(previousButton(wrapper).element.disabled).toBe(true);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("renders the page size label and default choices", () => {
    const wrapper = mountTable();

    expect(wrapper.get(".page-size-control").text()).toContain(
      "Zeilen pro Seite"
    );
    expect(availableSizes(wrapper)).toEqual([5, 10, 25, 50]);
  });

  it("deduplicates and sorts page size options", () => {
    const wrapper = mountTable({
      pageSize: 7,
      pageSizeOptions: [25, 5, 25, 10, 5],
    });

    expect(availableSizes(wrapper)).toEqual([5, 7, 10, 25]);
    expect(wrapper.get("select").element.value).toBe("7");
  });

  it.each([0, -1, 2.5, Number.NaN, Infinity, Number.MAX_SAFE_INTEGER + 1])(
    "resets invalid pageSize=%s to ten",
    (pageSize) => {
      const wrapper = mountTable({ items: makeManyRows(12), pageSize });

      expect(wrapper.get("select").element.value).toBe("10");
      expect(firstColumnValues(wrapper)).toHaveLength(10);
      expect(rangeText(wrapper)).toBe("1–10 von 12");
    }
  );

  it("filters invalid page size options", () => {
    const wrapper = mountTable({
      pageSize: 7,
      pageSizeOptions: [0, -1, 2.5, Number.NaN, Infinity, 5, 5, 1, 10],
    });

    expect(availableSizes(wrapper)).toEqual([1, 5, 7, 10]);
  });

  it("resets to page one when the page size changes", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    expect(rangeText(wrapper)).toBe("6–10 von 12");

    await wrapper.get("select.page-size-select").setValue("10");
    expect(rangeText(wrapper)).toBe("1–10 von 12");
    expect(firstColumnValues(wrapper)).toHaveLength(10);
    expect(previousButton(wrapper).element.disabled).toBe(true);
  });

  it("responds to external page size updates", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ pageSize: 7 });

    expect(wrapper.get("select").element.value).toBe("7");
    expect(availableSizes(wrapper)).toContain(7);
    expect(rangeText(wrapper)).toBe("1–7 von 12");
    expect(firstColumnValues(wrapper)).toHaveLength(7);
  });

  it("resets a subsequently invalid page size to ten", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ pageSize: 0 });

    expect(wrapper.get("select").element.value).toBe("10");
    expect(rangeText(wrapper)).toBe("1–10 von 12");
  });

  it("resets to page one when sorting changes", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await sortButton(wrapper, "ID").trigger("click"); // DESC -> ASC

    expect(rangeText(wrapper)).toBe("1–5 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4", "5"]);
  });

  it("sorts all items before pagination", () => {
    const wrapper = mountTable({
      items: makeManyRows(12),
      defaultSortKey: "id",
      defaultSortDirection: "asc",
      pageSize: 5,
    });

    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4", "5"]);
  });

  it("resets the page after toggling pagination", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ pagination: false });

    expect(wrapper.findAll("tbody tr")).toHaveLength(12);
    expect(wrapper.find(".table-pagination").exists()).toBe(false);

    await wrapper.setProps({ pagination: true });
    expect(rangeText(wrapper)).toBe("1–5 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["12", "11", "10", "9", "8"]);
  });

  it("keeps the sort state when toggling pagination", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await sortButton(wrapper, "ID").trigger("click");
    await wrapper.setProps({ pagination: false });
    await wrapper.setProps({ pagination: true });

    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4", "5"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("ascending");
  });

  it("resets the page when items are replaced", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ items: makeManyRows(12) }); // New array reference.

    expect(rangeText(wrapper)).toBe("1–5 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["12", "11", "10", "9", "8"]);
  });

  it("updates the range when items are removed", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ items: makeManyRows(3) });

    expect(rangeText(wrapper)).toBe("1–3 von 3");
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "1"]);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("responds to length changes within the same reactive array", async () => {
    const items = reactive(makeManyRows(12));
    const wrapper = mountTable({ items, pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await nextButton(wrapper).trigger("click");
    expect(rangeText(wrapper)).toBe("11–12 von 12");

    items.splice(0, 2); // 10 rows, 2 pages.
    await nextTick();

    expect(rangeText(wrapper)).toBe("1–5 von 10");
    expect(firstColumnValues(wrapper)).toEqual(["12", "11", "10", "9", "8"]);
  });

  it("shows the correct range on a full last page", async () => {
    const wrapper = mountTable({ items: makeManyRows(10), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    expect(rangeText(wrapper)).toBe("6–10 von 10");
    expect(firstColumnValues(wrapper)).toHaveLength(5);
  });
});

describe("DataTable - prop updates and accessibility", () => {
  it("updates rows without remounting the table", async () => {
    const wrapper = mountTable();

    await wrapper.setProps({
      items: [{ id: 7, name: "Neu", age: 99, active: false }],
    });
    expect(firstColumnValues(wrapper)).toEqual(["7"]);
    expect(rowCells(wrapper)[0]).toEqual(["7", "Neu", "99", "false"]);
  });

  it("updates columns when their props change", async () => {
    const wrapper = mountTable();

    await wrapper.setProps({ columns: [{ key: "name", label: "Person" }] });

    expect(wrapper.findAll("thead th")).toHaveLength(1);
    expect(header(wrapper, "Person").exists()).toBe(true);
    expect(firstColumnValues(wrapper)).toEqual([
      "Berta",
      "Zoe",
      "Clara",
      "Anna",
    ]);
  });

  it("updates defaultSortKey before manual sorting", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    await wrapper.setProps({ defaultSortKey: "age" });
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "4", "3"]);
    expect(header(wrapper, "Alter").attributes("aria-sort")).toBe("ascending");
  });

  it("updates defaultSortDirection before manual sorting", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    await wrapper.setProps({ defaultSortDirection: "desc" });
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);
  });

  it("preserves manual sorting when default props change", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    await sortButton(wrapper, "Alter").trigger("click");
    await wrapper.setProps({
      defaultSortKey: "id",
      defaultSortDirection: "desc",
    });

    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "4", "3"]);
    expect(header(wrapper, "Alter").attributes("aria-sort")).toBe("ascending");
  });

  it("updates default rowKey sorting when rowKey changes", async () => {
    const wrapper = mountTable({
      items: [
        { id: 2, code: 10, label: "A" },
        { id: 3, code: 5, label: "B" },
        { id: 1, code: 20, label: "C" },
      ],
      columns: [{ key: "label", label: "Text" }],
    });

    expect(firstColumnValues(wrapper)).toEqual(["B", "A", "C"]); // ID DESC
    await wrapper.setProps({ rowKey: "code" });
    expect(firstColumnValues(wrapper)).toEqual(["C", "A", "B"]); // Code DESC
  });

  it('renders sort buttons with type="button"', () => {
    const wrapper = mountTable();

    expect(
      wrapper
        .findAll("thead button")
        .every((button) => button.attributes("type") === "button")
    ).toBe(true);
  });

  it("sets aria-sort only on the active column", () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    expect(wrapper.findAll("th[aria-sort]")).toHaveLength(1);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("ascending");
  });

  it("labels pagination for screen readers", () => {
    const wrapper = mountTable();

    expect(wrapper.get("nav.page-navigation").attributes("aria-label")).toBe(
      "Tabellenseiten"
    );
    expect(wrapper.get(".page-range").attributes("aria-live")).toBe("polite");
    expect(wrapper.get(".page-range").attributes("aria-atomic")).toBe("true");
    expect(previousButton(wrapper).attributes("aria-label")).toBe(
      "Vorherige Seite"
    );
    expect(nextButton(wrapper).attributes("aria-label")).toBe("Nächste Seite");
  });

  it("keeps the table accessible without pagination", () => {
    const wrapper = mountTable({ caption: "Personen", pagination: false });

    expect(wrapper.get("table").classes()).toContain("data-table");
    expect(wrapper.get("caption").text()).toBe("Personen");
    expect(wrapper.findAll("thead th")).toHaveLength(4);
    expect(wrapper.findAll("tbody tr")).toHaveLength(4);
  });
});

describe("DataTable - optional row keys", () => {
  it("renders without rowKey in original order by default", () => {
    const wrapper = mount(DataTable, {
      props: {
        items: [{ name: "Zoe" }, { name: "Anna" }, { name: "Berta" }],
        columns: [{ key: "name", label: "Name" }],
        pagination: false,
      },
    });

    expect(firstColumnValues(wrapper)).toEqual(["Zoe", "Anna", "Berta"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("sorts without rowKey and restores input order after the third click", async () => {
    const wrapper = mountTable({ rowKey: undefined, pagination: false });

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "4", "2", "3"]);
    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);
    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("keeps row component identity when sorting without rowKey", async () => {
    const mounts = [];
    const StatefulCell = {
      props: { name: String },
      setup(props) {
        const initialName = props.name;
        mounts.push(initialName);
        return () => h("span", { class: "initial-name" }, initialName);
      },
    };
    const wrapper = mount(DataTable, {
      props: {
        items: [{ name: "Zoe" }, { name: "Anna" }, { name: "Berta" }],
        columns: [{ key: "name", label: "Name" }],
        pagination: false,
      },
      slots: { "cell-name": ({ row }) => h(StatefulCell, { name: row.name }) },
    });

    expect(firstColumnValues(wrapper)).toEqual(["Zoe", "Anna", "Berta"]);
    expect(mounts).toEqual(["Zoe", "Anna", "Berta"]);

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["Anna", "Berta", "Zoe"]);
    expect(mounts).toHaveLength(3);

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["Zoe", "Berta", "Anna"]);
    expect(mounts).toHaveLength(3);
  });

  it("uses index fallback for missing IDs while sorting and paging", async () => {
    const wrapper = mount(DataTable, {
      props: {
        items: [
          { id: null, name: "C" },
          { id: null, name: "A" },
          { id: null, name: "B" },
        ],
        columns: [{ key: "name", label: "Name" }],
        rowKey: "id",
        defaultSortKey: "name",
        pageSize: 2,
      },
    });

    expect(firstColumnValues(wrapper)).toEqual(["A", "B"]);
    expect(rangeText(wrapper)).toBe("1–2 von 3");
    await nextButton(wrapper).trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["C"]);
  });

  it("respects explicit sort configuration without rowKey", () => {
    const wrapper = mountTable({
      rowKey: undefined,
      defaultSortKey: "name",
      pagination: false,
    });

    expect(firstColumnValues(wrapper)).toEqual(["1", "4", "2", "3"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("ascending");
  });

  it("returns to input order after rowKey is removed", async () => {
    const wrapper = mountTable({ pagination: false });

    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
    await wrapper.setProps({ rowKey: undefined });
    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });
});
