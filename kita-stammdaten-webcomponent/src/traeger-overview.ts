import { defineCustomElement } from "vue";

import TraegerOverviewVueComponent from "@/traeger-overview.ce.vue";

// convert into custom element constructor
const TraegerOverviewWebComponent = defineCustomElement(
  TraegerOverviewVueComponent
);

// register
customElements.define(
  "traeger-overview",
  TraegerOverviewWebComponent
);
