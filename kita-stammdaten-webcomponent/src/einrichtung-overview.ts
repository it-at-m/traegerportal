import { defineCustomElement } from "vue";

import EinrichtungOverviewVueComponent from "@/einrichtung-overview.ce.vue";

// convert into custom element constructor
const EinrichtungOverviewWebComponent = defineCustomElement(
  EinrichtungOverviewVueComponent
);

// register
customElements.define(
  "einrichtung-overview",
  EinrichtungOverviewWebComponent
);
