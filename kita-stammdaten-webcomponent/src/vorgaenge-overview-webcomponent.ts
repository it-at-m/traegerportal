import { defineCustomElement } from "vue";

import VorgaengeOverviewVueComponent from "@/vorgaenge-overview.ce.vue";

// convert into custom element constructor
const VorgaengeOverviewWebComponent = defineCustomElement(
  VorgaengeOverviewVueComponent
);

// register
customElements.define("vorgaenge-overview", VorgaengeOverviewWebComponent);
