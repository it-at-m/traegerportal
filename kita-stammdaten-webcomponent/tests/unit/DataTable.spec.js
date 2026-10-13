/**
 * @file DataTable.spec.js
 * @description Unit tests for DataTable.
 *
 * @generated AI-assisted
 */

// @vitest-environment jsdom
// Requires vitest, @vue/test-utils, vue, @vitejs/plugin-vue, jsdom.
// Adjust the SFC import path.
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
  expect(result, `Spalte ${label} fehlt`).toBeDefined();
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

describe("DataTable – Darstellung und Zellen", () => {
  it('rendert alle Spalten mit ihrem Label und scope="col"', () => {
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

  it("rendert die Werte aller Zeilen in der standardmäßigen ID-DESC-Reihenfolge", () => {
    const wrapper = mountTable({ pagination: false });

    expect(rowCells(wrapper)).toEqual([
      ["4", "Berta", "31", "true"],
      ["3", "Zoe", "40", "true"],
      ["2", "Clara", "22", "false"],
      ["1", "Anna", "22", "false"],
    ]);
  });

  it("zeigt eine Caption nur bei nicht leerem caption-Prop", async () => {
    const wrapper = mountTable({ caption: "Mitarbeitendenübersicht" });

    expect(wrapper.get("caption").text()).toBe("Mitarbeitendenübersicht");
    expect(wrapper.get("caption").classes()).toContain("sr-only");

    await wrapper.setProps({ caption: "" });
    expect(wrapper.find("caption").exists()).toBe(false);
  });

  it("rendert ohne Caption-Prop überhaupt kein caption-Element", () => {
    expect(mountTable().find("caption").exists()).toBe(false);
  });

  it("zeigt null und undefined als leere Zellen an", () => {
    const wrapper = mountTable({
      items: [{ id: 1, name: null, age: undefined }],
      columns: makeColumns().slice(0, 3),
    });

    expect(rowCells(wrapper)).toEqual([["1", "", ""]]);
  });

  it("zeigt Zahlen und Booleans als Strings an", () => {
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

  it("formatiert Date-Zellen als deutsches Datum", () => {
    const date = new Date(2025, 1, 3); // Feb 3, 2025 (local).
    const wrapper = mountTable({
      items: [{ id: 1, date }],
      columns: [{ key: "date", label: "Datum" }],
    });

    expect(firstColumnValues(wrapper)).toEqual([
      date.toLocaleDateString("de-DE"),
    ]);
  });

  it("zeigt nicht unterstützte Objekt- und Array-Zellwerte als leer an", () => {
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

  it("ermittelt einen fehlenden Zellwert als leer", () => {
    const wrapper = mountTable({
      items: [{ id: 1 }],
      columns: [{ key: "missing", label: "Fehlt" }],
    });

    expect(firstColumnValues(wrapper)).toEqual([""]);
  });

  it("nutzt column.value statt des Rohwerts und übergibt die Zeile", () => {
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

  it("verwendet einen benannten cell-<key>-Slot inklusive row, column und value", () => {
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

  it("gibt dem Slot den berechneten Wert aus column.value weiter", () => {
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

  it("zeigt bei leerer Liste eine Leerzustandszeile mit passendem colspan", () => {
    const wrapper = mountTable({ items: [] });

    expect(wrapper.findAll("tbody tr")).toHaveLength(1);
    expect(wrapper.get("tbody td").text()).toBe("Keine Einträge vorhanden.");
    expect(wrapper.get("tbody td").attributes("colspan")).toBe("4");
    expect(rangeText(wrapper)).toBe("0–0 von 0");
  });

  it("setzt colspan bei null Spalten mindestens auf 1", () => {
    const wrapper = mountTable({ items: [], columns: [] });

    expect(wrapper.findAll("thead th")).toHaveLength(0);
    expect(wrapper.get("tbody td").attributes("colspan")).toBe("1");
  });

  it("blendet den Leerzustand aus, sobald Zeilen vorhanden sind", async () => {
    const wrapper = mountTable({ items: [] });

    await wrapper.setProps({ items: makeRows() });
    expect(wrapper.find("tbody").text()).not.toContain(
      "Keine Einträge vorhanden."
    );
    expect(wrapper.findAll("tbody tr")).toHaveLength(4);
  });
});

describe("DataTable – Standardsortierung und Sortierzustand", () => {
  it("sortiert ohne explizite Vorgaben nach rowKey absteigend", () => {
    const wrapper = mountTable({ pagination: false });

    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("descending");
    expect(header(wrapper, "Name").attributes("aria-sort")).toBeUndefined();
  });

  it("funktioniert auch, wenn rowKey nicht in columns enthalten ist", () => {
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

  it("sortiert weiterhin nach rowKey, wenn dessen Spalte nicht sortierbar ist", () => {
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

  it("sortiert numerisch lesbare Strings beim rowKey-Fallback natürlich", () => {
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

  it("ändert beim Klick auf die rowKey-Spalte den Fallback DESC zu ASC", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "ID").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("ascending");
  });

  it("wechselt beim nächsten Klick auf rowKey von ASC zurück zu DESC", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "ID").trigger("click");
    await sortButton(wrapper, "ID").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
  });

  it("startet bei einer anderen Spalte mit ASC und wechselt danach zu DESC", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "4", "2", "3"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("ascending");
    expect(header(wrapper, "ID").attributes("aria-sort")).toBeUndefined();

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("descending");
  });

  it("kehrt nach dem dritten Klick auf eine andere Spalte zur ID-DESC-Sortierung zurück", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Name").trigger("click");

    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("descending");
    expect(header(wrapper, "Name").attributes("aria-sort")).toBeUndefined();
  });

  it("wechselt beim Klick auf eine neue Spalte unmittelbar zu deren ASC-Sortierung", async () => {
    const wrapper = mountTable();

    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Alter").trigger("click");

    // Equal ages retain input order (IDs 1, 2).
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "4", "3"]);
    expect(header(wrapper, "Alter").attributes("aria-sort")).toBe("ascending");
  });

  it("verwendet defaultSortKey mit Richtung ASC", () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    expect(firstColumnValues(wrapper)).toEqual(["1", "4", "2", "3"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("ascending");
  });

  it("verwendet defaultSortKey mit Richtung DESC", () => {
    const wrapper = mountTable({
      defaultSortKey: "name",
      defaultSortDirection: "desc",
    });

    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("descending");
  });

  it("setzt ohne rowKey-Fallback eine Sortierung nach ASC → DESC → unsortiert zurück", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });
    expect(firstColumnValues(wrapper)).toEqual(["1", "4", "2", "3"]);

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("verhindert die Standardsortierung bei unbekanntem defaultSortKey", () => {
    const wrapper = mountTable({ defaultSortKey: "does-not-exist" });

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("verhindert die Standardsortierung bei explizit nicht sortierbarem defaultSortKey", () => {
    const wrapper = mountTable({ defaultSortKey: "active" });

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("lässt nicht sortierbare Spalten ohne Sortierbutton", () => {
    const wrapper = mountTable();

    expect(header(wrapper, "Aktiv").find("button").exists()).toBe(false);
    expect(header(wrapper, "Aktiv").get(".sort-label").text()).toBe("Aktiv");
    expect(wrapper.findAll("thead button.sort-button")).toHaveLength(3);
  });

  it("zeigt nur für die aktive Spalte ein Sortier-Icon", async () => {
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

  it("setzt die Richtung der Icons korrekt und macht sie für Screenreader unsichtbar", async () => {
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

  it("aktualisiert die deutschsprachigen Sortierlabels im Fallback-Zyklus", async () => {
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

  it('verwendet ohne Fallback den Text "Sortierung zurücksetzen"', async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    await sortButton(wrapper, "Name").trigger("click");
    expect(sortButton(wrapper, "Name").attributes("aria-label")).toBe(
      "Name Sortierung zurücksetzen"
    );
  });
});

describe("DataTable – Vergleichslogik", () => {
  it("sortiert Zahlen numerisch statt lexikografisch", () => {
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

  it("sortiert Strings mit numerischen Bestandteilen natürlich", () => {
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

  it("sortiert unabhängig von Groß- und Kleinschreibung", () => {
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

  it("berücksichtigt den de-Collator mit sensitivity=base", () => {
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

  it("sortiert Dates über Zeitstempel aufsteigend und absteigend", async () => {
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

  it("hält null, undefined und den leeren String am Ende der ASC-Sortierung", () => {
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

  it("hält leere Werte auch bei DESC am Ende", () => {
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

  it("behandelt ein ungültiges Date als leeren Sortierwert", () => {
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

  it("belässt gleichwertige Werte dank stabiler Sortierung in Eingabereihenfolge", () => {
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

  it("sortiert mit den durch column.value berechneten Werten", () => {
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

  it("sortiert boolesche Werte über ihre String-Repräsentation", () => {
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

  it("verändert beim Sortieren nicht die übergebenen items", async () => {
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

describe("DataTable – eigener defaultSort-Comparator", () => {
  it("setzt ohne defaultSortKey die erste sortierbare Spalte als Standardspalte", () => {
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

  it("verwendet den Comparator statt des Spaltenwertvergleichs", () => {
    const comparator = vi.fn((a, b) => a.id - b.id);
    const wrapper = mountTable({
      defaultSort: comparator,
      defaultSortKey: "name",
    });

    // Sorts by ID, not name.
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4"]);
    expect(comparator).toHaveBeenCalled();
  });

  it("invertiert den Comparator beim Richtungswechsel", async () => {
    const wrapper = mountTable({
      defaultSort: (a, b) => a.id - b.id,
      defaultSortKey: "name",
    });

    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["4", "3", "2", "1"]);
  });

  it("benutzt nach dem Zurücksetzen wieder die ursprüngliche Datenreihenfolge", async () => {
    const wrapper = mountTable({
      defaultSort: (a, b) => a.id - b.id,
      defaultSortKey: "name",
    });

    await sortButton(wrapper, "Name").trigger("click");
    await sortButton(wrapper, "Name").trigger("click");

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });

  it("benutzt den Comparator auch nach Wechsel zu einer anderen Spalte erneut", async () => {
    const wrapper = mountTable({
      defaultSort: (a, b) => a.id - b.id,
      defaultSortKey: "name",
    });

    await sortButton(wrapper, "Alter").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "4", "3"]);
    await sortButton(wrapper, "Name").trigger("click");
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4"]);
  });

  it("interpretiert den Comparator in der konfigurierten Default-Richtung", async () => {
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

  it("bleibt unsortiert, wenn keine sortierbare Default-Spalte existiert", () => {
    const wrapper = mountTable({
      columns: [{ key: "id", label: "ID", sortable: false }],
      defaultSort: (a, b) => a.id - b.id,
    });

    expect(firstColumnValues(wrapper)).toEqual(["3", "1", "4", "2"]);
    expect(wrapper.find("th[aria-sort]").exists()).toBe(false);
  });
});

describe("DataTable – clientseitige Pagination", () => {
  it("ist standardmäßig aktiviert und startet mit einer Seitengröße von 10", () => {
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

  it("rendert bei pagination=false sämtliche Zeilen ohne Steuerelemente", () => {
    const wrapper = mountTable({
      items: makeManyRows(12),
      pagination: false,
      pageSize: 5,
    });

    expect(wrapper.find(".table-pagination").exists()).toBe(false);
    expect(wrapper.findAll("tbody tr")).toHaveLength(12);
    expect(firstColumnValues(wrapper)[0]).toBe("12");
  });

  it("navigiert auf die nächste und vorherige Seite", async () => {
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

  it("zeigt auf der letzten Seite nur die verbleibenden Zeilen", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await nextButton(wrapper).trigger("click");

    expect(rangeText(wrapper)).toBe("11–12 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["2", "1"]);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("deaktiviert die vorherige Schaltfläche auf Seite 1 und die nächste auf der letzten Seite", async () => {
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

  it("deaktiviert beide Navigationsbuttons bei nur einer Seite", () => {
    const wrapper = mountTable();

    expect(previousButton(wrapper).element.disabled).toBe(true);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("deaktiviert beide Navigationsbuttons für eine leere Tabelle", () => {
    const wrapper = mountTable({ items: [] });

    expect(previousButton(wrapper).element.disabled).toBe(true);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("zeigt das Label der Seitengröße und die voreingestellten Optionen", () => {
    const wrapper = mountTable();

    expect(wrapper.get(".page-size-control").text()).toContain(
      "Zeilen pro Seite"
    );
    expect(availableSizes(wrapper)).toEqual([5, 10, 25, 50]);
  });

  it("sortiert doppelte/unsortierte pageSizeOptions und ergänzt die aktuelle Größe", () => {
    const wrapper = mountTable({
      pageSize: 7,
      pageSizeOptions: [25, 5, 25, 10, 5],
    });

    expect(availableSizes(wrapper)).toEqual([5, 7, 10, 25]);
    expect(wrapper.get("select").element.value).toBe("7");
  });

  it.each([0, -1, 2.5, Number.NaN, Infinity, Number.MAX_SAFE_INTEGER + 1])(
    "setzt die ungültige pageSize=%s auf 10 zurück",
    (pageSize) => {
      const wrapper = mountTable({ items: makeManyRows(12), pageSize });

      expect(wrapper.get("select").element.value).toBe("10");
      expect(firstColumnValues(wrapper)).toHaveLength(10);
      expect(rangeText(wrapper)).toBe("1–10 von 12");
    }
  );

  it("filtert ungültige Werte aus pageSizeOptions", () => {
    const wrapper = mountTable({
      pageSize: 7,
      pageSizeOptions: [0, -1, 2.5, Number.NaN, Infinity, 5, 5, 1, 10],
    });

    expect(availableSizes(wrapper)).toEqual([1, 5, 7, 10]);
  });

  it("ändert die Seitengröße über das Select und kehrt zu Seite 1 zurück", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    expect(rangeText(wrapper)).toBe("6–10 von 12");

    await wrapper.get("select.page-size-select").setValue("10");
    expect(rangeText(wrapper)).toBe("1–10 von 12");
    expect(firstColumnValues(wrapper)).toHaveLength(10);
    expect(previousButton(wrapper).element.disabled).toBe(true);
  });

  it("reagiert auf eine von außen geänderte pageSize und setzt den Index zurück", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ pageSize: 7 });

    expect(wrapper.get("select").element.value).toBe("7");
    expect(availableSizes(wrapper)).toContain(7);
    expect(rangeText(wrapper)).toBe("1–7 von 12");
    expect(firstColumnValues(wrapper)).toHaveLength(7);
  });

  it("setzt eine nachträglich ungültig gewordene Seitengröße auf 10", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ pageSize: 0 });

    expect(wrapper.get("select").element.value).toBe("10");
    expect(rangeText(wrapper)).toBe("1–10 von 12");
  });

  it("setzt bei einer Sortieränderung die aktuelle Seite zurück", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await sortButton(wrapper, "ID").trigger("click"); // DESC -> ASC

    expect(rangeText(wrapper)).toBe("1–5 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4", "5"]);
  });

  it("sortiert alle Datensätze, bevor die Seitenselektion erfolgt", () => {
    const wrapper = mountTable({
      items: makeManyRows(12),
      defaultSortKey: "id",
      defaultSortDirection: "asc",
      pageSize: 5,
    });

    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4", "5"]);
  });

  it("setzt den Index beim Deaktivieren und erneuten Aktivieren zurück", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ pagination: false });

    expect(wrapper.findAll("tbody tr")).toHaveLength(12);
    expect(wrapper.find(".table-pagination").exists()).toBe(false);

    await wrapper.setProps({ pagination: true });
    expect(rangeText(wrapper)).toBe("1–5 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["12", "11", "10", "9", "8"]);
  });

  it("behält beim Umschalten der Pagination die aktuelle Sortierung bei", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await sortButton(wrapper, "ID").trigger("click");
    await wrapper.setProps({ pagination: false });
    await wrapper.setProps({ pagination: true });

    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "3", "4", "5"]);
    expect(header(wrapper, "ID").attributes("aria-sort")).toBe("ascending");
  });

  it("setzt beim Austausch der items die Seitennummer zurück", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ items: makeManyRows(12) }); // New array reference.

    expect(rangeText(wrapper)).toBe("1–5 von 12");
    expect(firstColumnValues(wrapper)).toEqual(["12", "11", "10", "9", "8"]);
  });

  it("aktualisiert die Anzeige, wenn die Datenmenge schrumpft", async () => {
    const wrapper = mountTable({ items: makeManyRows(12), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    await wrapper.setProps({ items: makeManyRows(3) });

    expect(rangeText(wrapper)).toBe("1–3 von 3");
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "1"]);
    expect(nextButton(wrapper).element.disabled).toBe(true);
  });

  it("beobachtet auch Änderungen an der Array-Länge bei gleicher Referenz", async () => {
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

  it("berechnet den Bereich korrekt bei exakt voller letzter Seite", async () => {
    const wrapper = mountTable({ items: makeManyRows(10), pageSize: 5 });

    await nextButton(wrapper).trigger("click");
    expect(rangeText(wrapper)).toBe("6–10 von 10");
    expect(firstColumnValues(wrapper)).toHaveLength(5);
  });
});

describe("DataTable – Prop-Updates und Barrierefreiheit", () => {
  it("aktualisiert Zeilen bei neuen items ohne Remount", async () => {
    const wrapper = mountTable();

    await wrapper.setProps({
      items: [{ id: 7, name: "Neu", age: 99, active: false }],
    });
    expect(firstColumnValues(wrapper)).toEqual(["7"]);
    expect(rowCells(wrapper)[0]).toEqual(["7", "Neu", "99", "false"]);
  });

  it("aktualisiert die Spalten bei neuen columns", async () => {
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

  it("übernimmt eine neue defaultSortKey-Prop solange keine Sortierung überschrieben wurde", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    await wrapper.setProps({ defaultSortKey: "age" });
    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "4", "3"]);
    expect(header(wrapper, "Alter").attributes("aria-sort")).toBe("ascending");
  });

  it("übernimmt eine neue defaultSortDirection solange keine Sortierung überschrieben wurde", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    await wrapper.setProps({ defaultSortDirection: "desc" });
    expect(firstColumnValues(wrapper)).toEqual(["3", "2", "4", "1"]);
  });

  it("behält die manuell gewählte Sortierung beim Wechsel der Default-Props bei", async () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    await sortButton(wrapper, "Alter").trigger("click");
    await wrapper.setProps({
      defaultSortKey: "id",
      defaultSortDirection: "desc",
    });

    expect(firstColumnValues(wrapper)).toEqual(["1", "2", "4", "3"]);
    expect(header(wrapper, "Alter").attributes("aria-sort")).toBe("ascending");
  });

  it("aktualisiert den rowKey-Fallback, wenn rowKey geändert wird", async () => {
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

  it('stellt Sortierbuttons als type="button" dar', () => {
    const wrapper = mountTable();

    expect(
      wrapper
        .findAll("thead button")
        .every((button) => button.attributes("type") === "button")
    ).toBe(true);
  });

  it("setzt die aria-sort-Attribute nur auf die aktive Spaltenüberschrift", () => {
    const wrapper = mountTable({ defaultSortKey: "name" });

    expect(wrapper.findAll("th[aria-sort]")).toHaveLength(1);
    expect(header(wrapper, "Name").attributes("aria-sort")).toBe("ascending");
  });

  it("kennzeichnet die Seitennavigation und -bereichsausgabe für Screenreader", () => {
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

  it("hält die Tabellenstruktur bei deaktivierter Pagination zugänglich", () => {
    const wrapper = mountTable({ caption: "Personen", pagination: false });

    expect(wrapper.get("table").classes()).toContain("data-table");
    expect(wrapper.get("caption").text()).toBe("Personen");
    expect(wrapper.findAll("thead th")).toHaveLength(4);
    expect(wrapper.findAll("tbody tr")).toHaveLength(4);
  });
});
